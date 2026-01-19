package com.dingdong.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dingdong.common.constant.RelationActionType;
import com.dingdong.common.constant.RelationStatus;
import com.dingdong.entity.user.UserRelation;
import com.dingdong.service.unbind.UnbindQueueService;
import com.dingdong.service.user.IRelationHistoryService;
import com.dingdong.service.user.IUserRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 解绑扫描任务
 * 基于 Redis ZSet 延迟队列实现，每分钟检查到期的解绑任务
 * 
 * 优势：
 * 1. 精确度高：每分钟检查，最多延迟1分钟
 * 2. 性能好：只查询 Redis，不扫描数据库
 * 3. 可靠性：数据持久化在 Redis，应用重启不丢失
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnbindScanTask {

    private final IUserRelationService userRelationService;
    private final UnbindQueueService unbindQueueService;
    private final IRelationHistoryService relationHistoryService;

    /**
     * 每分钟执行一次，检查到期的解绑任务
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scanAndUnbind() {
        // 获取所有到期的解绑任务
        Set<String> dueTasks = unbindQueueService.getDueUnbindTasks();

        if (dueTasks == null || dueTasks.isEmpty()) {
            return; // 无任务时静默返回，减少日志噪音
        }

        log.info("发现 {} 条到期解绑申请，开始处理...", dueTasks.size());

        for (String relationIdStr : dueTasks) {
            Long relationId = Long.parseLong(relationIdStr);
            try {
                // 获取关系记录，确认仍然存在且仍需解绑
                UserRelation relation = userRelationService.getById(relationId);

                if (relation == null) {
                    // 关系已不存在，移除任务
                    unbindQueueService.markTaskCompleted(relationId);
                    log.warn("关系记录不存在，跳过: relationId={}", relationId);
                    continue;
                }

                // 检查是否仍在解绑中状态
                if (!RelationStatus.UNBINDING.getCode().equals(relation.getStatus())) {
                    // 解绑已被撤回，移除任务
                    unbindQueueService.markTaskCompleted(relationId);
                    log.info("解绑已撤回，跳过: relationId={}", relationId);
                    continue;
                }

                // 执行解绑：将状态改为已解绑（不再逻辑删除，保留历史）
                relation.setStatus(RelationStatus.UNBOUND.getCode());
                relation.setUnbindExpireTime(null);
                boolean success = userRelationService.updateById(relation);

                if (success) {
                    // 设置冷却期（24小时内不能重新绑定）
                    unbindQueueService.setCooldown(relation.getInitiatorId(), relation.getPartnerId());

                    // 记录历史
                    relationHistoryService.recordHistory(relationId, RelationActionType.UNBIND_COMPLETED.getCode(),
                            0L, "解绑自动生效");

                    // 从队列移除已完成的任务
                    unbindQueueService.markTaskCompleted(relationId);

                    log.info("关系解绑成功: id={}, initiator={}, partner={}",
                            relationId, relation.getInitiatorId(), relation.getPartnerId());
                } else {
                    log.error("关系解绑失败: id={}", relationId);
                }

            } catch (Exception e) {
                log.error("处理解绑任务异常: relationId={}", relationId, e);
            }
        }
    }

    /**
     * 每小时执行一次，作为兜底机制
     * 扫描数据库中可能遗漏的解绑任务（如 Redis 宕机恢复后）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void fallbackScan() {
        log.debug("执行兜底扫描...");

        // 查找数据库中状态为解绑中且 unbind_expire_time 已过期的记录
        LocalDateTime now = LocalDateTime.now();

        List<UserRelation> relations = userRelationService.list(
                new LambdaQueryWrapper<UserRelation>()
                        .eq(UserRelation::getStatus, RelationStatus.UNBINDING.getCode())
                        .isNotNull(UserRelation::getUnbindExpireTime)
                        .lt(UserRelation::getUnbindExpireTime, now));

        if (relations.isEmpty()) {
            return;
        }

        log.info("兜底扫描发现 {} 条遗漏的解绑任务", relations.size());

        for (UserRelation relation : relations) {
            try {
                // 重新添加到 Redis 队列（会立即被下一次轮询处理）
                // 使用过去的时间确保立即执行
                unbindQueueService.addUnbindTask(relation.getId(), relation.getUnbindExpireTime().minusHours(24));
            } catch (Exception e) {
                log.error("兜底任务添加失败: id={}", relation.getId(), e);
            }
        }
    }
}
