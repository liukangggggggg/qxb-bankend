package com.qxb.common.core.domain.model;

import lombok.Data;

/**
 * 手机号注册请求对象
 * 
 * 支持账号密码+手机验证码注册方式。
 * 手机号和验证码为必填，用户名和密码可选：
 * - 仅手机号+验证码：只创建手机号认证记录
 * - 手机号+验证码+用户名+密码：同时创建手机号和密码认证记录
 *
 * @author qxb
 */
@Data
public class PhoneRegisterBody
{
    /** 手机号码 */
    private String phone;

    /** 短信验证码 */
    private String smsCode;

    /** 用户名（选填，用于创建密码认证） */
    private String username;

    /** 登录密码（选填，与用户名一起创建密码认证） */
    private String password;

}
