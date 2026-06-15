-- 1. 创建全局序列
CREATE SEQUENCE "public"."seq_sys_user_id" INCREMENT 1 START 10000;
CREATE SEQUENCE "public"."seq_sys_user_auth_id" INCREMENT 1 START 1;
CREATE SEQUENCE "public"."seq_biz_parent_child_relation_id" INCREMENT 1 START 1;
CREATE SEQUENCE "public"."seq_counselor_profile_id" INCREMENT 1 START 1;
CREATE SEQUENCE "public"."seq_counselor_qualification_id" INCREMENT 1 START 1;


-- ==========================================
-- 表一：用户核心主体表 (sys_user)
-- ==========================================
CREATE TABLE "public"."sys_user" (
                                     "user_id" int8 NOT NULL DEFAULT nextval('seq_sys_user_id'::regclass),
                                     "nick_name" varchar(30) NOT NULL,
                                     "real_name" varchar(30) DEFAULT ''::character varying,
                                     "avatar" varchar(255) DEFAULT ''::character varying,
                                     "email" varchar(50) DEFAULT ''::character varying,
                                     "city" varchar(20) DEFAULT ''::character varying,
                                     "sex" char(1) DEFAULT '0'::bpchar,
                                     "birthday" date DEFAULT NULL,
                                     "status" char(1) DEFAULT '0'::bpchar,
                                     "del_flag" char(1) DEFAULT '0'::bpchar,
                                     "ext_info" jsonb DEFAULT NULL,
                                     "create_by" varchar(64) DEFAULT ''::character varying,
                                     "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                     "update_by" varchar(64) DEFAULT ''::character varying,
                                     "update_time" timestamp(6) DEFAULT NULL,
                                     "remark" varchar(500) DEFAULT NULL::character varying,
                                     CONSTRAINT "pk_sys_user" PRIMARY KEY ("user_id")
)
    WITH (orientation=ROW);

-- 表及字段全注释
COMMENT ON TABLE "public"."sys_user" IS '用户核心主体表';
COMMENT ON COLUMN "public"."sys_user"."user_id" IS '全局唯一用户ID (UID底座，全系统业务追踪核心)';
COMMENT ON COLUMN "public"."sys_user"."nick_name" IS '用户昵称';
COMMENT ON COLUMN "public"."sys_user"."real_name" IS '真实姓名 (高频查询字段，实名认证后写入)';
COMMENT ON COLUMN "public"."sys_user"."avatar" IS '用户头像URL地址 (预留255位以兼容多端长URL)';
COMMENT ON COLUMN "public"."sys_user"."email" IS '电子邮箱 (用于找回密码、通知推送)';
COMMENT ON COLUMN "public"."sys_user"."city" IS '所在城市 (咨询师检索、本地服务推荐的核心筛选条件)';
COMMENT ON COLUMN "public"."sys_user"."sex" IS '用户性别 (0男 1女 2未知)';
COMMENT ON COLUMN "public"."sys_user"."birthday" IS '出生日期 (用于在心理咨询和量表测评中动态计算精确年龄段)';
COMMENT ON COLUMN "public"."sys_user"."status" IS '全局账号状态 (0正常 1停用，停用后全端、全机构均无法登录)';
COMMENT ON COLUMN "public"."sys_user"."del_flag" IS '逻辑删除标志 (0代表存在 2代表删除)';
COMMENT ON COLUMN "public"."sys_user"."ext_info" IS '扩展属性JSON (低频杂项字段兜底，如mailing_address、id_no等，键值可自由扩展)';
COMMENT ON COLUMN "public"."sys_user"."create_by" IS '创建者账号/ID';
COMMENT ON COLUMN "public"."sys_user"."create_time" IS '账号创建时间';
COMMENT ON COLUMN "public"."sys_user"."update_by" IS '更新者账号/ID';
COMMENT ON COLUMN "public"."sys_user"."update_time" IS '最后一次资料更新时间';
COMMENT ON COLUMN "public"."sys_user"."remark" IS '备注信息';


