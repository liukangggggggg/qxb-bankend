package com.qxb.common.core.domain.model;

/**
 * 发送短信验证码对象
 *
 * @author qxb
 */
public class SmsSendBody
{
    /** 手机号码 */
    private String phone;

    /** 验证码类型：login-登录 bind-绑定 */
    private String type;

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
