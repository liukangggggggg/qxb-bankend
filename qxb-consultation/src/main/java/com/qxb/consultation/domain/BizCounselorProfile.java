package com.qxb.consultation.domain;

import com.baomidou.mybatisplus.annotation.*;
import jdk.jfr.DataAmount;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class BizCounselorProfile {


        /** 自增主键 */
        @TableId(value = "id", type = IdType.INPUT)
        private Long id;

        /** 关联sys_user.user_id */
        @TableField("user_id")
        private Long userId;

        /** 个人头衔（如国家二级心理咨询师） */
        @TableField("personal_title")
        private String personalTitle;

        /** 个人介绍（富文本） */
        @TableField("introduction")
        private String introduction;

        /** 个人签名（短签名） */
        @TableField("signature")
        private String signature;

        /** 最高学历 */
        @TableField("education")
        private String education;

        /** 咨询师等级 */
        @TableField("level")
        private String level;

        /** 是否具备督导资质（0否 1是） */
        @TableField("is_supervisor")
        private String isSupervisor;

        /** 从业开始时间 */
        @TableField("start_date")
        private LocalDate startDate;

        /** 可工作时间段 */
        @TableField("work_time")
        private String workTime;

        /** 咨询方式（逗号分隔：文字/语音/视频/面对面） */
        @TableField("methods")
        private String methods;

        /** 咨询价格 */
        @TableField("price")
        private BigDecimal price;

        /** 面对面咨询地址 */
        @TableField("consulation_addr")
        private String consulationAddr;

        /** 咨询时间基数（如50分钟/次） */
        @TableField("time_base")
        private String timeBase;

        /** 是否接单（0否 1是） */
        @TableField("is_accept_order")
        private Integer isAcceptOrder;

        /** 公益咨询类型 */
        @TableField("public_consultation")
        private String publicConsultation;

        /** 累计咨询人数 */
        @TableField("counselor_pepole_count")
        private String counselorPepoleCount;

        /** 排序权重 */
        @TableField("sort_index")
        private Integer sortIndex;

        /** 是否置顶 */
        @TableField("is_top")
        private Integer isTop;

        /** 活动优先级 */
        @TableField("activity_priority")
        private String activityPriority;

        /** 赠送优惠券ID列表（逗号分隔） */
        @TableField("coupon_ids")
        private String couponIds;

        /** 是否会员专属 */
        @TableField("vip_exclusive")
        private Integer vipExclusive;

        /** 数据标签ID（逗号分隔） */
        @TableField("data_labels")
        private String dataLabels;

        /** 推荐人 */
        @TableField("referee")
        private String referee;

        /** 自定义标签 */
        @TableField("tags")
        private String tags;

        /** 微信二维码图片URL */
        @TableField("wx_qr_code")
        private String wxQrCode;

        /** APP推广二维码URL */
        @TableField("app_qr_code")
        private String appQrCode;

        /** 上架状态（0草稿 1已上架 2已下架） */
        @TableField("status")
        private String status;

        /** 创建者（插入自动填充） */
        @TableField(value = "creator", fill = FieldFill.INSERT)
        private String creator;

        /** 创建时间（插入自动填充） */
        @TableField(value = "create_time", fill = FieldFill.INSERT)
        private LocalDateTime createTime;

        /** 更新者（插入/更新自动填充） */
        @TableField(value = "updater", fill = FieldFill.INSERT_UPDATE)
        private String updater;

        /** 更新时间（插入/更新自动填充） */
        @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
        private LocalDateTime updateTime;

        /** 逻辑删除（0存在 1删除） */
        @TableLogic
        @TableField("deleted")
        private String deleted;
    }