-- ==========================================
-- 表二：多端登录认证表 (sys_user_auth)
-- ==========================================
CREATE TABLE "public"."sys_user_auth" (
                                          "auth_id" int8 NOT NULL DEFAULT nextval('seq_sys_user_auth_id'::regclass),
                                          "user_id" int8 NOT NULL,
                                          "identity_type" varchar(20) NOT NULL,
                                          "identifier" varchar(100) NOT NULL,
                                          "union_id" varchar(100) DEFAULT NULL::character varying,
                                          "credential" varchar(255) DEFAULT ''::character varying,
                                          "login_ip" varchar(128) DEFAULT ''::character varying,
                                          "login_date" timestamp(6) DEFAULT NULL,
                                          "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                          CONSTRAINT "pk_sys_user_auth" PRIMARY KEY ("auth_id")
)
    WITH (orientation=ROW);

-- 核心索引
CREATE UNIQUE INDEX "uk_auth_identity" ON "public"."sys_user_auth" ("identity_type", "identifier");
CREATE INDEX "idx_auth_union_id" ON "public"."sys_user_auth" ("union_id");
CREATE INDEX "idx_auth_user_id" ON "public"."sys_user_auth" ("user_id");

-- 表及字段全注释
COMMENT ON TABLE "public"."sys_user_auth" IS '多端登录凭证表';
COMMENT ON COLUMN "public"."sys_user_auth"."auth_id" IS '凭证记录自增ID';
COMMENT ON COLUMN "public"."sys_user_auth"."user_id" IS '关联用户主体表的user_id';
COMMENT ON COLUMN "public"."sys_user_auth"."identity_type" IS '认证登录渠道类型 (password:账号密码, phone:手机快捷验证码, wechat_mp:微信小程序)';
COMMENT ON COLUMN "public"."sys_user_auth"."identifier" IS '唯一通道标识 (对应类型分别存储:用户名、手机号、微信OpenID)';
COMMENT ON COLUMN "public"."sys_user_auth"."union_id" IS '微信开放平台唯一标识 (多端融合、跨端换绑防重复建户的核心依据)';
COMMENT ON COLUMN "public"."sys_user_auth"."credential" IS '凭证内容 (如密码哈希密文，微信或验证码免密登录时保持为NULL)';
COMMENT ON COLUMN "public"."sys_user_auth"."login_ip" IS '该通道最后一次登录的客户端IP地址';
COMMENT ON COLUMN "public"."sys_user_auth"."login_date" IS '该通道最后一次登录的精确时间';
COMMENT ON COLUMN "public"."sys_user_auth"."create_time" IS '该登录通道的绑定创建时间';


-- ==========================================
-- 表三：机构用户关系表 (sys_tenant_user)
-- ==========================================
CREATE TABLE "public"."sys_tenant_user" (
                                            "tenant_id" int8 NOT NULL,
                                            "user_id" int8 NOT NULL,
                                            "dept_id" int8 DEFAULT NULL,
                                            "user_type" varchar(2) DEFAULT '01'::character varying,
                                            "tenant_status" char(1) DEFAULT '0'::bpchar,
                                            "binding_time" timestamp(6) DEFAULT pg_systimestamp(),
                                            CONSTRAINT "pk_sys_tenant_user" PRIMARY KEY ("tenant_id", "user_id")
)
    WITH (orientation=ROW);

-- 核心索引
CREATE INDEX "idx_tenant_user_uid" ON "public"."sys_tenant_user" ("user_id");

-- 表及字段全注释
COMMENT ON TABLE "public"."sys_tenant_user" IS '机构与用户多对多关系表';
COMMENT ON COLUMN "public"."sys_tenant_user"."tenant_id" IS '机构机构ID (0代表平台自营大厅/不入驻的独立专家大厅)';
COMMENT ON COLUMN "public"."sys_tenant_user"."user_id" IS '关联用户主体表的user_id';
COMMENT ON COLUMN "public"."sys_tenant_user"."dept_id" IS '用户在该机构下所属的部门/科室ID (承接原组织架构关系)';
COMMENT ON COLUMN "public"."sys_tenant_user"."user_type" IS '用户在该机构下的身份类型 (01普通来访者/家长/孩子, 02机构咨询师, 03机构管理员)';
COMMENT ON COLUMN "public"."sys_tenant_user"."tenant_status" IS '用户在该机构内的专属状态 (0正常 1冻结，在单机构被冻结不影响去其他机构业务流通)';
COMMENT ON COLUMN "public"."sys_tenant_user"."binding_time" IS '用户与该机构首次发生业务关联/入驻的时间';


