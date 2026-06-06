package com.qxb.framework.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信配置
 *
 * @author qxb
 */
@Component
@ConfigurationProperties(prefix = "sms")
public class SmsProperties
{
    /** 是否启用短信功能 */
    private boolean enabled = true;

    /** 开发模式：验证码输出到日志，不实际发送 */
    private boolean devMode = true;

    /** 验证码有效期（分钟） */
    private int expireMinutes = 5;

    /** 验证码长度 */
    private int codeLength = 6;

    /** 发送间隔（秒） */
    private int sendInterval = 60;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isDevMode()
    {
        return devMode;
    }

    public void setDevMode(boolean devMode)
    {
        this.devMode = devMode;
    }

    public int getExpireMinutes()
    {
        return expireMinutes;
    }

    public void setExpireMinutes(int expireMinutes)
    {
        this.expireMinutes = expireMinutes;
    }

    public int getCodeLength()
    {
        return codeLength;
    }

    public void setCodeLength(int codeLength)
    {
        this.codeLength = codeLength;
    }

    public int getSendInterval()
    {
        return sendInterval;
    }

    public void setSendInterval(int sendInterval)
    {
        this.sendInterval = sendInterval;
    }
}
