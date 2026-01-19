package com.dingdong.service.unbind;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 解绑队列服务
 * 使用 Redis 实现解绑延迟队列和冷却期检查
 * 
 * 核心功能：
 * 1. 解绑延迟队列：使用 ZSet 存储待解绑任务，score 为执行时间戳
 * 2. 冷却期检查：使用 String Key + TTL 标记两用户间的冷却期
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnbindQueueService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 解绑延迟队列的 Key
     */
    private static final String UNBIND_QUEUE_KEY = "dingdong:unbind:queue";

    /**
     * 解绑冷却期 Key 前缀
     * 格式: dingdong:unbind:cooldown:{minUserId}:{maxUserId}
     */
    private static final String COOLDOWN_KEY_PREFIX = "dingdong:unbind:cooldown:";

    /**
     * 冷却期时间（24小时）
     */
    private static final Duration COOLDOWN_DURATION = Duration.ofHours(24);

    /**
     * 添加解绑任务到延迟队列
     * 
     * @param relationId 关系ID
     * @param unbindTime 解绑发起时间
     */
    public void addUnbindTask(Long relationId, LocalDateTime unbindTime) {
        // 计算24小时后的执行时间戳
        long executeTimestamp = unbindTime.plusHours(24).toInstant(ZoneOffset.of("+8")).toEpochMilli();

        Boolean added = stringRedisTemplate.opsForZSet().add(
                UNBIND_QUEUE_KEY,
                String.valueOf(relationId),
                executeTimestamp);

        if (Boolean.TRUE.equals(added)) {
            log.info("解绑任务已添加到队列: relationId={}, 执行时间={}",
                    relationId,
                    unbindTime.plusHours(24));
        }
    }

    /**
     * 从延迟队列移除解绑任务（用于撤回解绑）
     * 
     * @param relationId 关系ID
     */
    public void removeUnbindTask(Long relationId) {
        Long removed = stringRedisTemplate.opsForZSet().remove(UNBIND_QUEUE_KEY, String.valueOf(relationId));
        if (removed != null && removed > 0) {
            log.info("解绑任务已从队列移除: relationId={}", relationId);
        }
    }

    /**
     * 获取所有到期的解绑任务
     * 
     * @return 到期的关系ID集合
     */
    public Set<String> getDueUnbindTasks() {
        long currentTimestamp = System.currentTimeMillis();

        // 获取 score <= 当前时间戳 的所有任务
        return stringRedisTemplate.opsForZSet().rangeByScore(
                UNBIND_QUEUE_KEY,
                0,
                currentTimestamp);
    }

    /**
     * 从队列中移除已处理的任务
     * 
     * @param relationId 关系ID
     */
    public void markTaskCompleted(Long relationId) {
        stringRedisTemplate.opsForZSet().remove(UNBIND_QUEUE_KEY, String.valueOf(relationId));
    }

    /**
     * 设置两用户之间的冷却期
     * 解绑完成后调用，标记两用户在24小时内不能重新绑定
     * 
     * @param userId1 用户ID 1
     * @param userId2 用户ID 2
     */
    public void setCooldown(Long userId1, Long userId2) {
        String cooldownKey = buildCooldownKey(userId1, userId2);
        stringRedisTemplate.opsForValue().set(
                cooldownKey,
                String.valueOf(System.currentTimeMillis()),
                COOLDOWN_DURATION.toSeconds(),
                TimeUnit.SECONDS);
        log.info("设置解绑冷却期: {}与{}, 24小时后可重新绑定", userId1, userId2);
    }

    /**
     * 检查两用户之间是否在冷却期
     * 
     * @param userId1 用户ID 1
     * @param userId2 用户ID 2
     * @return true 表示在冷却期内，不能绑定；false 表示可以绑定
     */
    public boolean isInCooldown(Long userId1, Long userId2) {
        String cooldownKey = buildCooldownKey(userId1, userId2);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey));
    }

    /**
     * 获取冷却期剩余时间（秒）
     * 
     * @param userId1 用户ID 1
     * @param userId2 用户ID 2
     * @return 剩余秒数，-1 表示不存在冷却期，-2 表示 key 不存在
     */
    public Long getCooldownRemainingSeconds(Long userId1, Long userId2) {
        String cooldownKey = buildCooldownKey(userId1, userId2);
        return stringRedisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
    }

    /**
     * 构建冷却期 Key
     * 使用较小的 userId 在前，确保无论绑定方向如何，Key 都一致
     * 
     * @param userId1 用户ID 1
     * @param userId2 用户ID 2
     * @return 冷却期 Key
     */
    private String buildCooldownKey(Long userId1, Long userId2) {
        long minId = Math.min(userId1, userId2);
        long maxId = Math.max(userId1, userId2);
        return COOLDOWN_KEY_PREFIX + minId + ":" + maxId;
    }

    /**
     * 获取队列中的任务数量（用于监控）
     * 
     * @return 任务数量
     */
    public Long getQueueSize() {
        return stringRedisTemplate.opsForZSet().size(UNBIND_QUEUE_KEY);
    }

    /**
     * 清除冷却期（可选，用于管理员操作）
     * 
     * @param userId1 用户ID 1
     * @param userId2 用户ID 2
     */
    public void clearCooldown(Long userId1, Long userId2) {
        String cooldownKey = buildCooldownKey(userId1, userId2);
        stringRedisTemplate.delete(cooldownKey);
        log.info("已清除冷却期: {}与{}", userId1, userId2);
    }
}