-- ==========================================
-- 表四：亲子绑定关系表 (biz_parent_child_relation)
-- ==========================================
CREATE TABLE "public"."biz_parent_child_relation" (
                                                  "relation_id" int8 NOT NULL DEFAULT nextval('seq_biz_parent_child_relation_id'::regclass),
                                                  "parent_user_id" int8 NOT NULL,
                                                  "child_user_id" int8 NOT NULL,
                                                  "relation_type" char(1) DEFAULT '1'::bpchar,
                                                  "auth_status" char(1) DEFAULT '0'::bpchar,
                                                  "bind_time" timestamp(6) DEFAULT NULL,
                                                  "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                                  CONSTRAINT "pk_biz_parent_child_relation" PRIMARY KEY ("relation_id")
)
    WITH (orientation=ROW);

-- 核心索引
CREATE UNIQUE INDEX "uk_parent_child" ON "public"."biz_parent_child_relation" ("parent_user_id", "child_user_id");
CREATE INDEX "idx_child_parent" ON "public"."biz_parent_child_relation" ("child_user_id");

-- 表及字段全注释
COMMENT ON TABLE "public"."biz_parent_child_relation" IS '亲子绑定关系表';
COMMENT ON COLUMN "public"."biz_parent_child_relation"."relation_id" IS '亲子绑定关系自增主键';
COMMENT ON COLUMN "public"."biz_parent_child_relation"."parent_user_id" IS '家长的用户唯一UID (关联sys_user.user_id)';
COMMENT ON COLUMN "public"."biz_parent_child_relation"."child_user_id" IS '孩子的用户唯一UID (关联sys_user.user_id)';
COMMENT ON COLUMN "public"."biz_parent_child_relation"."relation_type" IS '监护关系类型 (1父亲, 2母亲, 3其他法定监护人)';
COMMENT ON COLUMN "public"."biz_parent_child_relation"."auth_status" IS '绑定关系的审核/确认状态 (0待确认/审批中, 1已确认绑定成功, 2已拒绝解绑)';
COMMENT ON COLUMN "public"."biz_parent_child_relation"."bind_time" IS '亲子关系确认绑定的生效时间';
COMMENT ON COLUMN "public"."biz_parent_child_relation"."create_time" IS '家长发起绑定申请的时间';


-- ==========================================
-- 表五：角色信息表 (sys_role)
-- ==========================================
CREATE SEQUENCE "public"."seq_sys_role_id" INCREMENT 1 START 100;

CREATE TABLE "public"."sys_role" (
                                     "role_id" int8 NOT NULL DEFAULT nextval('seq_sys_role_id'::regclass),
                                     "role_name" varchar(30) NOT NULL,
                                     "role_key" varchar(100) NOT NULL,
                                     "role_sort" int4 NOT NULL,
                                     "data_scope" char(1) DEFAULT '1'::bpchar,
                                     "menu_check_strictly" int2 DEFAULT 1,
                                     "dept_check_strictly" int2 DEFAULT 1,
                                     "status" char(1) NOT NULL,
                                     "del_flag" char(1) DEFAULT '0'::bpchar,
                                     "create_by" varchar(64) DEFAULT ''::character varying,
                                     "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                     "update_by" varchar(64) DEFAULT ''::character varying,
                                     "update_time" timestamp(6) DEFAULT NULL,
                                     "remark" varchar(500) DEFAULT NULL::character varying,
                                     CONSTRAINT "pk_sys_role" PRIMARY KEY ("role_id")
)
    WITH (orientation=ROW);

CREATE UNIQUE INDEX "uk_sys_role_key" ON "public"."sys_role" ("role_key");

COMMENT ON TABLE "public"."sys_role" IS '角色信息表';
COMMENT ON COLUMN "public"."sys_role"."role_id" IS '角色ID';
COMMENT ON COLUMN "public"."sys_role"."role_name" IS '角色名称';
COMMENT ON COLUMN "public"."sys_role"."role_key" IS '角色权限字符串';
COMMENT ON COLUMN "public"."sys_role"."role_sort" IS '显示顺序';
COMMENT ON COLUMN "public"."sys_role"."data_scope" IS '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）';
COMMENT ON COLUMN "public"."sys_role"."menu_check_strictly" IS '菜单树选择项是否关联显示';
COMMENT ON COLUMN "public"."sys_role"."dept_check_strictly" IS '部门树选择项是否关联显示';
COMMENT ON COLUMN "public"."sys_role"."status" IS '角色状态（0正常 1停用）';
COMMENT ON COLUMN "public"."sys_role"."del_flag" IS '删除标志（0代表存在 2代表删除）';
COMMENT ON COLUMN "public"."sys_role"."create_by" IS '创建者';
COMMENT ON COLUMN "public"."sys_role"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."sys_role"."update_by" IS '更新者';
COMMENT ON COLUMN "public"."sys_role"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."sys_role"."remark" IS '备注';


