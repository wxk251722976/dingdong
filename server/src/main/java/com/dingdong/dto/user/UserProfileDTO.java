package com.dingdong.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户资料更新 DTO
 */
@Data
public class UserProfileDTO {

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;
}
