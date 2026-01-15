package com.dingdong.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信小程序配置类
 * 使用 WxJava SDK 实现微信小程序相关功能
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wx.miniapp")
public class WxMaConfig {

    /** 小程序 appId */
    private String appid;

    /** 小程序 secret */
    private String secret;

    /** 订阅消息模板配置 */
    private SubscribeMessage subscribeMessage;

    @Data
    public static class SubscribeMessage {
        /** 叮咚提醒模板ID */
        private String remindCheckin;
        /** 打卡完成通知模板ID */
        private String checkinComplete;
        /** 漏打卡通知模板ID */
        private String missedCheckin;
    }

    /**
     * 配置 WxMaService Bean
     */
    @Bean
    public WxMaService wxMaService() {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(this.appid);
        config.setSecret(this.secret);

        WxMaService service = new WxMaServiceImpl();
        service.setWxMaConfig(config);
        return service;
    }
}
