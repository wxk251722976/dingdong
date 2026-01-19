package com.dingdong.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.entity.user.RelationHistory;
import com.dingdong.mapper.user.RelationHistoryMapper;
import com.dingdong.service.user.IRelationHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 关系历史记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RelationHistoryServiceImpl extends ServiceImpl<RelationHistoryMapper, RelationHistory>
        implements IRelationHistoryService {

    @Override
    public void recordHistory(Long relationId, Integer actionType, Long operatorId, String reason) {
        RelationHistory history = new RelationHistory();
        history.setRelationId(relationId);
        history.setActionType(actionType);
        history.setOperatorId(operatorId);
        history.setReason(reason);
        history.setCreateTime(LocalDateTime.now());

        this.save(history);
        log.info("记录关系历史: relationId={}, actionType={}, operatorId={}", relationId, actionType, operatorId);
    }

    @Override
    public List<RelationHistory> getHistoryByRelationId(Long relationId) {
        return this.list(new LambdaQueryWrapper<RelationHistory>()
                .eq(RelationHistory::getRelationId, relationId)
                .orderByDesc(RelationHistory::getCreateTime));
    }
}
