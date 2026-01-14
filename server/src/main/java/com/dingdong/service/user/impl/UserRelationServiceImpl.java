package com.dingdong.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.dto.user.BindDTO;
import com.dingdong.entity.user.SysUser;
import com.dingdong.entity.user.UserRelation;
import com.dingdong.mapper.user.UserRelationMapper;
import com.dingdong.service.user.ISysUserService;
import com.dingdong.service.user.IUserRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户关系业务实现
 * 
 * @author Antigravity
 */
@Service
public class UserRelationServiceImpl extends ServiceImpl<UserRelationMapper, UserRelation>
        implements IUserRelationService {

    @Autowired
    @Lazy // 避免循环依赖，如果有的话
    private ISysUserService sysUserService;

    @Override
    public List<SysUser> getEldersByChildId(Long childId) {
        List<UserRelation> relations = this.list(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getChildId, childId));

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> elderIds = relations.stream()
                .map(UserRelation::getElderId)
                .collect(Collectors.toList());

        return sysUserService.listByIds(elderIds);
    }

    @Override
    public boolean bindElder(BindDTO bindDTO) {
        // 简单查重
        long count = this.count(new LambdaQueryWrapper<UserRelation>()
                .eq(UserRelation::getChildId, bindDTO.getChildId())
                .eq(UserRelation::getElderId, bindDTO.getElderId()));

        if (count > 0) {
            throw new RuntimeException("关系已存在");
        }

        UserRelation relation = new UserRelation();
        relation.setChildId(bindDTO.getChildId());
        relation.setElderId(bindDTO.getElderId());
        relation.setCreateTime(LocalDateTime.now());
        return this.save(relation);
    }
}
