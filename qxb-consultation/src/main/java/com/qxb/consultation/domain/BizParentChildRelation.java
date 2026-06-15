package com.qxb.consultation.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 亲子绑定关系表
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BizParentChildRelation {

    /** 亲子绑定关系自增主键 */
    @TableId(value = "relation_id", type = IdType.AUTO)
    private Long relationId;

    /** 家长的用户唯一UID (关联sys_user.user_id) */
    @TableField("parent_user_id")
    private Long parentUserId;

    /** 孩子的用户唯一UID (关联sys_user.user_id) */
    @TableField("child_user_id")
    private Long childUserId;

    /** 监护关系类型 (1父亲, 2母亲, 3其他法定监护人) */
    @TableField("relation_type")
    private String relationType;

    /** 绑定关系的审核/确认状态 (0待确认/审批中, 1已确认绑定成功, 2已拒绝解绑) */
    @TableField("auth_status")
    private String authStatus;

    /** 亲子关系确认绑定的生效时间 */
    @TableField("bind_time")
    private LocalDateTime bindTime;

    /** 家长发起绑定申请的时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}