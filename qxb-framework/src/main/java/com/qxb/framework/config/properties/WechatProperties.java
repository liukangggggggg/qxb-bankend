package com.qxb.framework.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置
 *
 * 从 application.yml 的 wechat 前缀读取配置：
 * <pre>
 * wechat:
 *   app-id: xxx
 *   app-secret: xxx
 * </pre>
 *
 * @author qxb
 */
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties
{
    /** 微信小程序 AppID */
    private String appId;

    /** 微信小程序 AppSecret */
    private String appSecret;

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
}
