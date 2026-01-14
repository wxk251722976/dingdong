package com.dingdong.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dingdong.dto.user.BindDTO;
import com.dingdong.entity.user.SysUser;
import com.dingdong.entity.user.UserRelation;

import java.util.List;

/**
 * 用户关系业务接口
 * 
 * @author Antigravity
 */
public interface IUserRelationService extends IService<UserRelation> {

    /**
     * 获取指定子女绑定的所有老人
     * 
     * @param childId 子女ID
     * @return 老人用户列表
     */
    List<SysUser> getEldersByChildId(Long childId);

    /**
     * 绑定老人与子女关系
     * 
     * @param bindDTO 绑定请求
     * @return 成功返回 true
     */
    boolean bindElder(BindDTO bindDTO);

    /**
     * 获取用户的所有关系（包括我监督的和监督我的）
     * 
     * @param userId 用户ID
     * @return 关系列表
     */
    List<UserRelation> getMyRelations(Long userId);

    /**
     * 发送绑定邀请
     * 
     * @param relation 关系实体（包含elderId, childId, relationName）
     * @return 是否成功
     */
    boolean sendInvite(UserRelation relation);

    /**
     * 接受绑定邀请
     * 
     * @param relationId 关系记录ID
     * @return 是否成功
     */
    boolean acceptInvite(Long relationId);
}
