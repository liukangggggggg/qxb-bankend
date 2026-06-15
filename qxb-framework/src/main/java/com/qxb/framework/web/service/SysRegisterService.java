package com.qxb.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.qxb.common.constant.CacheConstants;
import com.qxb.common.constant.Constants;
import com.qxb.common.constant.UserConstants;
import com.qxb.common.core.domain.entity.SysUser;
import com.qxb.common.core.domain.entity.SysUserAuth;
import com.qxb.common.core.domain.model.RegisterBody;
import com.qxb.common.core.redis.RedisCache;
import com.qxb.common.exception.user.CaptchaException;
import com.qxb.common.exception.user.CaptchaExpireException;
import com.qxb.common.utils.MessageUtils;
import com.qxb.common.utils.SecurityUtils;
import com.qxb.common.utils.StringUtils;
import com.qxb.framework.manager.AsyncManager;
import com.qxb.framework.manager.factory.AsyncFactory;
import com.qxb.common.core.domain.model.PhoneRegisterBody;
import com.qxb.common.enums.SmsCodeType;
import com.qxb.common.exception.ServiceException;
import com.qxb.system.mapper.SysUserAuthMapper;
import com.qxb.system.service.ISysConfigService;
import com.qxb.system.service.ISysUserService;

import java.util.Random;

@Component
public class SysRegisterService
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private SysUserAuthMapper authMapper;

    @Autowired
    private SmsCodeService smsCodeService;

    @Transactional
    public String register(RegisterBody registerBody)
    {
        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();

        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }

        if (StringUtils.isEmpty(username))
        {
            msg = "用户名不能为空";
        }
        else if (StringUtils.isEmpty(password))
        {
            msg = "用户密码不能为空";
        }
        else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            msg = "账户长度必须在2到20个字符之间";
        }
        else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            msg = "密码长度必须在5到20个字符之间";
        }
        else if (authMapper.selectAuthByIdentifier("password", username) != null)
        {
            msg = "用户'" + username + "'已存在，注册失败";
        }
        else
        {
            SysUser user = new SysUser();
            user.setNickName(generateRandomPsychologyNickname());
            userService.registerUser(user);

            SysUserAuth auth = new SysUserAuth();
            auth.setUserId(user.getUserId());
            auth.setIdentityType("password");
            auth.setIdentifier(username);
            auth.setCredential(SecurityUtils.encryptPassword(password));
            authMapper.insertUserAuth(auth);

            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.register.success")));
        }
        return msg;
    }

    @Transactional
    public String phoneRegister(PhoneRegisterBody body)
    {
        String phone = body.getPhone();
        String smsCode = body.getSmsCode();
        String username = body.getUsername();
        String password = body.getPassword();

        if (StringUtils.isEmpty(phone))
        {
            return "手机号不能为空";
        }
        if (StringUtils.isEmpty(smsCode))
        {
            return "短信验证码不能为空";
        }
        try
        {
            smsCodeService.verifyCode(phone, SmsCodeType.REGISTER, smsCode);
        }
        catch (ServiceException e)
        {
            return e.getMessage();
        }

        if (authMapper.selectAuthByIdentifier("phone", phone) != null)
        {
            return "该手机号已被注册";
        }
        if (StringUtils.isNotEmpty(username) && authMapper.selectAuthByIdentifier("password", username) != null)
        {
            return "用户名已被使用";
        }
        if (StringUtils.isNotEmpty(password) && (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH))
        {
            return "密码长度必须在5到20个字符之间";
        }

        SysUser user = new SysUser();
        user.setNickName(generateRandomPsychologyNickname());
        userService.registerUser(user);

        SysUserAuth phoneAuth = new SysUserAuth();
        phoneAuth.setUserId(user.getUserId());
        phoneAuth.setIdentityType("phone");
        phoneAuth.setIdentifier(phone);
        authMapper.insertUserAuth(phoneAuth);

        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password))
        {
            SysUserAuth pwdAuth = new SysUserAuth();
            pwdAuth.setUserId(user.getUserId());
            pwdAuth.setIdentityType("password");
            pwdAuth.setIdentifier(username);
            pwdAuth.setCredential(SecurityUtils.encryptPassword(password));
            authMapper.insertUserAuth(pwdAuth);
        }

        AsyncManager.me().execute(AsyncFactory.recordLogininfor(phone, Constants.REGISTER, "手机号注册成功"));
        return "";
    }

    public void validateCaptcha(String username, String code, String uuid)
    {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null)
        {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new CaptchaException();
        }
    }
    
    /**
     * 生成心理咨询相关的随机昵称
     * 
     * @return 随机昵称
     */
    private String generateRandomPsychologyNickname() {
        String[] prefixes = {
            "心灵", "心理", "情感", "治愈", "倾听", "阳光", "温暖", "微笑", "宁静", "和谐",
            "成长", "探索", "智慧", "平衡", "自由", "理解", "接纳", "关怀", "平和", "勇气"
        };
        
        String[] suffixes = {
            "使者", "朋友", "导师", "守护", "向导", "驿站", "港湾", "花园", "桥梁", "明灯",
            "伙伴", "知音", "知己", "同行", "天使", "精灵", "使者", "顾问", "专家", "达人"
        };
        
        Random random = new Random();
        String prefix = prefixes[random.nextInt(prefixes.length)];
        String suffix = suffixes[random.nextInt(suffixes.length)];
        
        // 添加数字后缀以增加唯一性
        int numberSuffix = 1000 + random.nextInt(9000);
        
        return prefix + suffix + numberSuffix;
    }
}
