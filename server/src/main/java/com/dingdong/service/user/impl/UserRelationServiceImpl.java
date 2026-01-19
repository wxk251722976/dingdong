package com.dingdong.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.common.constant.RelationActionType;
import com.dingdong.common.constant.RelationStatus;
import com.dingdong.dto.user.BindDTO;
import com.dingdong.dto.user.RelationDisplayDTO;
import com.dingdong.entity.user.SysUser;
import com.dingdong.entity.user.UserRelation;
import com.dingdong.mapper.user.UserRelationMapper;
import com.dingdong.service.unbind.UnbindQueueService;
import com.dingdong.service.user.IRelationHistoryService;
import com.dingdong.service.user.ISysUserService;
import com.dingdong.service.user.IUserRelationService;
import com.dingdong.service.wechat.SubscribeMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户关系业务实现
 * 处理用户之间的关系绑定、邀请确认等业务逻辑
 * 
 * 重构说明：
 * - 使用 initiatorId（发起人）和 partnerId（伙伴）替代 supervisorId/supervisedId
 * - 关系是平等的，两个用户可以互相给对方布置任务
 * - 状态: 0-待确认, 1-已接受, 2-已拒绝, 3-解绑中
 */
@Service
@RequiredArgsConstructor
public class UserRelationServiceImpl extends ServiceImpl<UserRelationMapper, UserRelation>
        implements IUserRelationService {

    private final ISysUserService sysUserService;
    private final UnbindQueueService unbindQueueService;
    private final IRelationHistoryService relationHistoryService;
    private final SubscribeMessageService subscribeMessageService;

    @Override
    public List<SysUser> getSupervisedListBySupervisorId(Long userId) {
        // 查询该用户绑定的所有已接受关系
        List<UserRelation> relations = this.list(new LambdaQueryWrapper<UserRelation>()
                .and(w -> w.eq(UserRelation::getInitiatorId, userId).or().eq(UserRelation::getPartnerId, userId))
                .eq(UserRelation::getStatus, RelationStatus.ACCEPTED.getCode()));

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取对方用户ID列表并批量查询用户信息
        List<Long> partnerIds = relations.stream()
                .map(r -> r.getInitiatorId().equals(userId) ? r.getPartnerId() : r.getInitiatorId())
                .collect(Collectors.toList());

        return sysUserService.listByIds(partnerIds);
    }

    @Override
    public boolean bindSupervised(BindDTO bindDTO) {
        Long initiatorId = bindDTO.getSupervisorId(); // 发起人（原supervisorId）
        Long partnerId = bindDTO.getSupervisedId(); // 伙伴（原supervisedId）

        // 1. 检查 Redis 冷却期（解绑24小时后才可以重新发起）
        if (unbindQueueService.isInCooldown(initiatorId, partnerId)) {
            Long remainingSeconds = unbindQueueService.getCooldownRemainingSeconds(initiatorId, partnerId);
            long remainingHours = remainingSeconds != null ? remainingSeconds / 3600 : 24;
            throw new RuntimeException("解绑后需等待24小时才能重新绑定，剩余约" + remainingHours + "小时");
        }

        // 2. 检查是否已存在关系（无论谁是发起人）
        long count = this.count(new LambdaQueryWrapper<UserRelation>()
                .and(w -> w
                        .eq(UserRelation::getInitiatorId, initiatorId)
                        .eq(UserRelation::getPartnerId, partnerId))
                .or(w -> w
                        .eq(UserRelation::getInitiatorId, partnerId)
                        .eq(UserRelation::getPartnerId, initiatorId))
                .in(UserRelation::getStatus, Arrays.asList(
                        RelationStatus.ACCEPTED.getCode(),
                        RelationStatus.PENDING.getCode(),
                        RelationStatus.UNBINDING.getCode())));

        if (count > 0) {
            throw new RuntimeException("关系已存在或正在处理中");
        }

        // 3. 创建新关系
        UserRelation relation = new UserRelation();
        relation.setInitiatorId(initiatorId);
        relation.setPartnerId(partnerId);
        relation.setRelationName(bindDTO.getRelationName());
        relation.setRelationType(bindDTO.getRelationType() != null ? bindDTO.getRelationType() : 0);
        relation.setStatus(RelationStatus.ACCEPTED.getCode());
        boolean success = this.save(relation);

        // 4. 记录历史
        if (success) {
            relationHistoryService.recordHistory(relation.getId(), RelationActionType.BIND.getCode(),
                    initiatorId, "绑定关系: " + bindDTO.getRelationName());
        }

        return success;
    }

    @Override
    public List<UserRelation> getMyRelations(Long userId) {
        // 查询用户参与的所有关系（可能是发起人或伙伴）
        return this.list(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getInitiatorId, userId)
                .or()
                .eq(UserRelation::getPartnerId, userId));
    }

    @Override
    public boolean sendInvite(UserRelation relation) {
        relation.setStatus(RelationStatus.PENDING.getCode());
        return this.save(relation);
    }

    @Override
    public boolean acceptInvite(Long relationId) {
        UserRelation relation = this.getById(relationId);
        if (relation == null) {
            throw new RuntimeException("关系记录不存在");
        }

        relation.setStatus(RelationStatus.ACCEPTED.getCode());
        boolean success = this.updateById(relation);

        if (success) {
            relationHistoryService.recordHistory(relationId, RelationActionType.BIND.getCode(),
                    relation.getPartnerId(), "接受邀请");
        }

        return success;
    }

    @Override
    public boolean rejectInvite(Long relationId) {
        UserRelation relation = this.getById(relationId);
        if (relation == null) {
            throw new RuntimeException("关系记录不存在");
        }

        relation.setStatus(RelationStatus.REJECTED.getCode());
        return this.updateById(relation);
    }

    @Override
    public List<RelationDisplayDTO> getRelationsWithUserInfo(Long userId) {
        // 1. 获取所有关系
        List<UserRelation> relations = getMyRelations(userId);
        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 收集所有需要查询的对方用户ID
        Set<Long> userIds = new HashSet<>();
        for (UserRelation r : relations) {
            if (r.getInitiatorId() != null && !r.getInitiatorId().equals(userId)) {
                userIds.add(r.getInitiatorId());
            }
            if (r.getPartnerId() != null && !r.getPartnerId().equals(userId)) {
                userIds.add(r.getPartnerId());
            }
        }

        // 3. 批量查询用户信息
        Map<Long, SysUser> userMap = userIds.isEmpty() ? new HashMap<>()
                : sysUserService.listByIds(userIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, Function.identity()));

        // 4. 组装展示对象DTO
        return relations.stream().map(r -> {
            RelationDisplayDTO dto = new RelationDisplayDTO();
            dto.setId(r.getId());
            dto.setRelationName(r.getRelationName());
            dto.setStatus(r.getStatus());

            // 确定对方用户ID（平等关系，无需区分角色）
            Long otherUserId = r.getInitiatorId().equals(userId) ? r.getPartnerId() : r.getInitiatorId();
            dto.setOtherUserId(otherUserId);

            // 旧字段兼容（前端可能还在使用）
            dto.setInitiatorId(r.getInitiatorId());
            dto.setPartnerId(r.getPartnerId());

            // 设置角色（保持向后兼容，但现在关系是平等的）
            if (userId.equals(r.getInitiatorId())) {
                dto.setRole("INITIATOR");
            } else {
                dto.setRole("PARTNER");
            }

            SysUser otherUser = userMap.get(otherUserId);
            if (otherUser != null) {
                dto.setOtherNickname(otherUser.getNickname());
                dto.setOtherAvatar(otherUser.getAvatar());
            } else {
                dto.setOtherNickname("未知用户");
            }

            // 如果正在解绑中，添加解绑信息
            if (RelationStatus.UNBINDING.getCode().equals(r.getStatus()) && r.getUnbindExpireTime() != null) {
                dto.setUnbindExpireTime(r.getUnbindExpireTime());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean initiateUnbind(Long relationId, String reason, Long operatorId) {
        UserRelation relation = this.getById(relationId);
        if (relation == null) {
            throw new RuntimeException("关系不存在");
        }

        // 验证操作人是否在关系中
        if (!operatorId.equals(relation.getInitiatorId()) && !operatorId.equals(relation.getPartnerId())) {
            throw new RuntimeException("非法操作：您不是关系成员");
        }

        if (RelationStatus.UNBINDING.getCode().equals(relation.getStatus())) {
            throw new RuntimeException("已在解绑流程中");
        }

        // 确定对方用户ID
        Long otherUserId = operatorId.equals(relation.getInitiatorId())
                ? relation.getPartnerId()
                : relation.getInitiatorId();

        SysUser operator = sysUserService.getById(operatorId);
        SysUser otherUser = sysUserService.getById(otherUserId);

        // 更新关系状态为解绑中
        LocalDateTime unbindExpireTime = LocalDateTime.now().plusHours(24);
        relation.setStatus(RelationStatus.UNBINDING.getCode());
        relation.setUnbindExpireTime(unbindExpireTime);
        boolean success = this.updateById(relation);

        if (success) {
            // 添加解绑任务到 Redis 延迟队列
            unbindQueueService.addUnbindTask(relationId, LocalDateTime.now());

            // 记录历史
            relationHistoryService.recordHistory(relationId, RelationActionType.UNBIND_INITIATED.getCode(),
                    operatorId, reason);

            // 发送通知
            if (otherUser != null && otherUser.getOpenid() != null) {
                String initiatorName = (operator != null) ? operator.getNickname() : "对方";
                subscribeMessageService.sendUnbindMessage(otherUser.getOpenid(), initiatorName, reason,
                        relation.getRelationName());
            }
        }

        return success;
    }

    @Override
    public boolean withdrawUnbind(Long relationId, Long operatorId) {
        UserRelation relation = this.getById(relationId);
        if (relation == null) {
            throw new RuntimeException("关系不存在");
        }

        // 验证操作人是否在关系中
        if (!operatorId.equals(relation.getInitiatorId()) && !operatorId.equals(relation.getPartnerId())) {
            throw new RuntimeException("非法操作：您不是关系成员");
        }

        if (!RelationStatus.UNBINDING.getCode().equals(relation.getStatus())) {
            throw new RuntimeException("关系未在解绑流程中");
        }

        // 恢复为已接受状态
        relation.setStatus(RelationStatus.ACCEPTED.getCode());
        relation.setUnbindExpireTime(null);

        boolean success = this.updateById(relation);

        if (success) {
            // 从 Redis 延迟队列移除解绑任务
            unbindQueueService.removeUnbindTask(relationId);

            // 记录历史
            relationHistoryService.recordHistory(relationId, RelationActionType.UNBIND_WITHDRAWN.getCode(),
                    operatorId, "撤回解绑");
        }

        return success;
    }

    @Override
    public boolean checkRelation(Long userId, Long otherUserId) {
        if (userId.equals(otherUserId)) {
            return true;
        }

        return this.count(new LambdaQueryWrapper<UserRelation>()
                .and(w -> w.eq(UserRelation::getInitiatorId, userId).eq(UserRelation::getPartnerId, otherUserId))
                .or(w -> w.eq(UserRelation::getInitiatorId, otherUserId).eq(UserRelation::getPartnerId, userId))
                .eq(UserRelation::getStatus, RelationStatus.ACCEPTED.getCode())) > 0;
    }
}
