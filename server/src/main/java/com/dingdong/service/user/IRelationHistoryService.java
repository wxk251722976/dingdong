package com.dingdong.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dingdong.entity.user.RelationHistory;

import java.util.List;

/**
 * 关系历史记录服务接口
 */
public interface IRelationHistoryService extends IService<RelationHistory> {

    /**
     * 记录关系操作历史
     * 
     * @param relationId 关系ID
     * @param actionType 操作类型
     * @param operatorId 操作人ID
     * @param reason     原因/备注
     */
    void recordHistory(Long relationId, Integer actionType, Long operatorId, String reason);

    /**
     * 获取关系的历史记录列表
     * 
     * @param relationId 关系ID
     * @return 历史记录列表
     */
    List<RelationHistory> getHistoryByRelationId(Long relationId);
}
