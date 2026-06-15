package com.qxb.consultation.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 咨询师资格认证表（一个咨询师可有多张证书）
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BizCounselorQualification {

    /** 自增主键 */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /** 关联sys_user.user_id */
    @TableField("user_id")
    private Long userId;

    /** 证书名称（如国家三级心理咨询师、沙盘治疗师） */
    @TableField("qualification_name")
    private String qualificationName;

    /** 发证机构（如人力资源和社会保障部） */
    @TableField("issuing_authority")
    private String issuingAuthority;

    /** 证书编号 */
    @TableField("cert_no")
    private String certNo;

    /** 获得日期 */
    @TableField("obtain_date")
    private LocalDate obtainDate;

    /** 有效期至 */
    @TableField("expire_date")
    private LocalDate expireDate;

    /** 证书照片/扫描件URL */
    @TableField("cert_image")
    private String certImage;

    /** 审核状态（0待审核 1已通过 2已驳回） */
    @TableField("verify_status")
    private String verifyStatus;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除（0存在 1删除） */
    @TableLogic
    @TableField("deleted")
    private String deleted;
}
