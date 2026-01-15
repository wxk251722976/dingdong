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
    public List<SysUser> getSupervisedListBySupervisorId(Long supervisorId) {
        // 查询该监督者绑定的所有关系
        List<UserRelation> relations = this.list(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getSupervisorId, supervisorId)
                .eq(UserRelation::getStatus, RelationStatus.ACCEPTED.getCode()));

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取被监督者ID列表并批量查询用户信息
        List<Long> supervisedIds = relations.stream()
                .map(UserRelation::getSupervisedId)
                .collect(Collectors.toList());

        return sysUserService.listByIds(supervisedIds);
    }

    @Override
    public boolean bindSupervised(BindDTO bindDTO) {
        // 检查是否已存在绑定关系
        long count = this.count(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getSupervisorId, bindDTO.getSupervisorId())
                .eq(UserRelation::getSupervisedId, bindDTO.getSupervisedId()));

        if (count > 0) {
            throw new RuntimeException("关系已存在");
        }

        // 创建新的绑定关系，默认为已接受状态（直接绑定场景）
        UserRelation relation = new UserRelation();
        relation.setSupervisorId(bindDTO.getSupervisorId());
        relation.setSupervisedId(bindDTO.getSupervisedId());
        relation.setRelationName(bindDTO.getRelationName());
        relation.setStatus(RelationStatus.ACCEPTED.getCode());
        return this.save(relation);
    }

    @Override
    public List<UserRelation> getMyRelations(Long userId) {
        // 查询用户作为监督者或被监督者的所有关系
        LambdaQueryWrapper<UserRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRelation::getSupervisorId, userId)
                .or()
                .eq(UserRelation::getSupervisedId, userId);
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

        // 更新状态为已接受
        relation.setStatus(RelationStatus.ACCEPTED.getCode());
        return this.updateById(relation);
    }

    @Override
    public boolean rejectInvite(Long relationId) {
        // 查询关系记录
        UserRelation relation = this.getById(relationId);
        if (relation == null) {
            throw new RuntimeException("关系记录不存在");
        }

        // 更新状态为已拒绝
        relation.setStatus(RelationStatus.REJECTED.getCode());
        return this.updateById(relation);
    }
}
