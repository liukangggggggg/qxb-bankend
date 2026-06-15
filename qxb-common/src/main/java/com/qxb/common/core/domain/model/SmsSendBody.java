package com.qxb.common.core.domain.model;

import lombok.Data;

/**
 * 发送短信验证码对象
 *
 * @author qxb
 */
@Data
public class SmsSendBody
{
    /** 手机号码 */
    private String phone;

    /** 验证码类型：login-登录 bind-绑定 */
    private String type;


}
