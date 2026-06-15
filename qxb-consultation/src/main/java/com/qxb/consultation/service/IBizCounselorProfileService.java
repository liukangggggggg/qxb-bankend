package com.qxb.consultation.service;

import com.qxb.consultation.domain.BizCounselorProfile;

import java.util.List;

/**
 * 咨询师档案 服务层
 */
public interface IBizCounselorProfileService {

    /**
     * 根据主键查询咨询师档案
     *
     * @param id 档案ID
     * @return 咨询师档案信息
     */
    BizCounselorProfile selectById(Long id);

    /**
     * 根据用户ID查询咨询师档案
     *
     * @param userId 用户ID
     * @return 咨询师档案信息
     */
    BizCounselorProfile selectByUserId(Long userId);

    /**
     * 多条件查询咨询师档案列表
     *
     * @param query 查询条件
     * @return 咨询师档案列表
     */
    List<BizCounselorProfile> selectList(BizCounselorProfile query);

    /**
     * 查询咨询师档案总数
     *
     * @param query 查询条件
     * @return 总数
     */
    int selectCount(BizCounselorProfile query);

    /**
     * 新增咨询师档案
     *
     * @param profile 咨询师档案信息
     * @return 结果
     */
    int insert(BizCounselorProfile profile);

    /**
     * 修改咨询师档案
     *
     * @param profile 咨询师档案信息
     * @return 结果
     */
    int update(BizCounselorProfile profile);

    /**
     * 删除咨询师档案
     *
     * @param id 档案ID
     * @return 结果
     */
    int deleteById(Long id);

    /**
     * 校验用户是否已有档案
     *
     * @param userId 用户ID
     * @return true-已存在 false-不存在
     */
    boolean checkProfileExists(Long userId);

    /**
     * 上架咨询师档案
     *
     * @param id 档案ID
     * @return 结果
     */
    int publishProfile(Long id);

    /**
     * 下架咨询师档案
     *
     * @param id 档案ID
     * @return 结果
     */
    int unpublishProfile(Long id);

    /**
     * 查询已上架的咨询师列表
     *
     * @return 咨询师档案列表
     */
    List<BizCounselorProfile> selectPublishedList();

    /**
     * 更新接单状态
     *
     * @param userId 用户ID
     * @param isAcceptOrder 接单状态（0否 1是）
     * @return 结果
     */
    int updateAcceptOrderStatus(Long userId, Integer isAcceptOrder);

    /**
     * 统计咨询师累计咨询人数
     *
     * @param userId 用户ID
     * @return 累计咨询人数
     */
    String getCounselorPeopleCount(Long userId);

    /**
     * 增加累计咨询人数
     *
     * @param userId 用户ID
     * @return 结果
     */
    int incrementCounselorPeopleCount(Long userId);
}