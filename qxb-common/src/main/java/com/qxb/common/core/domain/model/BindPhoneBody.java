package com.qxb.common.core.domain.model;

/**
 * 绑定手机号对象
 *
 * @author qxb
 */
public class BindPhoneBody
{
    /** 手机号码 */
    private String phone;

    /** 短信验证码 */
    private String smsCode;

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getSmsCode()
    {
        return smsCode;
    }

    public void setSmsCode(String smsCode)
    {
        this.smsCode = smsCode;
    }
}
