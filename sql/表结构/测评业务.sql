-- 创建用户购买量表凭证专属序列
CREATE SEQUENCE "public"."seq_user_scale_id" INCREMENT 1 START 10000;

-- =============================================================================
-- 表十九：用户购买量表凭证表 (sys_user_scale) ── 订单对应的测评业务核心表
-- =============================================================================
CREATE TABLE "public"."sys_user_scale" (
                                           "user_scale_id" int8 NOT NULL DEFAULT nextval('seq_user_scale_id'::regclass),
                                           "user_id" int8 NOT NULL,
                                           "scale_id" int8 NOT NULL,
                                           "use_status" char(1) DEFAULT '0'::bpchar,
                                           "lock_status" char(1) DEFAULT '1'::bpchar,
                                           "eval_record_id" int8 DEFAULT NULL,
                                           "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                           "pay_time" timestamp(6) DEFAULT NULL,
                                           CONSTRAINT "pk_sys_user_scale" PRIMARY KEY ("user_course_id")
) WITH (orientation=ROW, storetype=ustore) DISTRIBUTE BY HASH("user_id");

-- 核心联合索引：加速用户在“我的测评”页面捞取自己买过的量表记录
CREATE INDEX "idx_user_scale_lookup" ON "public"."sys_user_scale" ("user_id", "scale_id", "lock_status");

-- 表及全字段注释覆盖
COMMENT ON TABLE "public"."sys_user_scale" IS '用户购买量表凭证表 (订单最终指向的、证明用户拥有某量表答题权的业务表)';
COMMENT ON COLUMN "public"."sys_user_scale"."user_scale_id" IS '用户量表关系唯一主键 (对应sys_order.business_id)';
COMMENT ON COLUMN "public"."sys_user_scale"."user_id" IS '购买量表的用户全局UID (关联sys_user.user_id，孩子自己买绑孩子，家长买绑家长)';
COMMENT ON COLUMN "public"."sys_user_scale"."scale_id" IS '所购买的量表配置ID (关联scale.id)';
COMMENT ON COLUMN "public"."sys_user_scale"."use_status" IS '作答消耗状态 (0-未使用/待作答, 1-作答中/答到一半, 2-已作答完结并生成报告)';
COMMENT ON COLUMN "public"."sys_user_scale"."lock_status" IS '该凭证的锁定状态 (0-正常解锁可做题, 1-待支付锁死, 2-因退款或违规强行再次冻结)';
COMMENT ON COLUMN "public"."sys_user_scale"."eval_record_id" IS '最终产出的测评报告ID (可为空，只有当use_status=2作答完结时，才反向绑定sys_eval_record.id，方便用户从凭证一键跳转看报告)';
COMMENT ON COLUMN "public"."sys_user_scale"."create_time" IS '用户点击购买、生成此作答凭证的时间';
COMMENT ON COLUMN "public"."sys_user_scale"."pay_time" IS '订单支付成功、该凭证正式解锁的时间';