-- ==========================================
-- 表六：用户和角色关联表 (sys_user_role)
-- ==========================================
CREATE TABLE "public"."sys_user_role" (
                                          "user_id" int8 NOT NULL,
                                          "role_id" int8 NOT NULL,
                                          CONSTRAINT "pk_sys_user_role" PRIMARY KEY ("user_id", "role_id")
)
    WITH (orientation=ROW);

CREATE INDEX "idx_sys_user_role_rid" ON "public"."sys_user_role" ("role_id");

COMMENT ON TABLE "public"."sys_user_role" IS '用户和角色关联表';
COMMENT ON COLUMN "public"."sys_user_role"."user_id" IS '用户ID（关联sys_user.user_id）';
COMMENT ON COLUMN "public"."sys_user_role"."role_id" IS '角色ID（关联sys_role.role_id）';


-- ==========================================
-- 表七：学生信息表 (edu_student_info)
-- ==========================================
CREATE SEQUENCE "public"."seq_sys_student_id" INCREMENT 1 START 1;

CREATE TABLE "public"."edu_student_info" (
                                             "id" int8 NOT NULL DEFAULT nextval('seq_sys_student_id'::regclass),
                                             "user_id" int8 NOT NULL,
                                             "student_no" varchar(30) NOT NULL,
                                             "class_name" varchar(50) DEFAULT ''::character varying,
                                             "grade" varchar(20) DEFAULT ''::character varying,
                                             "school_name" varchar(100) DEFAULT ''::character varying,
                                             "binding_code" varchar(6) NOT NULL,
                                             "bind_status" char(1) DEFAULT '0'::bpchar,
                                             "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                             CONSTRAINT "pk_edu_student_info" PRIMARY KEY ("id")
)
    WITH (orientation=ROW);

CREATE UNIQUE INDEX "uk_student_no" ON "public"."edu_student_info" ("student_no");
CREATE INDEX "idx_student_user" ON "public"."edu_student_info" ("user_id");

COMMENT ON TABLE "public"."edu_student_info" IS '学生信息表（绑定码认领模式）';
COMMENT ON COLUMN "public"."edu_student_info"."id" IS '自增主键';
COMMENT ON COLUMN "public"."edu_student_info"."user_id" IS '关联sys_user.user_id';
COMMENT ON COLUMN "public"."edu_student_info"."student_no" IS '学号（唯一标识）';
COMMENT ON COLUMN "public"."edu_student_info"."class_name" IS '班级名称';
COMMENT ON COLUMN "public"."edu_student_info"."grade" IS '年级';
COMMENT ON COLUMN "public"."edu_student_info"."school_name" IS '学校名称';
COMMENT ON COLUMN "public"."edu_student_info"."binding_code" IS '6位绑定码（家长认领时校验）';
COMMENT ON COLUMN "public"."edu_student_info"."bind_status" IS '绑定状态（0未绑定 1已绑定）';
COMMENT ON COLUMN "public"."edu_student_info"."create_time" IS '创建时间';


