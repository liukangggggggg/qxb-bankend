

-- ==========================================
-- 表五：角色信息表 (sys_role)
-- ==========================================
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
    WITH (orientation=ROW, storetype=ustore)
    DISTRIBUTE BY HASH("role_id");

-- 核心索引
CREATE UNIQUE INDEX "uk_sys_role_key" ON "public"."sys_role" ("role_key");

-- 表及字段全注释
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
    WITH (orientation=ROW)
    DISTRIBUTE BY HASH("user_id");

-- 核心索引
CREATE INDEX "idx_sys_user_role_rid" ON "public"."sys_user_role" ("role_id");

-- 表及字段全注释
COMMENT ON TABLE "public"."sys_user_role" IS '用户和角色关联表';
COMMENT ON COLUMN "public"."sys_user_role"."user_id" IS '用户ID（关联sys_user.user_id）';
COMMENT ON COLUMN "public"."sys_user_role"."role_id" IS '角色ID（关联sys_role.role_id）';
