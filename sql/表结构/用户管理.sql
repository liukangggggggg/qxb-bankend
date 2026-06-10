-- 1. 创建全局序列
CREATE SEQUENCE "public"."seq_qxb_user_id" INCREMENT 1 START 10000;
CREATE SEQUENCE "public"."seq_qxb_user_auth_id" INCREMENT 1 START 1;
CREATE SEQUENCE "public"."seq_qxb_parent_child_id" INCREMENT 1 START 1;


-- ==========================================
-- 表一：用户核心主体表 (qxb_user)
-- ==========================================
CREATE TABLE "public"."qxb_user" (
                                     "user_id" int8 NOT NULL DEFAULT nextval('seq_qxb_user_id'::regclass),
                                     "nick_name" varchar(30) NOT NULL,
                                     "avatar" varchar(255) DEFAULT ''::character varying,
                                     "sex" char(1) DEFAULT '0'::bpchar,
                                     "birthday" date DEFAULT NULL,
                                     "status" char(1) DEFAULT '0'::bpchar,
                                     "del_flag" char(1) DEFAULT '0'::bpchar,
                                     "create_by" varchar(64) DEFAULT ''::character varying,
                                     "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                     "update_by" varchar(64) DEFAULT ''::character varying,
                                     "update_time" timestamp(6) DEFAULT NULL,
                                     "remark" varchar(500) DEFAULT NULL::character varying,
                                     CONSTRAINT "pk_qxb_user" PRIMARY KEY ("user_id")
)
    WITH (orientation=ROW, storetype=ustore)
    DISTRIBUTE BY HASH("user_id");

-- 表及字段全注释
COMMENT ON TABLE "public"."qxb_user" IS '用户核心主体表';
COMMENT ON COLUMN "public"."qxb_user"."user_id" IS '全局唯一用户ID (UID底座，全系统业务追踪核心)';
COMMENT ON COLUMN "public"."qxb_user"."nick_name" IS '用户昵称';
COMMENT ON COLUMN "public"."qxb_user"."avatar" IS '用户头像URL地址 (预留255位以兼容多端长URL)';
COMMENT ON COLUMN "public"."qxb_user"."sex" IS '用户性别 (0男 1女 2未知)';
COMMENT ON COLUMN "public"."qxb_user"."birthday" IS '出生日期 (用于在心理咨询和量表测评中动态计算精确年龄段)';
COMMENT ON COLUMN "public"."qxb_user"."status" IS '全局账号状态 (0正常 1停用，停用后全端、全机构均无法登录)';
COMMENT ON COLUMN "public"."qxb_user"."del_flag" IS '逻辑删除标志 (0代表存在 2代表删除)';
COMMENT ON COLUMN "public"."qxb_user"."create_by" IS '创建者账号/ID';
COMMENT ON COLUMN "public"."qxb_user"."create_time" IS '账号创建时间';
COMMENT ON COLUMN "public"."qxb_user"."update_by" IS '更新者账号/ID';
COMMENT ON COLUMN "public"."qxb_user"."update_time" IS '最后一次资料更新时间';
COMMENT ON COLUMN "public"."qxb_user"."remark" IS '备注信息';


-- ==========================================
-- 表二：多端登录认证表 (qxb_user_auth)
-- ==========================================
CREATE TABLE "public"."qxb_user_auth" (
                                          "auth_id" int8 NOT NULL DEFAULT nextval('seq_qxb_user_auth_id'::regclass),
                                          "user_id" int8 NOT NULL,
                                          "identity_type" varchar(20) NOT NULL,
                                          "identifier" varchar(100) NOT NULL,
                                          "union_id" varchar(100) DEFAULT NULL::character varying,
                                          "credential" varchar(255) DEFAULT ''::character varying,
                                          "login_ip" varchar(128) DEFAULT ''::character varying,
                                          "login_date" timestamp(6) DEFAULT NULL,
                                          "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                          CONSTRAINT "pk_qxb_user_auth" PRIMARY KEY ("auth_id")
)
    WITH (orientation=ROW, storetype=ustore)
    DISTRIBUTE BY HASH("user_id");

-- 核心索引
CREATE UNIQUE INDEX "uk_auth_identity" ON "public"."qxb_user_auth" ("identity_type", "identifier");
CREATE INDEX "idx_auth_union_id" ON "public"."qxb_user_auth" ("union_id");
CREATE INDEX "idx_auth_user_id" ON "public"."qxb_user_auth" ("user_id");