-- ==========================================
-- 表八：咨询师档案表 (biz_counselor_profile)
-- ==========================================
-- 领域专属表：只有咨询师身份的用户的专业技能信息
CREATE TABLE "public"."biz_counselor_profile" (
                                                  "id" int8 NOT NULL DEFAULT nextval('seq_counselor_profile_id'::regclass),
                                                  "user_id" int8 NOT NULL,
                                                  "personal_title" varchar(100) DEFAULT ''::character varying,
                                                  "introduction" text DEFAULT NULL,
                                                  "signature" text DEFAULT NULL,
                                                  "education" varchar(32) DEFAULT ''::character varying,
                                                  "level" varchar(32) DEFAULT ''::character varying,
                                                  "is_supervisor" char(1) DEFAULT '0'::bpchar,
                                                  "start_date" date DEFAULT NULL,
                                                  "work_time" varchar(255) DEFAULT ''::character varying,
                                                  "methods" varchar(100) DEFAULT ''::character varying,
                                                  "price" numeric(10,4) DEFAULT NULL,
                                                  "consulation_addr" varchar(200) DEFAULT ''::character varying,
                                                  "time_base" varchar(200) DEFAULT ''::character varying,
                                                  "is_accept_order" int2 DEFAULT 0,
                                                  "public_consultation" varchar(10) DEFAULT ''::character varying,
                                                  "counselor_pepole_count" varchar(20) DEFAULT '0',
                                                  "sort_index" int4 DEFAULT 0,
                                                  "is_top" int2 DEFAULT 0,
                                                  "activity_priority" varchar(100) DEFAULT ''::character varying,
                                                  "coupon_ids" varchar(50) DEFAULT ''::character varying,
                                                  "vip_exclusive" int2 DEFAULT 0,
                                                  "data_labels" varchar(50) DEFAULT ''::character varying,
                                                  "referee" varchar(255) DEFAULT ''::character varying,
                                                  "tags" text DEFAULT NULL,
                                                  "wx_qr_code" text DEFAULT NULL,
                                                  "app_qr_code" text DEFAULT NULL,
                                                  "status" varchar(32) DEFAULT '0'::character varying,
                                                  "creator" varchar(64) DEFAULT ''::character varying,
                                                  "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                                  "updater" varchar(64) DEFAULT ''::character varying,
                                                  "update_time" timestamp(6) DEFAULT NULL,
                                                  "deleted" char(1) DEFAULT '0'::bpchar,
                                                  CONSTRAINT "pk_counselor_profile" PRIMARY KEY ("id")
)
    WITH (orientation=ROW);

CREATE UNIQUE INDEX "uk_counselor_profile_uid" ON "public"."biz_counselor_profile" ("user_id");

COMMENT ON TABLE "public"."biz_counselor_profile" IS '咨询师档案表（领域专属，仅咨询师身份用户有记录）';
COMMENT ON COLUMN "public"."biz_counselor_profile"."id" IS '自增主键';
COMMENT ON COLUMN "public"."biz_counselor_profile"."user_id" IS '关联sys_user.user_id';
COMMENT ON COLUMN "public"."biz_counselor_profile"."personal_title" IS '个人头衔（如国家二级心理咨询师）';
COMMENT ON COLUMN "public"."biz_counselor_profile"."introduction" IS '个人介绍（富文本）';
COMMENT ON COLUMN "public"."biz_counselor_profile"."signature" IS '个人签名（短签名）';
COMMENT ON COLUMN "public"."biz_counselor_profile"."education" IS '最高学历';
COMMENT ON COLUMN "public"."biz_counselor_profile"."level" IS '咨询师等级';
COMMENT ON COLUMN "public"."biz_counselor_profile"."is_supervisor" IS '是否具备督导资质（0否 1是）';
COMMENT ON COLUMN "public"."biz_counselor_profile"."start_date" IS '从业开始时间';
COMMENT ON COLUMN "public"."biz_counselor_profile"."work_time" IS '可工作时间段';
COMMENT ON COLUMN "public"."biz_counselor_profile"."methods" IS '咨询方式（逗号分隔：文字/语音/视频/面对面）';
COMMENT ON COLUMN "public"."biz_counselor_profile"."price" IS '咨询价格';
COMMENT ON COLUMN "public"."biz_counselor_profile"."consulation_addr" IS '面对面咨询地址';
COMMENT ON COLUMN "public"."biz_counselor_profile"."time_base" IS '咨询时间基数（如50分钟/次）';
COMMENT ON COLUMN "public"."biz_counselor_profile"."is_accept_order" IS '是否接单（0否 1是）';
COMMENT ON COLUMN "public"."biz_counselor_profile"."public_consultation" IS '公益咨询类型（0精品推荐 1公益咨询 2自营咨询师 3半价咨询）';
COMMENT ON COLUMN "public"."biz_counselor_profile"."counselor_pepole_count" IS '累计咨询人数';
COMMENT ON COLUMN "public"."biz_counselor_profile"."sort_index" IS '排序权重';
COMMENT ON COLUMN "public"."biz_counselor_profile"."is_top" IS '是否置顶';
COMMENT ON COLUMN "public"."biz_counselor_profile"."activity_priority" IS '活动优先级';
COMMENT ON COLUMN "public"."biz_counselor_profile"."coupon_ids" IS '赠送优惠券ID列表（逗号分隔）';
COMMENT ON COLUMN "public"."biz_counselor_profile"."vip_exclusive" IS '是否会员专属';
COMMENT ON COLUMN "public"."biz_counselor_profile"."data_labels" IS '数据标签ID（逗号分隔）';
COMMENT ON COLUMN "public"."biz_counselor_profile"."referee" IS '推荐人';
COMMENT ON COLUMN "public"."biz_counselor_profile"."tags" IS '自定义标签';
COMMENT ON COLUMN "public"."biz_counselor_profile"."wx_qr_code" IS '微信二维码图片URL';
COMMENT ON COLUMN "public"."biz_counselor_profile"."app_qr_code" IS 'APP推广二维码URL';
COMMENT ON COLUMN "public"."biz_counselor_profile"."status" IS '上架状态（0草稿 1已上架 2已下架）；审核通过/驳回记录走biz_audit_log';
COMMENT ON COLUMN "public"."biz_counselor_profile"."creator" IS '创建者';
COMMENT ON COLUMN "public"."biz_counselor_profile"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."biz_counselor_profile"."updater" IS '更新者';
COMMENT ON COLUMN "public"."biz_counselor_profile"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."biz_counselor_profile"."deleted" IS '逻辑删除（0存在 1删除）';


