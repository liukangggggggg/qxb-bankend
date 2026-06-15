package com.qxb.consultation.service;

import com.qxb.consultation.domain.BizCounselorQualification;

import java.util.List;

/**
 * 咨询师资格认证 服务层
 */
public interface IBizCounselorQualificationService {

    /**
     * 根据主键查询资格证书
     *
     * @param id 证书ID
     * @return 资格证书信息
     */
    BizCounselorQualification selectById(Long id);

    /**
     * 根据用户ID查询证书列表
     *
     * @param userId 用户ID
     * @return 资格证书列表
     */
    List<BizCounselorQualification> selectByUserId(Long userId);

    /**
     * 根据证书编号查询
     *
     * @param certNo 证书编号
     * @return 资格证书信息
     */
    BizCounselorQualification selectByCertNo(String certNo);

    /**
     * 根据审核状态查询证书列表
     *
     * @param verifyStatus 审核状态
     * @return 资格证书列表
     */
    List<BizCounselorQualification> selectByVerifyStatus(String verifyStatus);

    /**
     * 多条件查询证书列表
     *
     * @param query 查询条件
     * @return 资格证书列表
     */
    List<BizCounselorQualification> selectList(BizCounselorQualification query);

    /**
     * 查询证书总数
     *
     * @param query 查询条件
     * @return 总数
     */
    int selectCount(BizCounselorQualification query);

    /**
     * 新增资格证书
     *
     * @param qualification 资格证书信息
     * @return 结果
     */
    int insert(BizCounselorQualification qualification);

    /**
     * 修改资格证书
     *
     * @param qualification 资格证书信息
     * @return 结果
     */
    int update(BizCounselorQualification qualification);

    /**
     * 删除资格证书
     *
     * @param id 证书ID
     * @return 结果
     */
    int deleteById(Long id);

    /**
     * 审核资格证书（通过/驳回）
     *
     * @param id 证书ID
     * @param verifyStatus 审核状态（1已通过 2已驳回）
     * @return 结果
     */
    int auditQualification(Long id, String verifyStatus);

    /**
     * 校验证书编号是否已存在
     *
     * @param certNo 证书编号
     * @return true-已存在 false-不存在
     */
    boolean checkCertNoExists(String certNo);

    /**
     * 统计用户的证书数量
     *
     * @param userId 用户ID
     * @return 证书数量
     */
    int countByUserId(Long userId);

    /**
     * 统计待审核的证书数量
     *
     * @return 待审核数量
     */
    int countPendingAudit();

    /**
     * 查询用户已通过的证书列表
     *
     * @param userId 用户ID
     * @return 资格证书列表
     */
    List<BizCounselorQualification> selectApprovedByUserId(Long userId);
}