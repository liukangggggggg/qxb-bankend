package com.qxb.consultation.mapper;

import com.qxb.consultation.domain.BizCounselorProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 咨询师档案 Mapper 接口
 * 对应表：public.biz_counselor_profile
 */
@Mapper
public interface BizCounselorProfileMapper {

    /**
     * 根据主键查询单条记录
     */
    BizCounselorProfile selectById(Long id);

    /**
     * 根据用户ID查询（唯一索引，业务高频查询）
     */
    BizCounselorProfile selectByUserId(Long userId);

    /**
     * 多条件查询列表
     * @param query 查询条件实体
     */
    List<BizCounselorProfile> selectList(BizCounselorProfile query);

    /**
     * 多条件查询总条数（分页配套使用）
     * @param query 查询条件实体
     */
    Integer selectCount(BizCounselorProfile query);

    /**
     * 选择性新增（空字段不插入，复用数据库默认值）
     * @param record 新增数据实体
     * @return 影响行数
     */
    int insertSelective(BizCounselorProfile record);

    /**
     * 根据主键选择性更新（仅更新非空字段）
     * @param record 更新数据实体
     * @return 影响行数
     */
    int updateByIdSelective(BizCounselorProfile record);

    /**
     * 逻辑删除（更新删除标记，不物理删除）
     * @param id 主键ID
     * @param updater 更新人
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int deleteById(
            @Param("id") Long id,
            @Param("updater") String updater,
            @Param("updateTime") LocalDateTime updateTime
    );
}