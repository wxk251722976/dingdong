package com.dingdong.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dingdong.entity.user.UserFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户反馈Mapper
 */
@Mapper
public interface UserFeedbackMapper extends BaseMapper<UserFeedback> {
}
