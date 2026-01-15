package com.dingdong.service.user.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingdong.common.constant.UserConstants;
import com.dingdong.common.exception.ServiceException;
import com.dingdong.dto.auth.LoginDTO;
import com.dingdong.entity.user.SysUser;
import com.dingdong.mapper.user.SysUserMapper;
import com.dingdong.service.user.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户业务逻辑实现类
 * 
 * @author Antigravity
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.secret}")
    private String secret;

    @Value("${wechat.api-url}")
    private String apiUrl;

    private final com.dingdong.service.user.IUserLevelConfigService levelConfigService;

    public SysUserServiceImpl(
            @org.springframework.context.annotation.Lazy com.dingdong.service.user.IUserLevelConfigService levelConfigService) {
        this.levelConfigService = levelConfigService;
    }

    @Override
    public SysUser loginOrRegister(LoginDTO loginDTO) {
        String code = loginDTO.getCode();
        // 1. 调用微信接口获取 OpenID
        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                apiUrl, appId, secret, code);
        String result = HttpUtil.get(url);
        log.info("微信登录返回结果: {}", result);

        JSONObject json = JSONUtil.parseObj(result);
        String openid = json.getStr("openid");

        if (!StringUtils.hasText(openid)) {
            log.error("微信登录失败，响应内容: {}", result);
            throw new ServiceException("微信登录失败");
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
