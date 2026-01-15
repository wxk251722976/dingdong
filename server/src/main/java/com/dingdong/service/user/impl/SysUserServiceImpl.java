package com.dingdong.service.user.impl;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.common.constant.UserConstants;
import com.dingdong.common.exception.ServiceException;
import com.dingdong.dto.auth.LoginDTO;
import com.dingdong.entity.user.SysUser;
import com.dingdong.mapper.user.SysUserMapper;
import com.dingdong.service.user.ISysUserService;
import com.dingdong.service.wechat.WechatApiService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户业务逻辑实现类
 * 使用 WxJava SDK 实现微信登录
 * 
 * @author Antigravity
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final WechatApiService wechatApiService;
    private final com.dingdong.service.user.IUserLevelConfigService levelConfigService;

    public SysUserServiceImpl(
            WechatApiService wechatApiService,
            @Lazy com.dingdong.service.user.IUserLevelConfigService levelConfigService) {
        this.wechatApiService = wechatApiService;
        this.levelConfigService = levelConfigService;
    }

    @Override
    public SysUser loginOrRegister(LoginDTO loginDTO) {
        String code = loginDTO.getCode();

        // 1. 使用 WxJava SDK 获取 OpenID
        String openid;
        try {
            WxMaJscode2SessionResult session = wechatApiService.getSessionInfo(code);
            openid = session.getOpenid();
            log.info("微信登录成功: openid={}", openid);
        } catch (WxErrorException e) {
            log.error("微信登录失败: {}", e.getMessage());
            throw new ServiceException("微信登录失败: " + e.getMessage());
        }

        if (!StringUtils.hasText(openid)) {
            throw new ServiceException("微信登录失败：无法获取用户信息");
        }

        // 2. 查询数据库是否存在该用户
        SysUser user = this.lambdaQuery().eq(SysUser::getOpenid, openid).one();

        if (user == null) {
            // 3. 不存在则注册新用户
            user = new SysUser();
            user.setOpenid(openid);
            user.setNickname(loginDTO.getNickname());
            user.setAvatar(loginDTO.getAvatar());
            user.setCreateTime(LocalDateTime.now());
            user.setLevelCode(UserConstants.DEFAULT_LEVEL_CODE);
            // 默认无角色，需选择
            this.save(user);
        } else {
            // 4. 存在则更新信息
            boolean needUpdate = false;
            if (StringUtils.hasText(loginDTO.getNickname())) {
                user.setNickname(loginDTO.getNickname());
                needUpdate = true;
            }
            if (StringUtils.hasText(loginDTO.getAvatar())) {
                user.setAvatar(loginDTO.getAvatar());
                needUpdate = true;
            }
            if (needUpdate) {
                this.updateById(user);
            }
        }

        // 填充等级名称
        var config = levelConfigService.getUserLevelConfig(user.getId());
        if (config != null) {
            user.setLevelName(config.getLevelName());
        }

        return user;
    }
}
