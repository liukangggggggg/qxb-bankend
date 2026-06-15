package com.qxb.framework.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.qxb.common.core.domain.entity.SysUser;
import com.qxb.common.core.domain.entity.SysUserAuth;
import com.qxb.common.core.domain.model.LoginUser;
import com.qxb.common.enums.UserStatus;
import com.qxb.common.exception.ServiceException;
import com.qxb.common.utils.MessageUtils;
import com.qxb.common.utils.StringUtils;
import com.qxb.system.mapper.SysUserAuthMapper;
import com.qxb.system.service.ISysUserService;

@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysUserAuthMapper authMapper;

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private SysPermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        SysUserAuth auth = authMapper.selectAuthByIdentifier("password", username);
        if (StringUtils.isNull(auth))
        {
            log.info("登录用户：{} 登录凭证不存在.", username);
            throw new ServiceException(MessageUtils.message("user.not.exists"));
        }

        SysUser user = userService.selectUserById(auth.getUserId());
        if (StringUtils.isNull(user))
        {
            log.info("登录用户：{} 关联用户不存在.", username);
            throw new ServiceException(MessageUtils.message("user.not.exists"));
        }
        else if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException(MessageUtils.message("user.password.delete"));
        }
        else if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException(MessageUtils.message("user.blocked"));
        }

        passwordService.validate(auth);

        return createLoginUser(user, auth);
    }

    public UserDetails createLoginUser(SysUser user, SysUserAuth auth)
    {
        return new LoginUser(user.getUserId(), user.getDeptId(), user, auth, permissionService.getMenuPermission(user));
    }
}
