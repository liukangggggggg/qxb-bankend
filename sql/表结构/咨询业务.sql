-- 创建预约业务专属序列
CREATE SEQUENCE "public"."seq_qxb_appointment_id" INCREMENT 1 START 50000;

-- =============================================================================
-- 表十六：咨询预约表 (qxb_appointment) ── 订单对应的最高频业务表
-- =============================================================================
CREATE TABLE "public"."qxb_appointment" (
                                            "appointment_id" int8 NOT NULL DEFAULT nextval('seq_qxb_appointment_id'::regclass),
                                            "tenant_id" int8 NOT NULL,
                                            "user_id" int8 NOT NULL,
                                            "consultant_id" int8 NOT NULL,
                                            "schedule_id" int8 NOT NULL,
                                            "appointment_status" char(1) DEFAULT '0'::bpchar,
                                            "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                            "complete_time" timestamp(6) DEFAULT NULL,
                                            CONSTRAINT "pk_qxb_appointment" PRIMARY KEY ("appointment_id")
) WITH (orientation=ROW, storetype=ustore) DISTRIBUTE BY HASH("tenant_id"); -- 租户级绝对隔离业务，以tenant_id分布

-- 核心索引：加速咨询师和来访者查看自己的预约日程，并在分布式下优化Local Join
CREATE INDEX "idx_appointment_lookup" ON "public"."qxb_appointment" ("tenant_id", "user_id", "consultant_id");

-- 表及全字段注释覆盖
COMMENT ON TABLE "public"."qxb_appointment" IS '咨询预约表 (订单对应的核心高频业务表，受多租户绝对隔离保护)';
COMMENT ON COLUMN "public"."qxb_appointment"."appointment_id" IS '预约单业务唯一主键ID (对应qxb_order.business_id)';
COMMENT ON COLUMN "public"."qxb_appointment"."tenant_id" IS '所属连锁机构租户ID (0代表平台自营大厅独立专家)';
COMMENT ON COLUMN "public"."qxb_appointment"."user_id" IS '来访者/挂号本人的全局UID (关联qxb_user.user_id)';
COMMENT ON COLUMN "public"."qxb_appointment"."consultant_id" IS '负责本次疏导的咨询师专家用户UID (关联qxb_user.user_id)';
COMMENT ON COLUMN "public"."qxb_appointment"."schedule_id" IS '关联的具体排班时段时段ID (用于锁定该时段已被抢占)';
COMMENT ON COLUMN "public"."qxb_appointment"."appointment_status" IS '业务状态机 (0-待支付, 1-预约成功/待履约, 2-咨询疏导中, 3-已完成待清算, 4-用户爽约, 5-已取消/退款)';
COMMENT ON COLUMN "public"."qxb_appointment"."create_time" IS '用户发起预约点进下单界面的时间';
COMMENT ON COLUMN "public"."qxb_appointment"."complete_time" IS '咨询师真正完成50分钟心理疏导、点击确认履约的完结时间';
