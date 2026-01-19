package com.dingdong.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dingdong.entity.user.RelationHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 关系历史记录 Mapper
 */
@Mapper
public interface RelationHistoryMapper extends BaseMapper<RelationHistory> {
}
