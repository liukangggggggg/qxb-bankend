package com.qxb.framework.web.service;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.qxb.common.constant.CacheConstants;
import com.qxb.common.core.redis.RedisCache;
import com.qxb.common.enums.SmsCodeType;
import com.qxb.common.exception.ServiceException;
import com.qxb.common.utils.MessageUtils;
import com.qxb.common.utils.StringUtils;
import com.qxb.framework.config.properties.SmsProperties;

/**
 * 短信验证码服务
 *
 * @author qxb
 */
@Component
public class SmsCodeService
{
    private static final Logger log = LoggerFactory.getLogger(SmsCodeService.class);

    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private SmsProperties smsProperties;

    /**
     * 发送短信验证码
     */
    public void sendCode(String phone, SmsCodeType type)
    {
        validatePhone(phone);
        if (!smsProperties.isEnabled())
        {
            throw new ServiceException("短信服务未启用");
        }
        String limitKey = CacheConstants.SMS_SEND_LIMIT_KEY + type.getCode() + ":" + phone;
        if (redisCache.hasKey(limitKey))
        {
            throw new ServiceException("发送过于频繁，请稍后再试");
        }
        String code = generateCode();
        String codeKey = buildCodeKey(phone, type);
        redisCache.setCacheObject(codeKey, code, smsProperties.getExpireMinutes(), TimeUnit.MINUTES);
        redisCache.setCacheObject(limitKey, "1", smsProperties.getSendInterval(), TimeUnit.SECONDS);
        if (smsProperties.isDevMode())
        {
            log.info("【开发模式】短信验证码 phone={}, type={}, code={}", phone, type.getCode(), code);
        }
        else
        {
            // TODO: 接入阿里云/腾讯云短信服务
            throw new ServiceException("短信服务未配置，请联系管理员");
        }
    }

    /**
     * 校验短信验证码（校验成功后删除）
     */
    public void verifyCode(String phone, SmsCodeType type, String smsCode)
    {
        validatePhone(phone);
        if (StringUtils.isEmpty(smsCode))
        {
            throw new ServiceException("短信验证码不能为空");
        }
        String codeKey = buildCodeKey(phone, type);
        String cachedCode = redisCache.getCacheObject(codeKey);
        if (cachedCode == null)
        {
            throw new ServiceException("短信验证码已失效，请重新获取");
        }
        if (!smsCode.equals(cachedCode))
        {
            throw new ServiceException("短信验证码错误");
        }
        redisCache.deleteObject(codeKey);
    }

    public void validatePhone(String phone)
    {
        if (StringUtils.isEmpty(phone))
        {
            throw new ServiceException(MessageUtils.message("user.mobile.phone.number.not.valid"));
        }
        if (!phone.matches(PHONE_PATTERN))
        {
            throw new ServiceException(MessageUtils.message("user.mobile.phone.number.not.valid"));
        }
    }

    private String buildCodeKey(String phone, SmsCodeType type)
    {
        return CacheConstants.SMS_CODE_KEY + type.getCode() + ":" + phone;
    }

    private String generateCode()
    {
        int length = smsProperties.getCodeLength();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            code.append((int) (Math.random() * 10));
        }
        return code.toString();
    }
}
