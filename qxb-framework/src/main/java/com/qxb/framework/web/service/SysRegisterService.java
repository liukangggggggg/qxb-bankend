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
import com.qxb.system.mapper.SysUserAuthMapper;
import com.qxb.system.service.ISysConfigService;
import com.qxb.system.service.ISysUserService;

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
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        }
        else
        {
            SysUser user = new SysUser();
            user.setNickName(username);
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
}
