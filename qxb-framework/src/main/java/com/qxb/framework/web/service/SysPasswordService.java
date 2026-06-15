package com.qxb.framework.web.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.qxb.common.constant.CacheConstants;
import com.qxb.common.core.domain.entity.SysUserAuth;
import com.qxb.common.core.redis.RedisCache;
import com.qxb.common.exception.user.UserPasswordNotMatchException;
import com.qxb.common.exception.user.UserPasswordRetryLimitExceedException;
import com.qxb.common.utils.SecurityUtils;
import com.qxb.framework.security.context.AuthenticationContextHolder;

@Component
public class SysPasswordService
{
    @Autowired
    private RedisCache redisCache;

    @Value(value = "${user.password.maxRetryCount}")
    private int maxRetryCount;

    @Value(value = "${user.password.lockTime}")
    private int lockTime;

    private String getCacheKey(String username)
    {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }

    public void validate(SysUserAuth auth)
    {
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        //获取用户输入密码错误次数
        Integer retryCount = redisCache.getCacheObject(getCacheKey(username));

        if (retryCount == null) retryCount = 0;

        if (retryCount >= Integer.valueOf(maxRetryCount).intValue())
        {
            throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
        }

        if (!matches(auth, password))
        {
            retryCount = retryCount + 1;
            redisCache.setCacheObject(getCacheKey(username), retryCount, lockTime, TimeUnit.MINUTES);
            throw new UserPasswordNotMatchException();
        }
        else
        {
            clearLoginRecordCache(username);
        }
    }

    public boolean matches(SysUserAuth auth, String rawPassword)
    {
        return SecurityUtils.matchesPassword(rawPassword, auth.getCredential());
    }

    public void clearLoginRecordCache(String loginName)
    {
        if (redisCache.hasKey(getCacheKey(loginName)))
        {
            redisCache.deleteObject(getCacheKey(loginName));
        }
    }
}
