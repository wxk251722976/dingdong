package com.dingdong.common.base;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 包含审计字段和逻辑删除字段
 */
@Data
public class BaseEntity implements Serializable {

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 创建人ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建人名称 */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 更新人ID */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 更新人名称 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    /** 逻辑删除标记: 0-未删除, 1-已删除 */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
