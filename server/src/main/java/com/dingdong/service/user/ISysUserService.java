package com.dingdong.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dingdong.dto.auth.LoginDTO;
import com.dingdong.dto.user.RoleSwitchDTO;
import com.dingdong.entity.user.SysUser;

/**
 * 用户业务逻辑接口
 * 
 * @author Antigravity
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 登录或注册
     * 
     * @param loginDTO 登录请求参数 (code, nickname, avatar)
     * @return 登录后的用户信息
     */
    SysUser loginOrRegister(LoginDTO loginDTO);

    /**
     * 切换用户角色
     * 
     * @param roleSwitchDTO 角色切换参数 (userId, role)
     * @return 操作结果
     */
    boolean switchRole(RoleSwitchDTO roleSwitchDTO);
}
