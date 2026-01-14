package com.dingdong.dto.user;

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

    /** 子女ID */
    private Long childId;

    /** 老人ID */
    private Long elderId;
}
