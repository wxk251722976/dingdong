package com.dingdong.dto.user;

import lombok.Data;
import java.io.Serializable;

/**
 * 角色切换请求参数 DTO
 * 
 * @author Antigravity
 */
@Data
public class RoleSwitchDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @jakarta.validation.constraints.NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 目标角色代码 */
    @jakarta.validation.constraints.NotBlank(message = "角色不能为空")
    private String role;
}
