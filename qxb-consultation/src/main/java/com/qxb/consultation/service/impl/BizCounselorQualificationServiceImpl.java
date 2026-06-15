package com.qxb.consultation.service.impl;

import com.qxb.common.exception.ServiceException;
import com.qxb.consultation.domain.BizCounselorQualification;
import com.qxb.consultation.mapper.BizCounselorQualificationMapper;
import com.qxb.consultation.service.IBizCounselorQualificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 咨询师资格认证 服务层实现
 */
@Service
public class BizCounselorQualificationServiceImpl implements IBizCounselorQualificationService {

    @Autowired
    private BizCounselorQualificationMapper qualificationMapper;

    /**
     * 根据主键查询资格证书
     */
    @Override
    public BizCounselorQualification selectById(Long id) {
        return qualificationMapper.selectById(id);
    }

    /**
     * 根据用户ID查询证书列表
     */
    @Override
    public List<BizCounselorQualification> selectByUserId(Long userId) {
        return qualificationMapper.selectByUserId(userId);
    }

    /**
     * 根据证书编号查询
     */
    @Override
    public BizCounselorQualification selectByCertNo(String certNo) {
        return qualificationMapper.selectByCertNo(certNo);
    }

    /**
     * 根据审核状态查询证书列表
     */
    @Override
    public List<BizCounselorQualification> selectByVerifyStatus(String verifyStatus) {
        return qualificationMapper.selectByVerifyStatus(verifyStatus);
    }

    /**
     * 多条件查询证书列表
     */
    @Override
    public List<BizCounselorQualification> selectList(BizCounselorQualification query) {
        return qualificationMapper.selectList(query);
    }

    /**
     * 查询证书总数
     */
    @Override
    public int selectCount(BizCounselorQualification query) {
        return qualificationMapper.selectCount(query);
    }

    /**
     * 新增资格证书
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(BizCounselorQualification qualification) {
        if (qualification.getUserId() == null) {
            throw new ServiceException("用户ID不能为空");
        }

        if (qualification.getCertNo() == null || qualification.getCertNo().isEmpty()) {
            throw new ServiceException("证书编号不能为空");
        }

        if (checkCertNoExists(qualification.getCertNo())) {
            throw new ServiceException("该证书编号已存在");
        }

        if (qualification.getQualificationName() == null || qualification.getQualificationName().isEmpty()) {
            throw new ServiceException("证书名称不能为空");
        }

        qualification.setVerifyStatus("0");
        qualification.setDeleted("0");
        qualification.setCreateTime(LocalDateTime.now());
        qualification.setUpdateTime(LocalDateTime.now());

        return qualificationMapper.insertSelective(qualification);
    }

    /**
     * 修改资格证书
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(BizCounselorQualification qualification) {
        if (qualification.getId() == null) {
            throw new ServiceException("证书ID不能为空");
        }

        BizCounselorQualification existQualification = qualificationMapper.selectById(qualification.getId());
        if (existQualification == null) {
            throw new ServiceException("资格证书不存在");
        }

        if (!"0".equals(existQualification.getVerifyStatus())) {
            throw new ServiceException("已审核的证书不能修改");
        }

        qualification.setUpdateTime(LocalDateTime.now());
        return qualificationMapper.updateByIdSelective(qualification);
    }

    /**
     * 删除资格证书
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        if (id == null) {
            throw new ServiceException("证书ID不能为空");
        }

        BizCounselorQualification qualification = qualificationMapper.selectById(id);
        if (qualification == null) {
            throw new ServiceException("资格证书不存在");
        }

        return qualificationMapper.deleteById(id, LocalDateTime.now());
    }

    /**
     * 审核资格证书（通过/驳回）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditQualification(Long id, String verifyStatus) {
        if (id == null) {
            throw new ServiceException("证书ID不能为空");
        }

        if (!"1".equals(verifyStatus) && !"2".equals(verifyStatus)) {
            throw new ServiceException("审核状态只能是1(已通过)或2(已驳回)");
        }

        BizCounselorQualification qualification = qualificationMapper.selectById(id);
        if (qualification == null) {
            throw new ServiceException("资格证书不存在");
        }

        if (!"0".equals(qualification.getVerifyStatus())) {
            throw new ServiceException("该证书已经审核过，不能重复审核");
        }

        return qualificationMapper.updateVerifyStatus(id, verifyStatus, LocalDateTime.now());
    }

    /**
     * 校验证书编号是否已存在
     */
    @Override
    public boolean checkCertNoExists(String certNo) {
        if (certNo == null || certNo.isEmpty()) {
            return false;
        }
        BizCounselorQualification qualification = qualificationMapper.selectByCertNo(certNo);
        return qualification != null;
    }

    /**
     * 统计用户的证书数量
     */
    @Override
    public int countByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        List<BizCounselorQualification> list = qualificationMapper.selectByUserId(userId);
        return list != null ? list.size() : 0;
    }

    /**
     * 统计待审核的证书数量
     */
    @Override
    public int countPendingAudit() {
        BizCounselorQualification query = new BizCounselorQualification();
        query.setVerifyStatus("0");
        return qualificationMapper.selectCount(query);
    }

    /**
     * 查询用户已通过的证书列表
     */
    @Override
    public List<BizCounselorQualification> selectApprovedByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }

        BizCounselorQualification query = new BizCounselorQualification();
        query.setUserId(userId);
        query.setVerifyStatus("1");
        return qualificationMapper.selectList(query);
    }
}