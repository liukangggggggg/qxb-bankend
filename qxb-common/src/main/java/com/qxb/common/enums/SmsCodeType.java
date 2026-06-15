package com.qxb.common.enums;

/**
 * 短信验证码类型
 *
 * @author qxb
 */
public enum SmsCodeType
{
    /** 手机号登录 */
    LOGIN("login"),

    /** 绑定手机号 */
    BIND("bind"),

    /** 注册 */
    REGISTER("register");

    private final String code;

    SmsCodeType(String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }

    public static SmsCodeType fromCode(String code)
    {
        for (SmsCodeType type : values())
        {
            if (type.code.equals(code))
            {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的短信验证码类型: " + code);
    }
}