-- 表及字段全注释
COMMENT ON TABLE "public"."qxb_user_auth" IS '多端登录凭证表';
COMMENT ON COLUMN "public"."qxb_user_auth"."auth_id" IS '凭证记录自增ID';
COMMENT ON COLUMN "public"."qxb_user_auth"."user_id" IS '关联用户主体表的user_id';
COMMENT ON COLUMN "public"."qxb_user_auth"."identity_type" IS '认证登录渠道类型 (password:账号密码, phone:手机快捷验证码, wechat_mp:微信小程序)';
COMMENT ON COLUMN "public"."qxb_user_auth"."identifier" IS '唯一通道标识 (对应类型分别存储:用户名、手机号、微信OpenID)';
COMMENT ON COLUMN "public"."qxb_user_auth"."union_id" IS '微信开放平台唯一标识 (多端融合、跨端换绑防重复建户的核心依据)';
COMMENT ON COLUMN "public"."qxb_user_auth"."credential" IS '凭证内容 (如密码哈希密文，微信或验证码免密登录时保持为NULL)';
COMMENT ON COLUMN "public"."qxb_user_auth"."login_ip" IS '该通道最后一次登录的客户端IP地址';
COMMENT ON COLUMN "public"."qxb_user_auth"."login_date" IS '该通道最后一次登录的精确时间';
COMMENT ON COLUMN "public"."qxb_user_auth"."create_time" IS '该登录通道的绑定创建时间';


-- ==========================================
-- 表三：机构用户关系表 (qxb_tenant_user)
-- ==========================================
CREATE TABLE "public"."qxb_tenant_user" (
                                            "tenant_id" int8 NOT NULL,
                                            "user_id" int8 NOT NULL,
                                            "dept_id" int8 DEFAULT NULL,
                                            "user_type" varchar(2) DEFAULT '01'::character varying,
                                            "tenant_status" char(1) DEFAULT '0'::bpchar,
                                            "binding_time" timestamp(6) DEFAULT pg_systimestamp(),
                                            CONSTRAINT "pk_qxb_tenant_user" PRIMARY KEY ("tenant_id", "user_id")
)
    WITH (orientation=ROW)
    DISTRIBUTE BY HASH("user_id");

-- 核心索引
CREATE INDEX "idx_tenant_user_uid" ON "public"."qxb_tenant_user" ("user_id");

-- 表及字段全注释
COMMENT ON TABLE "public"."qxb_tenant_user" IS '机构与用户多对多关系表';
COMMENT ON COLUMN "public"."qxb_tenant_user"."tenant_id" IS '机构机构ID (0代表平台自营大厅/不入驻的独立专家大厅)';
COMMENT ON COLUMN "public"."qxb_tenant_user"."user_id" IS '关联用户主体表的user_id';
COMMENT ON COLUMN "public"."qxb_tenant_user"."dept_id" IS '用户在该机构下所属的部门/科室ID (承接原组织架构关系)';
COMMENT ON COLUMN "public"."qxb_tenant_user"."user_type" IS '用户在该机构下的身份类型 (01普通来访者/家长/孩子, 02机构咨询师, 03机构管理员)';
COMMENT ON COLUMN "public"."qxb_tenant_user"."tenant_status" IS '用户在该机构内的专属状态 (0正常 1冻结，在单机构被冻结不影响去其他机构业务流通)';
COMMENT ON COLUMN "public"."qxb_tenant_user"."binding_time" IS '用户与该机构首次发生业务关联/入驻的时间';


-- ==========================================
-- 表四：亲子绑定关系表 (qxb_parent_child)
-- ==========================================
CREATE TABLE "public"."qxb_parent_child" (
                                             "relation_id" int8 NOT NULL DEFAULT nextval('seq_qxb_parent_child_id'::regclass),
                                             "parent_user_id" int8 NOT NULL,
                                             "child_user_id" int8 NOT NULL,
                                             "relation_type" char(1) DEFAULT '1'::bpchar,
                                             "auth_status" char(1) DEFAULT '0'::bpchar,
                                             "bind_time" timestamp(6) DEFAULT NULL,
                                             "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                             CONSTRAINT "pk_qxb_parent_child" PRIMARY KEY ("relation_id")

)
    WITH (orientation=ROW)
    DISTRIBUTE BY HASH("parent_user_id");

-- 核心索引
CREATE UNIQUE INDEX "uk_parent_child" ON "public"."qxb_parent_child" ("parent_user_id", "child_user_id");
CREATE INDEX "idx_child_parent" ON "public"."qxb_parent_child" ("child_user_id");

-- 表及字段全注释
COMMENT ON TABLE "public"."qxb_parent_child" IS '亲子绑定关系表';
COMMENT ON COLUMN "public"."qxb_parent_child"."relation_id" IS '亲子绑定关系自增主键';
COMMENT ON COLUMN "public"."qxb_parent_child"."parent_user_id" IS '家长的用户唯一UID (关联qxb_user.user_id)';
COMMENT ON COLUMN "public"."qxb_parent_child"."child_user_id" IS '孩子的用户唯一UID (关联qxb_user.user_id)';
COMMENT ON COLUMN "public"."qxb_parent_child"."relation_type" IS '监护关系类型 (1父亲, 2母亲, 3其他法定监护人)';
COMMENT ON COLUMN "public"."qxb_parent_child"."auth_status" IS '绑定关系的审核/确认状态 (0待确认/审批中, 1已确认绑定成功, 2已拒绝解绑)';
COMMENT ON COLUMN "public"."qxb_parent_child"."bind_time" IS '亲子关系确认绑定的生效时间';
COMMENT ON COLUMN "public"."qxb_parent_child"."create_time" IS '家长发起绑定申请的时间';
