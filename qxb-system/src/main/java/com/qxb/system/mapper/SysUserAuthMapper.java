package com.qxb.system.mapper;

import java.util.Date;
import org.apache.ibatis.annotations.Param;
import com.qxb.common.core.domain.entity.SysUserAuth;

public interface SysUserAuthMapper
{
    public SysUserAuth selectAuthByIdentifier(@Param("identityType") String identityType, @Param("identifier") String identifier);

    public SysUserAuth selectAuthByUserId(@Param("userId") Long userId);

    public int insertUserAuth(SysUserAuth auth);

    public int updateLoginInfo(@Param("authId") Long authId, @Param("loginIp") String loginIp, @Param("loginDate") Date loginDate);

    public int updateCredential(@Param("authId") Long authId, @Param("credential") String credential);

    public int deleteAuthByUserId(Long userId);
}
