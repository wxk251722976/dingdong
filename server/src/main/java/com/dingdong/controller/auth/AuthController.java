package com.dingdong.controller.auth;

import com.dingdong.common.Result;
import com.dingdong.common.util.JwtUtil;
import com.dingdong.dto.auth.LoginDTO;
import com.dingdong.dto.auth.LoginResultDTO;
import com.dingdong.dto.auth.TokenRefreshDTO;
import com.dingdong.entity.user.SysUser;
import com.dingdong.service.user.ISysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证控制器
 * 处理登录、注册及角色切换相关的请求
 * 
 * @author Antigravity
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ISysUserService sysUserService;
    private final JwtUtil jwtUtil;

    /**
     * 微信授权登录接口
     * 
     * @param loginDTO 包含 code, nickname, avatar
     * @return 返回登录成功的用户信息和 Token
     */
    @PostMapping("/login")
    public Result<LoginResultDTO> login(@RequestBody @Valid LoginDTO loginDTO) {
        SysUser user = sysUserService.loginOrRegister(loginDTO);
        String token = jwtUtil.generateToken(user.getId(), user.getNickname());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        return Result.success(new LoginResultDTO(user, token, refreshToken));
    }

    /**
     * 刷新 Token 接口
     */
    @PostMapping("/refresh")
    public Result<Map<String, String>> refreshToken(@RequestBody @Valid TokenRefreshDTO refreshDTO) {
        String refreshToken = refreshDTO.getRefreshToken();

        // 验证 Refresh Token 是否有效且类型正确
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            return Result.error(401, "Invalid Refresh Token");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error(401, "User not found");
        }
        String newToken = jwtUtil.generateToken(userId, user.getNickname());

        // 返回新的 Access Token
        // 可以选择是否同时也旋转 Refresh Token，这里暂只更新 Access Token
        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);

        return Result.success(result);
    }

}
