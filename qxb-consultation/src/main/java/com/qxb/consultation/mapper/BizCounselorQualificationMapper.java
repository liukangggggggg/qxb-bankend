package com.qxb.consultation.mapper;

import com.qxb.consultation.domain.BizCounselorQualification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 咨询师资格认证 Mapper 接口
 * 对应表：public.biz_counselor_qualification
 */
@Mapper
public interface BizCounselorQualificationMapper {

    /**
     * 根据主键查询单条记录
     */
    BizCounselorQualification selectById(Long id);

    /**
     * 根据用户ID查询证书列表
     */
    List<BizCounselorQualification> selectByUserId(Long userId);

    /**
     * 根据证书编号查询单条记录
     */
    BizCounselorQualification selectByCertNo(String certNo);

    /**
     * 根据审核状态查询证书列表
     */
    List<BizCounselorQualification> selectByVerifyStatus(String verifyStatus);

    /**
     * 多条件查询列表
     *
     * @param query 查询条件实体
     */
    List<BizCounselorQualification> selectList(BizCounselorQualification query);

    /**
     * 多条件查询总条数（分页配套使用）
     *
     * @param query 查询条件实体
     */
    Integer selectCount(BizCounselorQualification query);

    /**
     * 选择性新增（空字段不插入，复用数据库默认值）
     *
     * @param record 新增数据实体
     * @return 影响行数
     */
    int insertSelective(BizCounselorQualification record);

    /**
     * 根据主键选择性更新（仅更新非空字段）
     *
     * @param record 更新数据实体
     * @return 影响行数
     */
    int updateByIdSelective(BizCounselorQualification record);

    /**
     * 更新审核状态
     *
     * @param id 主键ID
     * @param verifyStatus 审核状态
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int updateVerifyStatus(
            @Param("id") Long id,
            @Param("verifyStatus") String verifyStatus,
            @Param("updateTime") LocalDateTime updateTime
    );

    /**
     * 逻辑删除（更新删除标记，不物理删除）
     *
     * @param id 主键ID
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int deleteById(
            @Param("id") Long id,
            @Param("updateTime") LocalDateTime updateTime
    );
}
