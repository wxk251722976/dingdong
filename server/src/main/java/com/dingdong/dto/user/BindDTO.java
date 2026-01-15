package com.dingdong.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 绑定请求参数 DTO
 * 
 * @author Antigravity
 */
@Data
public class BindDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 监督者ID */
    @NotNull(message = "监督者ID不能为空")
    private Long supervisorId;

    /** 被监督者ID */
    @NotNull(message = "被监督者ID不能为空")
    private Long supervisedId;
}
