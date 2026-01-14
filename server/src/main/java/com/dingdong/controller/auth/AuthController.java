package com.dingdong.controller.auth;

import com.dingdong.common.Result;
import com.dingdong.dto.auth.LoginDTO;
import com.dingdong.dto.user.RoleSwitchDTO;
import com.dingdong.entity.user.SysUser;
import com.dingdong.service.user.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * 用户认证控制器
 * 处理登录、注册及角色切换相关的请求
 * 
 * @author Antigravity
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 微信授权登录接口
     * 
     * @param loginDTO 包含 code, nickname, avatar
     * @return 返回登录成功的用户信息
     */
    @PostMapping("/login")
    public Result<SysUser> login(@RequestBody @Valid LoginDTO loginDTO) {
        SysUser user = sysUserService.loginOrRegister(loginDTO);
        return Result.success(user);
    }

    /**
     * 切换用户角色接口
     * 
     * @param roleSwitchDTO 包含 userId (用户ID) 和 role (目标角色: ELDER/CHILD)
     * @return 操作结果 true/false
     */
    @PostMapping("/switchRole")
    public Result<Boolean> switchRole(@RequestBody @Valid RoleSwitchDTO roleSwitchDTO) {
        boolean success = sysUserService.switchRole(roleSwitchDTO);
        return success ? Result.success(true) : Result.error("切换失败");
    }
}
