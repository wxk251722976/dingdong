package com.dingdong.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dingdong.dto.user.BindDTO;
import com.dingdong.dto.user.RelationDisplayDTO;
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
     * 获取指定监督者绑定的所有被监督者
     * 
     * @param supervisorId 监督者ID
     * @return 被监督者用户列表
     */
    List<SysUser> getSupervisedListBySupervisorId(Long supervisorId);

    /**
     * 绑定被监督者与监督者关系
     * 
     * @param bindDTO 绑定请求
     * @return 成功返回 true
     */
    boolean bindSupervised(BindDTO bindDTO);

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

    /**
     * 拒绝绑定邀请
     *
     * @param relationId 关系记录ID
     * @return 是否成功
     */
    boolean rejectInvite(Long relationId);

    /**
     * 获取用户的关系列表（带用户信息）
     * 
     * @param userId 用户ID
     * @return 关系展示列表
     */
    List<RelationDisplayDTO> getRelationsWithUserInfo(Long userId);

    /**
     * 发起解除绑定
     *
     * @param relationId 关系ID
     * @param reason     解绑原因
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean initiateUnbind(Long relationId, String reason, Long operatorId);

    /**
     * 撤回解除绑定
     *
     * @param relationId 关系ID
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean withdrawUnbind(Long relationId, Long operatorId);

    /**
     * 检查两个用户是否存在有效绑定关系
     * 
     * @param userId      用户A
     * @param otherUserId 用户B
     * @return 是否存在
     */
    boolean checkRelation(Long userId, Long otherUserId);
}