-- ==========================================
-- 表九：咨询师资格认证表 (biz_counselor_qualification)
-- ==========================================
CREATE TABLE "public"."biz_counselor_qualification" (
                                                        "id" int8 NOT NULL DEFAULT nextval('seq_counselor_qualification_id'::regclass),
                                                        "user_id" int8 NOT NULL,
                                                        "qualification_name" varchar(100) NOT NULL,
                                                        "issuing_authority" varchar(100) DEFAULT ''::character varying,
                                                        "cert_no" varchar(64) DEFAULT ''::character varying,
                                                        "obtain_date" date DEFAULT NULL,
                                                        "expire_date" date DEFAULT NULL,
                                                        "cert_image" varchar(255) DEFAULT ''::character varying,
                                                        "verify_status" char(1) DEFAULT '0'::bpchar,
                                                        "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                                        "update_time" timestamp(6) DEFAULT NULL,
                                                        "deleted" char(1) DEFAULT '0'::bpchar,
                                                        CONSTRAINT "pk_counselor_qualification" PRIMARY KEY ("id")
)
    WITH (orientation=ROW);

CREATE INDEX "idx_qualification_uid" ON "public"."biz_counselor_qualification" ("user_id");

COMMENT ON TABLE "public"."biz_counselor_qualification" IS '咨询师资格认证表（一个咨询师可有多张证书）';
COMMENT ON COLUMN "public"."biz_counselor_qualification"."id" IS '自增主键';
COMMENT ON COLUMN "public"."biz_counselor_qualification"."user_id" IS '关联sys_user.user_id';
COMMENT ON COLUMN "public"."biz_counselor_qualification"."qualification_name" IS '证书名称（如国家三级心理咨询师、沙盘治疗师）';
COMMENT ON COLUMN "public"."biz_counselor_qualification"."issuing_authority" IS '发证机构（如人力资源和社会保障部）';
COMMENT ON COLUMN "public"."biz_counselor_qualification"."cert_no" IS '证书编号';
COMMENT ON COLUMN "public"."biz_counselor_qualification"."obtain_date" IS '获得日期';
COMMENT ON COLUMN "public"."biz_counselor_qualification"."expire_date" IS '有效期至';
COMMENT ON COLUMN "public"."biz_counselor_qualification"."cert_image" IS '证书照片/扫描件URL';
COMMENT ON COLUMN "public"."biz_counselor_qualification"."verify_status" IS '审核状态（0待审核 1已通过 2已驳回）';
COMMENT ON COLUMN "public"."biz_counselor_qualification"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."biz_counselor_qualification"."update_time" IS '更新时间';
COMMENT ON COLUMN "public"."biz_counselor_qualification"."deleted" IS '逻辑删除（0存在 1删除）';