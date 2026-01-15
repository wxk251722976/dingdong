package com.dingdong.dto.auth;

import com.dingdong.entity.user.SysUser;
import lombok.Data;

/**
 * 登录响应 DTO
 */
@Data
public class LoginResultDTO {
    /** 用户信息 */
    private SysUser user;

    /** JWT Token (Access Token) */
    private String token;

    /** Refresh Token */
    private String refreshToken;

    public LoginResultDTO(SysUser user, String token, String refreshToken) {
        this.user = user;
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
