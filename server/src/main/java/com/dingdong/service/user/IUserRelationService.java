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
}
