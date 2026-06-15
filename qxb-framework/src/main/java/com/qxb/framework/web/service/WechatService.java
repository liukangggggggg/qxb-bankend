package com.qxb.framework.web.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qxb.common.exception.ServiceException;
import com.qxb.common.utils.StringUtils;
import com.qxb.framework.config.properties.WechatProperties;

/**
 * 微信小程序服务
 *
 * 提供与微信服务器交互的能力，目前支持：
 * - jscode2session：通过临时 code 换取 openid / session_key / unionid
 *
 * @author qxb
 */
@Component
public class WechatService
{
    private static final Logger log = LoggerFactory.getLogger(WechatService.class);

    /** 微信小程序 jscode2session 接口地址 */
    private static final String JSCODE2SESSION_URL =
        "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={jsCode}&grant_type=authorization_code";

    @Autowired
    private WechatProperties wechatProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 通过临时 code 换取微信用户标识
     *
     * @param code 前端 wx.login() 获取的临时凭证
     * @return WechatSession 包含 openid、session_key、unionid
     * @throws ServiceException 当 code 无效、配置缺失或微信接口返回错误时抛出
     */
    public WechatSession jscode2session(String code)
    {
        if (StringUtils.isEmpty(code))
        {
            throw new ServiceException("微信授权码不能为空");
        }
        if (StringUtils.isEmpty(wechatProperties.getAppId()) || StringUtils.isEmpty(wechatProperties.getAppSecret()))
        {
            throw new ServiceException("微信小程序未配置");
        }
        try
        {
            // 调用微信接口，将临时 code 换取 openid 和 session_key
            String response = restTemplate.getForObject(JSCODE2SESSION_URL, String.class,
                wechatProperties.getAppId(), wechatProperties.getAppSecret(), code);
            if (StringUtils.isEmpty(response))
            {
                throw new ServiceException("微信登录失败：请求微信接口返回空");
            }
            // 解析微信返回的 JSON 数据
            Map<String, Object> result = objectMapper.readValue(response,
                new TypeReference<Map<String, Object>>() {});
            // 检查微信接口错误码
            if (result.containsKey("errcode") && !"0".equals(String.valueOf(result.get("errcode"))))
            {
                String errMsg = (String) result.get("errmsg");
                log.error("微信登录失败：errcode={}, errmsg={}", result.get("errcode"), errMsg);
                throw new ServiceException("微信登录失败：" + errMsg);
            }
            String openid = (String) result.get("openid");
            String sessionKey = (String) result.get("session_key");
            String unionid = (String) result.get("unionid");
            if (StringUtils.isEmpty(openid))
            {
                throw new ServiceException("微信登录失败：未获取到用户标识");
            }
            // 封装返回结果
            WechatSession session = new WechatSession();
            session.setOpenid(openid);
            session.setSessionKey(sessionKey);
            session.setUnionid(unionid);
            return session;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("微信登录请求异常", e);
            throw new ServiceException("微信登录失败：" + e.getMessage());
        }
    }

    /**
     * 微信用户会话信息
     */
    public static class WechatSession
    {
        /** 用户唯一标识（每个小程序下每个用户唯一） */
        private String openid;

        /** 会话密钥（用于解密 encryptedData） */
        private String sessionKey;

        /** 用户在微信开放平台下的唯一标识（需绑定开放平台才返回） */
        private String unionid;

        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }

        public String getSessionKey() { return sessionKey; }
        public void setSessionKey(String sessionKey) { this.sessionKey = sessionKey; }

        public String getUnionid() { return unionid; }
        public void setUnionid(String unionid) { this.unionid = unionid; }
    }
}
