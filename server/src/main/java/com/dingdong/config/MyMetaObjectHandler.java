package com.dingdong.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.dingdong.common.context.SystemContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MyBatis-Plus 字段自动填充处理器
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        LocalDateTime now = LocalDateTime.now();

        // 填充创建时间和更新时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);

        // 从登录上下文获取当前用户ID
        Long currentUserId = Objects.requireNonNullElse(SystemContextHolder.getUserId(), 0L);

        String currentUsername = Objects.requireNonNullElse(SystemContextHolder.getUsername(), "");

        this.strictInsertFill(metaObject, "createBy", Long.class, currentUserId);
        this.strictInsertFill(metaObject, "createName", String.class, currentUsername);
        this.strictInsertFill(metaObject, "updateBy", Long.class, currentUserId);
        this.strictInsertFill(metaObject, "updateName", String.class, currentUsername);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");

        // 填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // 从登录上下文获取当前用户ID
        Long currentUserId = Objects.requireNonNullElse(SystemContextHolder.getUserId(), 0L);

        String currentUsername = Objects.requireNonNullElse(SystemContextHolder.getUsername(), "");

        this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserId);
        this.strictUpdateFill(metaObject, "updateName", String.class, currentUsername);
    }
}
