package com.dingdong.mapper.notification;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dingdong.entity.notification.NotificationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知推送记录 Mapper 接口
 */
@Mapper
public interface NotificationLogMapper extends BaseMapper<NotificationLog> {
}
