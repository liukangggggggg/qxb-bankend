package com.qxb.consultation.service;

import com.qxb.consultation.domain.BizParentChildRelation;

import java.util.List;

/**
 * 亲子绑定关系 服务层
 */
public interface IBizParentChildRelationService {

    /**
     * 根据主键查询亲子关系
     *
     * @param relationId 关系ID
     * @return 亲子关系信息
     */
    BizParentChildRelation selectById(Long relationId);

    /**
     * 根据家长ID查询所有孩子列表
     *
     * @param parentUserId 家长用户ID
     * @return 亲子关系列表
     */
    List<BizParentChildRelation> selectByParentUserId(Long parentUserId);

    /**
     * 根据孩子ID查询所有家长列表
     *
     * @param childUserId 孩子用户ID
     * @return 亲子关系列表
     */
    List<BizParentChildRelation> selectByChildUserId(Long childUserId);

    /**
     * 根据家长和孩子ID查询亲子关系
     *
     * @param parentUserId 家长用户ID
     * @param childUserId  孩子用户ID
     * @return 亲子关系信息
     */
    BizParentChildRelation selectByParentAndChild(Long parentUserId, Long childUserId);

    /**
     * 多条件查询亲子关系列表
     *
     * @param query 查询条件
     * @return 亲子关系列表
     */
    List<BizParentChildRelation> selectList(BizParentChildRelation query);

    /**
     * 查询亲子关系总数
     *
     * @param query 查询条件
     * @return 总数
     */
    int selectCount(BizParentChildRelation query);

    /**
     * 新增亲子绑定关系
     *
     * @param relation 亲子关系信息
     * @return 结果
     */
    int insert(BizParentChildRelation relation);

    /**
     * 修改亲子绑定关系
     *
     * @param relation 亲子关系信息
     * @return 结果
     */
    int update(BizParentChildRelation relation);

    /**
     * 审核亲子关系（确认/拒绝）
     *
     * @param relationId 关系ID
     * @param authStatus 审核状态（1已确认 2已拒绝）
     * @return 结果
     */
    int auditRelation(Long relationId, String authStatus);

    /**
     * 删除亲子绑定关系
     *
     * @param relationId 关系ID
     * @return 结果
     */
    int deleteById(Long relationId);

    /**
     * 解除亲子绑定关系
     *
     * @param parentUserId 家长用户ID
     * @param childUserId  孩子用户ID
     * @return 结果
     */
    int unbindRelation(Long parentUserId, Long childUserId);

    /**
     * 校验亲子关系是否已存在
     *
     * @param parentUserId 家长用户ID
     * @param childUserId  孩子用户ID
     * @return true-已存在 false-不存在
     */
    boolean checkRelationExists(Long parentUserId, Long childUserId);

    /**
     * 统计家长的孩子数量
     *
     * @param parentUserId 家长用户ID
     * @return 孩子数量
     */
    int countByParentUserId(Long parentUserId);

    /**
     * 统计孩子的家长数量
     *
     * @param childUserId 孩子用户ID
     * @return 家长数量
     */
    int countByChildUserId(Long childUserId);
}