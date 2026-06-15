package com.qxb.consultation.mapper;

import com.qxb.consultation.domain.BizParentChildRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 亲子绑定关系 Mapper 接口
 * 对应表：public.biz_parent_child_relation
 */
@Mapper
public interface BizParentChildRelationMapper {

    /**
     * 根据主键查询单条记录
     */
    BizParentChildRelation selectById(Long relationId);

    /**
     * 根据家长ID查询所有亲子关系列表
     */
    List<BizParentChildRelation> selectByParentUserId(Long parentUserId);

    /**
     * 根据孩子ID查询所有亲子关系列表
     */
    List<BizParentChildRelation> selectByChildUserId(Long childUserId);

    /**
     * 根据家长和孩子ID查询唯一的亲子关系（唯一索引查询）
     */
    BizParentChildRelation selectByParentAndChild(
            @Param("parentUserId") Long parentUserId,
            @Param("childUserId") Long childUserId
    );

    /**
     * 根据审核状态查询亲子关系列表
     */
    List<BizParentChildRelation> selectByAuthStatus(String authStatus);

    /**
     * 多条件查询列表
     *
     * @param query 查询条件实体
     */
    List<BizParentChildRelation> selectList(BizParentChildRelation query);

    /**
     * 多条件查询总条数（分页配套使用）
     *
     * @param query 查询条件实体
     */
    int selectCount(BizParentChildRelation query);

    /**
     * 选择性新增（空字段不插入，复用数据库默认值）
     *
     * @param record 新增数据实体
     * @return 影响行数
     */
    int insertSelective(BizParentChildRelation record);

    /**
     * 根据主键选择性更新（仅更新非空字段）
     *
     * @param record 更新数据实体
     * @return 影响行数
     */
    int updateByIdSelective(BizParentChildRelation record);

    /**
     * 更新审核状态和绑定时间
     *
     * @param relationId 主键ID
     * @param authStatus 审核状态
     * @param bindTime   绑定生效时间
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int updateAuthStatus(
            @Param("relationId") Long relationId,
            @Param("authStatus") String authStatus,
            @Param("bindTime") LocalDateTime bindTime,
            @Param("updateTime") LocalDateTime updateTime
    );

    /**
     * 根据主键删除
     *
     * @param relationId 主键ID
     * @return 影响行数
     */
    int deleteById(Long relationId);

    /**
     * 根据家长和孩子ID删除亲子关系
     *
     * @param parentUserId 家长ID
     * @param childUserId  孩子ID
     * @return 影响行数
     */
    int deleteByParentAndChild(
            @Param("parentUserId") Long parentUserId,
            @Param("childUserId") Long childUserId
    );
}