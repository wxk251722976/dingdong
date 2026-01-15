package com.dingdong.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dingdong.entity.user.UserFeedback;

/**
 * 用户反馈服务接口
 */
public interface IUserFeedbackService extends IService<UserFeedback> {

    /**
     * 提交反馈
     *
     * @param userId  用户ID
     * @param content 反馈内容
     * @param contact 联系方式
     * @return 是否成功
     */
    boolean submitFeedback(Long userId, String content, String contact);

    /**
     * 获取用户今日反馈数量
     *
     * @param userId 用户ID
     * @return 今日反馈数
     */
    int getTodayFeedbackCount(Long userId);
}
