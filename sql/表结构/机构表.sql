-- ==========================================
-- 机构信息表 (qxb_tenant)
-- 说明：作为全系统多租户架构的机构定义底座，
--       现有 qxb_tenant_user / qxb_appointment /
--       qxb_order 等表均依赖 tenant_id，
--       此前缺失机构主表，现补全。
-- ==========================================

-- 1. 创建全局序列
CREATE SEQUENCE "public"."seq_qxb_tenant_id" INCREMENT 1 START 100;


-- 2. 机构信息主表
CREATE TABLE "public"."qxb_tenant" (
                                       "tenant_id" int8 NOT NULL DEFAULT nextval('seq_qxb_tenant_id'::regclass),
                                       "tenant_code" varchar(20) NOT NULL,
                                       "tenant_name" varchar(100) NOT NULL,
                                       "logo" varchar(255) DEFAULT ''::character varying,
                                       "contact_person" varchar(30) DEFAULT ''::character varying,
                                       "contact_phone" varchar(20) DEFAULT ''::character varying,
                                       "contact_email" varchar(100) DEFAULT ''::character varying,
                                       "province" varchar(20) DEFAULT ''::character varying,
                                       "city" varchar(20) DEFAULT ''::character varying,
                                       "district" varchar(20) DEFAULT ''::character varying,
                                       "address" varchar(255) DEFAULT ''::character varying,
                                       "intro" varchar(500) DEFAULT ''::character varying,
                                       "status" char(1) DEFAULT '0'::bpchar,
                                       "del_flag" char(1) DEFAULT '0'::bpchar,
                                       "create_by" varchar(64) DEFAULT ''::character varying,
                                       "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                       "update_by" varchar(64) DEFAULT ''::character varying,
                                       "update_time" timestamp(6) DEFAULT NULL,
                                       "remark" varchar(500) DEFAULT NULL::character varying,
                                       CONSTRAINT "pk_qxb_tenant" PRIMARY KEY ("tenant_id")
)
    WITH (orientation=ROW, storetype=ustore)
    DISTRIBUTE BY HASH("tenant_id");

-- 核心索引
CREATE UNIQUE INDEX "uk_tenant_code" ON "public"."qxb_tenant" ("tenant_code");
CREATE INDEX "idx_tenant_name" ON "public"."qxb_tenant" ("tenant_name");

-- 表及字段全注释
COMMENT ON TABLE "public"."qxb_tenant" IS '机构信息表 (多租户架构下的机构定义底座)';
COMMENT ON COLUMN "public"."qxb_tenant"."tenant_id" IS '机构唯一ID (对应qxb_tenant_user.tenant_id等)';
COMMENT ON COLUMN "public"."qxb_tenant"."tenant_code" IS '机构编码 (唯一业务标识，如: HQ001, WH002)';
COMMENT ON COLUMN "public"."qxb_tenant"."tenant_name" IS '机构全称';
COMMENT ON COLUMN "public"."qxb_tenant"."logo" IS '机构Logo URL地址';
COMMENT ON COLUMN "public"."qxb_tenant"."contact_person" IS '机构联系人姓名';
COMMENT ON COLUMN "public"."qxb_tenant"."contact_phone" IS '机构联系电话';
COMMENT ON COLUMN "public"."qxb_tenant"."contact_email" IS '机构联系邮箱';
COMMENT ON COLUMN "public"."qxb_tenant"."province" IS '机构所在省份';
COMMENT ON COLUMN "public"."qxb_tenant"."city" IS '机构所在城市';
COMMENT ON COLUMN "public"."qxb_tenant"."district" IS '机构所在区县';
COMMENT ON COLUMN "public"."qxb_tenant"."address" IS '机构详细地址';
COMMENT ON COLUMN "public"."qxb_tenant"."intro" IS '机构简介';
COMMENT ON COLUMN "public"."qxb_tenant"."status" IS '机构全局状态 (0正常 1停用，停用后该机构下所有用户无法登录)';
COMMENT ON COLUMN "public"."qxb_tenant"."del_flag" IS '逻辑删除标志 (0代表存在 2代表删除)';
COMMENT ON COLUMN "public"."qxb_tenant"."create_by" IS '创建者账号/ID';
COMMENT ON COLUMN "public"."qxb_tenant"."create_time" IS '机构创建时间';
COMMENT ON COLUMN "public"."qxb_tenant"."update_by" IS '更新者账号/ID';
COMMENT ON COLUMN "public"."qxb_tenant"."update_time" IS '最后一次信息更新时间';
COMMENT ON COLUMN "public"."qxb_tenant"."remark" IS '备注信息';
