-- 创建课程模块专属序列
CREATE SEQUENCE "public"."seq_course_id" INCREMENT 1 START 1000;
CREATE SEQUENCE "public"."seq_user_course_id" INCREMENT 1 START 10000;

-- =============================================================================
-- 表十七：课程配置主表 (course) ── 属于元数据配置表（类似于量表主表）
-- =============================================================================
CREATE TABLE "public"."course" (
                                   "id" int8 NOT NULL DEFAULT nextval('seq_course_id'::regclass),
                                   "title" varchar(100) NOT NULL,
                                   "cover_img" varchar(255) DEFAULT ''::character varying,
                                   "price" numeric(10,2) NOT NULL DEFAULT 0.00,
                                   "description" text,
                                   "status" char(1) DEFAULT '1'::bpchar,
                                   "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                   CONSTRAINT "pk_course" PRIMARY KEY ("id")
) WITH (orientation=ROW) DISTRIBUTE BY HASH("id");

COMMENT ON TABLE "public"."course" IS '心理课程配置主表 (存储全网可售卖的课程信息)';
COMMENT ON COLUMN "public"."course"."id" IS '课程唯一自增ID';
COMMENT ON COLUMN "public"."course"."title" IS '课程名称 (如: 21天焦虑情绪自我拯救指南)';
COMMENT ON COLUMN "public"."course"."price" IS '课程售卖标准价格';
COMMENT ON COLUMN "public"."course"."status" IS '课程上架状态 (0-下架, 1-上架售卖)';


-- =============================================================================
-- 表十八：用户购买课程记录表 (qxb_user_course) ── 【核心：这就是订单对应的业务表！】
-- =============================================================================
CREATE TABLE "public"."qxb_user_course" (
                                            "user_course_id" int8 NOT NULL DEFAULT nextval('seq_user_course_id'::regclass),
                                            "user_id" int8 NOT NULL,
                                            "course_id" int8 NOT NULL,
                                            "learn_progress" int4 DEFAULT 0,
                                            "lock_status" char(1) DEFAULT '0'::bpchar,
                                            "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                            CONSTRAINT "pk_qxb_user_course" PRIMARY KEY ("user_course_id")
) WITH (orientation=ROW, storetype=ustore) DISTRIBUTE BY HASH("user_id"); -- 跟着用户走

-- 核心联合唯一索引：物理层锁死“同一个用户对同一门课重复购买”的Bug
CREATE UNIQUE INDEX "uk_user_course" ON "public"."qxb_user_course" ("user_id", "course_id");

COMMENT ON TABLE "public"."qxb_user_course" IS '用户购买课程记录表 (订单最终指向的、证明用户拥有该课的业务凭证表)';
COMMENT ON COLUMN "public"."qxb_user_course"."user_course_id" IS '用户课程关系唯一主键 (对应qxb_order.business_id)';
COMMENT ON COLUMN "public"."qxb_user_course"."user_id" IS '购买课程的用户全局UID (关联qxb_user.user_id)';
COMMENT ON COLUMN "public"."qxb_user_course"."course_id" IS '所购买的课程配置ID (关联course.id)';
COMMENT ON COLUMN "public"."qxb_user_course"."learn_progress" IS '该用户在这门课下的学习进度百分比 (0-100)';
COMMENT ON COLUMN "public"."qxb_user_course"."lock_status" IS '该课程购买凭证状态 (0-正常解锁可看, 1-因退款或违规强行锁死冻结)';
