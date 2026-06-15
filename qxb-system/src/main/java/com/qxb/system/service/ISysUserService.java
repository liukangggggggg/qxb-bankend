package com.qxb.system.service;

import java.util.List;
import com.qxb.common.core.domain.entity.SysUser;

public interface ISysUserService
{
    public List<SysUser> selectUserList(SysUser user);

    public List<SysUser> selectAllocatedList(SysUser user);

    public List<SysUser> selectUnallocatedList(SysUser user);

    public SysUser selectUserById(Long userId);

    public String selectUserRoleGroup(String userName);

    public boolean checkEmailUnique(SysUser user);

    public void checkUserAllowed(SysUser user);

    public void checkUserDataScope(Long userId);

    public int insertUser(SysUser user);

    public boolean registerUser(SysUser user);

    public int updateUser(SysUser user);

    public void insertUserAuth(Long userId, Long[] roleIds);

    public int updateUserStatus(SysUser user);

    public int updateUserProfile(SysUser user);

    public boolean updateUserAvatar(Long userId, String avatar);

    public int deleteUserById(Long userId);

    public int deleteUserByIds(Long[] userIds);

    public String importUser(List<SysUser> userList, Boolean isUpdateSupport, String operName);
}
