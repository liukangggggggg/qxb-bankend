package com.qxb.consultation.service.impl;

import com.qxb.common.exception.ServiceException;
import com.qxb.common.utils.SecurityUtils;
import com.qxb.consultation.domain.BizCounselorProfile;
import com.qxb.consultation.mapper.BizCounselorProfileMapper;
import com.qxb.consultation.service.IBizCounselorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 咨询师档案 服务层实现
 */
@Service
public class BizCounselorProfileServiceImpl implements IBizCounselorProfileService {

    @Autowired
    private BizCounselorProfileMapper profileMapper;

    /**
     * 根据主键查询咨询师档案
     */
    @Override
    public BizCounselorProfile selectById(Long id) {
        return profileMapper.selectById(id);
    }

    /**
     * 根据用户ID查询咨询师档案
     */
    @Override
    public BizCounselorProfile selectByUserId(Long userId) {
        return profileMapper.selectByUserId(userId);
    }

    /**
     * 多条件查询咨询师档案列表
     */
    @Override
    public List<BizCounselorProfile> selectList(BizCounselorProfile query) {
        return profileMapper.selectList(query);
    }

    /**
     * 查询咨询师档案总数
     */
    @Override
    public int selectCount(BizCounselorProfile query) {
        return profileMapper.selectCount(query);
    }

    /**
     * 新增咨询师档案
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(BizCounselorProfile profile) {
        if (profile.getUserId() == null) {
            throw new ServiceException("用户ID不能为空");
        }

        if (checkProfileExists(profile.getUserId())) {
            throw new ServiceException("该用户已存在咨询师档案");
        }

        profile.setStatus("0");
        profile.setIsAcceptOrder(0);
        profile.setSortIndex(0);
        profile.setIsTop(0);
        profile.setVipExclusive(0);
        profile.setDeleted("0");
        profile.setCreateTime(LocalDateTime.now());
        profile.setUpdateTime(LocalDateTime.now());

        return profileMapper.insertSelective(profile);
    }

    /**
     * 修改咨询师档案
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(BizCounselorProfile profile) {
        if (profile.getId() == null) {
            throw new ServiceException("档案ID不能为空");
        }

        BizCounselorProfile existProfile = profileMapper.selectById(profile.getId());
        if (existProfile == null) {
            throw new ServiceException("咨询师档案不存在");
        }

        profile.setUpdateTime(LocalDateTime.now());
        return profileMapper.updateByIdSelective(profile);
    }

    /**
     * 删除咨询师档案
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        if (id == null) {
            throw new ServiceException("档案ID不能为空");
        }

        BizCounselorProfile profile = profileMapper.selectById(id);
        if (profile == null) {
            throw new ServiceException("咨询师档案不存在");
        }

        String updater = SecurityUtils.getUsername();
        return profileMapper.deleteById(id, updater, LocalDateTime.now());
    }

    /**
     * 校验用户是否已有档案
     */
    @Override
    public boolean checkProfileExists(Long userId) {
        BizCounselorProfile profile = profileMapper.selectByUserId(userId);
        return profile != null;
    }

    /**
     * 上架咨询师档案
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int publishProfile(Long id) {
        if (id == null) {
            throw new ServiceException("档案ID不能为空");
        }

        BizCounselorProfile profile = profileMapper.selectById(id);
        if (profile == null) {
            throw new ServiceException("咨询师档案不存在");
        }

        BizCounselorProfile updateProfile = new BizCounselorProfile();
        updateProfile.setId(id);
        updateProfile.setStatus("1");
        updateProfile.setUpdateTime(LocalDateTime.now());

        return profileMapper.updateByIdSelective(updateProfile);
    }

    /**
     * 下架咨询师档案
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unpublishProfile(Long id) {
        if (id == null) {
            throw new ServiceException("档案ID不能为空");
        }

        BizCounselorProfile profile = profileMapper.selectById(id);
        if (profile == null) {
            throw new ServiceException("咨询师档案不存在");
        }

        BizCounselorProfile updateProfile = new BizCounselorProfile();
        updateProfile.setId(id);
        updateProfile.setStatus("2");
        updateProfile.setUpdateTime(LocalDateTime.now());

        return profileMapper.updateByIdSelective(updateProfile);
    }

    /**
     * 查询已上架的咨询师列表
     */
    @Override
    public List<BizCounselorProfile> selectPublishedList() {
        BizCounselorProfile query = new BizCounselorProfile();
        query.setStatus("1");
        return profileMapper.selectList(query);
    }

    /**
     * 更新接单状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAcceptOrderStatus(Long userId, Integer isAcceptOrder) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        if (isAcceptOrder == null || (isAcceptOrder != 0 && isAcceptOrder != 1)) {
            throw new ServiceException("接单状态只能是0(否)或1(是)");
        }

        BizCounselorProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new ServiceException("咨询师档案不存在");
        }

        BizCounselorProfile updateProfile = new BizCounselorProfile();
        updateProfile.setId(profile.getId());
        updateProfile.setIsAcceptOrder(isAcceptOrder);
        updateProfile.setUpdateTime(LocalDateTime.now());

        return profileMapper.updateByIdSelective(updateProfile);
    }

    /**
     * 统计咨询师累计咨询人数
     */
    @Override
    public String getCounselorPeopleCount(Long userId) {
        if (userId == null) {
            return "0";
        }

        BizCounselorProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            return "0";
        }

        return profile.getCounselorPepoleCount() != null ? profile.getCounselorPepoleCount() : "0";
    }

    /**
     * 增加累计咨询人数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int incrementCounselorPeopleCount(Long userId) {
        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        BizCounselorProfile profile = profileMapper.selectByUserId(userId);
        if (profile == null) {
            throw new ServiceException("咨询师档案不存在");
        }

        String currentCount = profile.getCounselorPepoleCount();
        int count = 0;
        if (currentCount != null && !currentCount.isEmpty()) {
            try {
                count = Integer.parseInt(currentCount);
            } catch (NumberFormatException e) {
                count = 0;
            }
        }

        BizCounselorProfile updateProfile = new BizCounselorProfile();
        updateProfile.setId(profile.getId());
        updateProfile.setCounselorPepoleCount(String.valueOf(count + 1));
        updateProfile.setUpdateTime(LocalDateTime.now());

        return profileMapper.updateByIdSelective(updateProfile);
    }
}