package com.dingdong.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * 封装常用的 Redis 操作
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    // ========== 通用操作 ==========

    /**
     * 设置过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("设置过期时间失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间（秒），-1 表示永不过期，-2 表示 key 不存在
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断 key 是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("判断 key 是否存在失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 删除 key
     *
     * @param keys 键（可变参数）
     * @return 删除的数量
     */
    public Long delete(String... keys) {
        if (keys == null || keys.length == 0) {
            return 0L;
        }
        return redisTemplate.delete(List.of(keys));
    }

    /**
     * 批量删除 key
     *
     * @param keys 键集合
     * @return 删除的数量
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    // ========== String 操作 ==========

    /**
     * 设置值
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.error("设置值失败，key: {}", key, e);
        }
    }

    /**
     * 设置值并指定过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("设置值失败，key: {}", key, e);
        }
    }

    /**
     * 获取值
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取字符串值
     *
     * @param key 键
     * @return 值
     */
    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 递增量
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 递减量
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ========== Hash 操作 ==========

    /**
     * 获取 Hash 中指定字段的值
     *
     * @param key   键
     * @param field 字段
     * @return 值
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 设置 Hash 中指定字段的值
     *
     * @param key   键
     * @param field 字段
     * @param value 值
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 获取 Hash 中所有字段和值
     *
     * @param key 键
     * @return 字段-值映射
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 批量设置 Hash 字段
     *
     * @param key 键
     * @param map 字段-值映射
     */
    public void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 删除 Hash 中的字段
     *
     * @param key    键
     * @param fields 字段（可变参数）
     * @return 删除的数量
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 判断 Hash 中是否存在字段
     *
     * @param key   键
     * @param field 字段
     * @return 是否存在
     */
    public Boolean hHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * Hash 字段递增
     *
     * @param key   键
     * @param field 字段
     * @param delta 递增量
     * @return 递增后的值
     */
    public Long hIncrement(String key, String field, long delta) {
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    // ========== Set 操作 ==========

    /**
     * 获取 Set 中所有成员
     *
     * @param key 键
     * @return 成员集合
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断 Set 中是否存在成员
     *
     * @param key   键
     * @param value 值
     * @return 是否存在
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 向 Set 中添加成员
     *
     * @param key    键
     * @param values 值（可变参数）
     * @return 添加的数量
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 获取 Set 的大小
     *
     * @param key 键
     * @return 大小
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 从 Set 中移除成员
     *
     * @param key    键
     * @param values 值（可变参数）
     * @return 移除的数量
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    // ========== List 操作 ==========

    /**
     * 获取 List 指定范围内的元素
     *
     * @param key   键
     * @param start 起始索引
     * @param end   结束索引
     * @return 元素列表
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取 List 的长度
     *
     * @param key 键
     * @return 长度
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 获取 List 指定索引的元素
     *
     * @param key   键
     * @param index 索引
     * @return 元素
     */
    public Object lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 向 List 右侧添加元素
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    public Long lRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 向 List 左侧添加元素
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    public Long lLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 从 List 右侧弹出元素
     *
     * @param key 键
     * @return 元素
     */
    public Object lRightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 从 List 左侧弹出元素
     *
     * @param key 键
     * @return 元素
     */
    public Object lLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    // ========== ZSet (有序集合) 操作 ==========

    /**
     * 向 ZSet 中添加成员
     *
     * @param key   键
     * @param value 值
     * @param score 分数
     * @return 是否成功
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 获取 ZSet 指定范围内的成员
     *
     * @param key   键
     * @param start 起始索引
     * @param end   结束索引
     * @return 成员集合
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 获取 ZSet 的大小
     *
     * @param key 键
     * @return 大小
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 获取 ZSet 成员的分数
     *
     * @param key   键
     * @param value 值
     * @return 分数
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    // ========== Bitmap 操作 ==========

    /**
     * 设置 Bitmap 指定位置的值
     *
     * @param key    键
     * @param offset 偏移量（位置）
     * @param value  值（true=1, false=0）
     * @return 设置前该位置的值
     */
    public Boolean setBit(String key, long offset, boolean value) {
        return redisTemplate.opsForValue().setBit(key, offset, value);
    }

    /**
     * 获取 Bitmap 指定位置的值
     *
     * @param key    键
     * @param offset 偏移量（位置）
     * @return 该位置的值（true=1, false=0）
     */
    public Boolean getBit(String key, long offset) {
        return redisTemplate.opsForValue().getBit(key, offset);
    }

    /**
     * 统计 Bitmap 中值为 1 的位数
     *
     * @param key 键
     * @return 值为 1 的位数
     */
    public Long bitCount(String key) {
        return redisTemplate
                .execute((RedisCallback<Long>) connection -> connection.stringCommands().bitCount(key.getBytes()));
    }

    /**
     * 统计 Bitmap 指定范围内值为 1 的位数
     *
     * @param key   键
     * @param start 起始字节位置
     * @param end   结束字节位置
     * @return 值为 1 的位数
     */
    public Long bitCount(String key, long start, long end) {
        return redisTemplate.execute(
                (RedisCallback<Long>) connection -> connection.stringCommands().bitCount(key.getBytes(), start, end));
    }

    /**
     * 对多个 Bitmap 执行位运算
     *
     * @param op      位运算类型（AND, OR, XOR, NOT）
     * @param destKey 结果存储的键
     * @param keys    参与运算的键
     * @return 结果 Bitmap 的大小（字节数）
     */
    public Long bitOp(RedisStringCommands.BitOperation op, String destKey, String... keys) {
        byte[][] keyBytes = new byte[keys.length][];
        for (int i = 0; i < keys.length; i++) {
            keyBytes[i] = keys[i].getBytes();
        }
        return redisTemplate.execute((RedisCallback<Long>) connection -> connection.stringCommands().bitOp(op,
                destKey.getBytes(), keyBytes));
    }

    /**
     * 查找 Bitmap 中第一个值为指定值的位置
     *
     * @param key   键
     * @param value 要查找的值（true=1, false=0）
     * @return 位置，如果未找到返回 -1
     */
    public Long bitPos(String key, boolean value) {
        return redisTemplate
                .execute((RedisCallback<Long>) connection -> connection.stringCommands().bitPos(key.getBytes(), value));
    }

    /**
     * 查找 Bitmap 指定范围内第一个值为指定值的位置
     *
     * @param key   键
     * @param value 要查找的值（true=1, false=0）
     * @param start 起始字节位置
     * @param end   结束字节位置
     * @return 位置，如果未找到返回 -1
     */
    public Long bitPos(String key, boolean value, long start, long end) {
        return redisTemplate.execute((RedisCallback<Long>) connection -> {
            org.springframework.data.domain.Range<Long> range = org.springframework.data.domain.Range.closed(start,
                    end);
            return connection.stringCommands().bitPos(key.getBytes(), value, range);
        });
    }
}
