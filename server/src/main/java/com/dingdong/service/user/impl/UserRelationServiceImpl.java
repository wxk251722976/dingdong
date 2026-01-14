package com.dingdong.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.common.constant.RelationStatus;
import com.dingdong.dto.user.BindDTO;
import com.dingdong.entity.user.SysUser;
import com.dingdong.entity.user.UserRelation;
import com.dingdong.mapper.user.UserRelationMapper;
import com.dingdong.service.user.ISysUserService;
import com.dingdong.service.user.IUserRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户关系业务实现
 * 处理用户之间的关系绑定、邀请确认等业务逻辑
 */
@Service
@RequiredArgsConstructor
public class UserRelationServiceImpl extends ServiceImpl<UserRelationMapper, UserRelation>
        implements IUserRelationService {

    private final ISysUserService sysUserService;

    @Override
    public List<SysUser> getEldersByChildId(Long childId) {
        // 查询该子女绑定的所有关系
        List<UserRelation> relations = this.list(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getChildId, childId)
                .eq(UserRelation::getStatus, RelationStatus.CONFIRMED.getCode()));

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取老人ID列表并批量查询用户信息
        List<Long> elderIds = relations.stream()
                .map(UserRelation::getElderId)
                .collect(Collectors.toList());

        return sysUserService.listByIds(elderIds);
    }

    @Override
    public boolean bindElder(BindDTO bindDTO) {
        // 检查是否已存在绑定关系
        long count = this.count(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getChildId, bindDTO.getChildId())
                .eq(UserRelation::getElderId, bindDTO.getElderId()));

        if (count > 0) {
            throw new RuntimeException("关系已存在");
        }

        // 创建新的绑定关系，默认为已确认状态（直接绑定场景）
        UserRelation relation = new UserRelation();
        relation.setChildId(bindDTO.getChildId());
        relation.setElderId(bindDTO.getElderId());
        relation.setStatus(RelationStatus.CONFIRMED.getCode());
        return this.save(relation);
    }

    @Override
    public List<UserRelation> getMyRelations(Long userId) {
        // 查询用户作为监督者或被监督者的所有关系
        LambdaQueryWrapper<UserRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelation::getChildId, userId)
                .or()
                .eq(UserRelation::getElderId, userId);
        return this.list(wrapper);
    }

    @Override
    public boolean sendInvite(UserRelation relation) {
        // 设置邀请状态为待确认
        relation.setStatus(RelationStatus.PENDING.getCode());
        return this.save(relation);
    }

    @Override
    public boolean acceptInvite(Long relationId) {
        // 查询关系记录
        UserRelation relation = this.getById(relationId);
        if (relation == null) {
            throw new RuntimeException("关系记录不存在");
        }

        // 更新状态为已确认
        relation.setStatus(RelationStatus.CONFIRMED.getCode());
        return this.updateById(relation);
    }
}
