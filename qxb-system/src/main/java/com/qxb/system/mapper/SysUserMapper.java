package com.qxb.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.qxb.common.core.domain.entity.SysUser;

public interface SysUserMapper
{
    public List<SysUser> selectUserList(SysUser sysUser);

    public List<SysUser> selectAllocatedList(SysUser user);

    public List<SysUser> selectUnallocatedList(SysUser user);

    public SysUser selectUserById(Long userId);

    public int insertUser(SysUser user);

    public int updateUser(SysUser user);

    public int updateUserAvatar(@Param("userId") Long userId, @Param("avatar") String avatar);

    public int updateUserStatus(@Param("userId") Long userId, @Param("status") String status);

    public int deleteUserById(Long userId);

    public int deleteUserByIds(Long[] userIds);
}
