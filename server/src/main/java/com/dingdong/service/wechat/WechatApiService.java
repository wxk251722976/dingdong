package com.dingdong.service.wechat;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信API服务
 * 提供微信小程序相关API调用，包括access_token管理和动态消息
 */
@Slf4j
@Service
public class WechatApiService {

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.secret}")
    private String secret;

    // access_token缓存
    private String cachedAccessToken;
    private long tokenExpireTime = 0;

    // 动态消息活动ID缓存 (relationId -> activityId)
    private final Map<Long, String> activityIdCache = new ConcurrentHashMap<>();

    /**
     * 获取access_token（带缓存）
     */
    public String getAccessToken() {
        // 检查缓存是否有效（提前5分钟刷新）
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireTime - 300000) {
            return cachedAccessToken;
        }

        String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appId, secret);

        String result = HttpUtil.get(url);
        log.info("获取access_token响应: {}", result);

        JSONObject json = JSONUtil.parseObj(result);
        if (json.containsKey("access_token")) {
            cachedAccessToken = json.getStr("access_token");
            int expiresIn = json.getInt("expires_in", 7200);
            tokenExpireTime = System.currentTimeMillis() + expiresIn * 1000L;
            return cachedAccessToken;
        }

        log.error("获取access_token失败: {}", result);
        throw new RuntimeException("获取access_token失败: " + json.getStr("errmsg"));
    }

    /**
     * 创建动态消息活动ID
     * 用于转发动态消息时声明消息类型
     * 
     * @param unionId 可选，关联的unionId
     * @return activityId
     */
    public String createActivityId(String unionId) {
        String accessToken = getAccessToken();
        String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/message/wxopen/activityid/create?access_token=%s",
                accessToken);

        Map<String, Object> params = new HashMap<>();
        if (unionId != null && !unionId.isEmpty()) {
            params.put("unionid", unionId);
        }

        String result = HttpUtil.post(url, JSONUtil.toJsonStr(params));
        log.info("创建activityId响应: {}", result);

        JSONObject json = JSONUtil.parseObj(result);
        if (json.getInt("errcode", 0) == 0) {
            return json.getStr("activity_id");
        }

        log.error("创建activityId失败: {}", result);
        throw new RuntimeException("创建activityId失败: " + json.getStr("errmsg"));
    }

    /**
     * 更新动态消息状态
     * 
     * @param activityId   活动ID
     * @param targetState  目标状态: 0-未开始, 1-已开始(待绑定), 2-已结束(已绑定/已拒绝)
     * @param templateInfo 模板信息
     */
    public void setUpdatableMsg(String activityId, int targetState, Map<String, Object> templateInfo) {
        String accessToken = getAccessToken();
        String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/message/wxopen/updatablemsg/send?access_token=%s",
                accessToken);

        Map<String, Object> params = new HashMap<>();
        params.put("activity_id", activityId);
        params.put("target_state", targetState);
        params.put("template_info", templateInfo);

        String result = HttpUtil.post(url, JSONUtil.toJsonStr(params));
        log.info("更新动态消息响应: {}", result);

        JSONObject json = JSONUtil.parseObj(result);
        if (json.getInt("errcode", 0) != 0) {
            log.error("更新动态消息失败: {}", result);
        }
    }

    /**
     * 缓存关系ID与活动ID的映射
     */
    public void cacheActivityId(Long relationId, String activityId) {
        activityIdCache.put(relationId, activityId);
    }

    /**
     * 获取缓存的活动ID
     */
    public String getCachedActivityId(Long relationId) {
        return activityIdCache.get(relationId);
    }

    /**
     * 移除缓存的活动ID
     */
    public void removeCachedActivityId(Long relationId) {
        activityIdCache.remove(relationId);
    }
}
