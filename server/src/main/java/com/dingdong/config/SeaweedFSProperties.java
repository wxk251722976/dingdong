package com.dingdong.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SeaweedFS 配置属性
 * 
 * @author Antigravity
 */
@Data
@Component
@ConfigurationProperties(prefix = "seaweedfs")
public class SeaweedFSProperties {

    /** Master 服务地址 */
    private String masterUrl = "http://localhost:9333";

    /** Volume 服务地址 */
    private String volumeUrl = "http://localhost:8082";

    /** 公网访问地址（用于返回给客户端） */
    private String publicUrl = "http://localhost:8082";
}
