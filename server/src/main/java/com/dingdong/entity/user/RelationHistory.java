package com.dingdong.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关系历史记录实体类
 * 记录关系的所有操作历史（绑定、解绑发起、解绑撤回、解绑完成等）
 */
@Data
@TableName("relation_history")
public class RelationHistory {
    /** 主键ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关系ID */
    private Long relationId;

    /**
     * 操作类型
     * 1-绑定, 2-发起解绑, 3-撤回解绑, 4-解绑完成
     */
    private Integer actionType;

    /** 操作人ID */
    private Long operatorId;

    /** 操作原因/备注 */
    private String reason;

    /** 创建时间 */
    private LocalDateTime createTime;
}
