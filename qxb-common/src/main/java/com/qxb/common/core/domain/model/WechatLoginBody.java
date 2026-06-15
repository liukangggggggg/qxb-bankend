package com.qxb.common.core.domain.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 微信小程序登录请求对象
 *
 * 前端通过 wx.login() 获取临时 code，
 * 后端通过 code 换取 openid 完成登录/注册。
 *
 * @author qxb
 */
@Setter
@Getter
public class WechatLoginBody
{
    /** 微信登录临时凭证（wx.login 获取） */
    private String code;

    /** 加密数据（可选，用于获取用户手机号等敏感信息） */
    private String encryptedData;

    /** 加密算法的初始向量 */
    private String iv;

    /** 原始数据字符串 */
    private String rawData;

    /** 数据签名 */
    private String signature;

}
