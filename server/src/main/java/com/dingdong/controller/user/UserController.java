package com.dingdong.controller.user;

import com.dingdong.common.Result;
import com.dingdong.dto.user.UserProfileDTO;
import com.dingdong.entity.user.SysUser;
import com.dingdong.service.user.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final ISysUserService sysUserService;

    /**
     * 更新用户资料
     */
    @PostMapping("/updateProfile")
    public Result<Void> updateProfile(@RequestBody UserProfileDTO userDto) {
        Long userId = userDto.getUserId();

        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setNickname(userDto.getNickname());
        user.setAvatar(userDto.getAvatar());
        sysUserService.updateById(user);

        return Result.success(null);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result<SysUser> getUserInfo(@RequestParam Long userId) {
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }
}
