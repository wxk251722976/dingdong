package com.dingdong.dto.auth;

import lombok.Data;
import java.io.Serializable;

/**
 * 登录请求参数 DTO
 * 
 * @author Antigravity
 */
@Data
public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 微信登录code */
    @jakarta.validation.constraints.NotBlank(message = "code不能为空")
    private String code;

    /** 用户昵称 */
    private String nickname;

    /** 用户头像 */
    private String avatar;
}
