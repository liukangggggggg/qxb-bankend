package com.qxb.framework.web.service;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.qxb.common.constant.CacheConstants;
import com.qxb.common.constant.Constants;
import com.qxb.common.constant.UserConstants;
import com.qxb.common.core.domain.entity.SysUser;
import com.qxb.common.core.domain.entity.SysUserAuth;
import com.qxb.common.core.domain.model.LoginUser;
import com.qxb.common.core.redis.RedisCache;
import com.qxb.common.enums.SmsCodeType;
import com.qxb.common.enums.UserStatus;
import com.qxb.common.exception.ServiceException;
import com.qxb.common.exception.user.BlackListException;
import com.qxb.common.exception.user.CaptchaException;
import com.qxb.common.exception.user.CaptchaExpireException;
import com.qxb.common.exception.user.UserNotExistsException;
import com.qxb.common.exception.user.UserPasswordNotMatchException;
import com.qxb.common.utils.DateUtils;
import com.qxb.common.utils.MessageUtils;
import com.qxb.common.utils.StringUtils;
import com.qxb.common.utils.ip.IpUtils;
import com.qxb.framework.manager.AsyncManager;
import com.qxb.framework.manager.factory.AsyncFactory;
import com.qxb.framework.security.context.AuthenticationContextHolder;
import com.qxb.system.mapper.SysUserAuthMapper;
import com.qxb.system.service.ISysConfigService;
import com.qxb.system.service.ISysUserService;

import static com.qxb.framework.datasource.DynamicDataSourceContextHolder.log;

/**
 * 登录校验方法
 * 
 * @author ruoyi
 */
@Component
public class SysLoginService
{
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private SmsCodeService smsCodeService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private SysUserAuthMapper authMapper;
    /**
     * 登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid)
    {
        // 验证码校验
        validateCaptcha(username, code, uuid);
        // 登录前置校验
        loginPreCheck(username, password);
        // 用户验证
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 通过Spring Security的认证管理器执行身份验证
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(username);
        if (loginUser != null) {
            return tokenService.createToken(loginUser);
        }
        return username;
    }

    /**
     * 手机号验证码登录
     *
     * @param phone 手机号码
     * @param smsCode 短信验证码
     * @return token
     */
    public String phoneLogin(String phone, String smsCode)
    {
        smsCodeService.validatePhone(phone);
        smsCodeService.verifyCode(phone, SmsCodeType.LOGIN, smsCode);

        SysUserAuth auth = authMapper.selectAuthByIdentifier("phone", phone);
        if (StringUtils.isNull(auth))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(phone, Constants.LOGIN_FAIL, MessageUtils.message("user.phone.not.bound")));
            throw new ServiceException(MessageUtils.message("user.phone.not.bound"));
        }

        SysUser user = userService.selectUserById(auth.getUserId());
        if (StringUtils.isNull(user))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(phone, Constants.LOGIN_FAIL, MessageUtils.message("user.not.exists")));
            throw new ServiceException(MessageUtils.message("user.not.exists"));
        }
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(phone, Constants.LOGIN_FAIL, MessageUtils.message("user.password.delete")));
            throw new ServiceException(MessageUtils.message("user.password.delete"));
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(phone, Constants.LOGIN_FAIL, MessageUtils.message("user.blocked")));
            throw new ServiceException(MessageUtils.message("user.blocked"));
        }
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(phone, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(phone, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) userDetailsService.createLoginUser(user, auth);
        recordLoginInfo(phone);
        return tokenService.createToken(loginUser);
    }

    /**
     * 校验验证码
     * 
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
            String captcha = redisCache.getCacheObject(verifyKey);
            if (captcha == null)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
                throw new CaptchaExpireException();
            }
            redisCache.deleteObject(verifyKey);
            if (!code.equalsIgnoreCase(captcha))
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw new CaptchaException();
            }
        }
    }

    /**
     * 登录前置校验
     * @param username 用户名
     * @param password 用户密码
     */
    public void loginPreCheck(String username, String password)
    {
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // IP黑名单校验
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param identifier 用户唯一标识
     */
    public void recordLoginInfo(String identifier)
    {
        SysUserAuth auth = authMapper.selectAuthByIdentifier("password", identifier);
        if (auth != null)
        {
            // 更新登录信息
            authMapper.updateLoginInfo(auth.getAuthId(), IpUtils.getIpAddr(), DateUtils.getNowDate());
        } else {
            // 记录日志，因为正常情况下不应出现找不到认证信息的情况
            log.warn("无法找到用户 {} 的认证信息，无法更新登录信息", identifier);
        }
    }
}
