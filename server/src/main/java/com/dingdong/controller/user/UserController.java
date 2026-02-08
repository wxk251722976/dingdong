package com.dingdong.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.dingdong.common.ErrorCode;
import com.dingdong.common.Result;
import com.dingdong.common.context.SystemContextHolder;
import com.dingdong.dto.user.UserProfileDTO;
import com.dingdong.entity.user.SysUser;
import com.dingdong.service.user.ISysUserService;
import com.dingdong.vo.user.UserVO;
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
    /**
     * 更新用户资料
     */
    @PostMapping("/updateProfile")
    public Result<Void> updateProfile(@RequestBody @jakarta.validation.Valid UserProfileDTO userDto) {
        Long userId = SystemContextHolder.getUserId();

        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error(ErrorCode.USER_NOT_EXIST);
        }

        user.setNickname(userDto.getNickname());
        user.setAvatar(userDto.getAvatar());
        sysUserService.updateById(user);

        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        Long userId = SystemContextHolder.getUserId();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(BeanUtil.toBean(user, UserVO.class));
    }

    /**
     * 根据用户ID获取用户基本信息（公开接口）
     * 用于绑定确认页面获取邀请者信息
     */
    @GetMapping("/info/{userId}")
    public Result<UserVO> getUserInfoById(@PathVariable Long userId) {
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(BeanUtil.toBean(user, UserVO.class));
    }
}
