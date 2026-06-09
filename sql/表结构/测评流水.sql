-- 1. 创建测评记录模块专属的全局独立序列对象
CREATE SEQUENCE "public"."seq_eval_record_id" INCREMENT 1 START 100000;
CREATE SEQUENCE "public"."seq_eval_detail_id" INCREMENT 1 START 1000000;
CREATE SEQUENCE "public"."seq_eval_grant_id" INCREMENT 1 START 1;


-- =============================================================================
-- 表九：用户测评记录主表 (sys_eval_record) —— 即测评报告表
-- =============================================================================
CREATE TABLE "public"."sys_eval_record" (
                                            "id" int8 NOT NULL DEFAULT nextval('seq_eval_record_id'::regclass),
                                            "user_id" int8 NOT NULL,
                                            "scale_id" int8 NOT NULL,
                                            "create_tenant_id" int8 DEFAULT 0,
                                            "total_score" numeric(6,2) NOT NULL,
                                            "factor_scores" jsonb DEFAULT NULL,
                                            "crisis_level" char(1) DEFAULT '0'::bpchar,
                                            "level_name" varchar(20) NOT NULL,
                                            "report_content" text NOT NULL,
                                            "is_private" char(1) DEFAULT '1'::bpchar,
                                            "del_flag" char(1) DEFAULT '0'::bpchar,
                                            "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                            CONSTRAINT "pk_sys_eval_record" PRIMARY KEY ("id")
) WITH (orientation=ROW, storetype=ustore) DISTRIBUTE BY HASH("user_id"); -- 以user_id作为分布键

-- 黄金检索索引：用于极速捞取个人历史测评档案，支持多APP端（家长端/孩子端）的高频调取
CREATE INDEX "idx_eval_record_user_scale" ON "public"."sys_eval_record" ("user_id", "scale_id", "create_time");

-- 表及全字段全路径注释覆盖
COMMENT ON TABLE "public"."sys_eval_record" IS '用户测评记录主表 (即心理测评报告主表，属于绝对隐私资产)';
COMMENT ON COLUMN "public"."sys_eval_record"."id" IS '测评记录唯一自增ID (报告编号)';
COMMENT ON COLUMN "public"."sys_eval_record"."user_id" IS '测评人用户UID (关联sys_user.user_id，孩子做的就绑定孩子UID)';
COMMENT ON COLUMN "public"."sys_eval_record"."scale_id" IS '关联的测评量表主表ID (关联scale.id)';
COMMENT ON COLUMN "public"."sys_eval_record"."create_tenant_id" IS '溯源租户ID (仅记录用户在哪个机构界面做的题，绝不作为机构越权查询的默认依据)';
COMMENT ON COLUMN "public"."sys_eval_record"."total_score" IS '本次测评总得分/总均分';
COMMENT ON COLUMN "public"."sys_eval_record"."factor_scores" IS '多维度因子得分明细 (openGauss高性能JSONB类型，存储各因子的原始分、标准T分等，形如:{"躯体化":2.5,"焦虑":1.8})';
COMMENT ON COLUMN "public"."sys_eval_record"."crisis_level" IS '心理危机预警级别 (0正常无风险, 1轻度关注, 2中度预警, 3重度阴性危机)';
COMMENT ON COLUMN "public"."sys_eval_record"."level_name" IS '严重程度/危机标签名称 (对应常模匹配出的结论名称，如: 重度抑郁倾向)';
COMMENT ON COLUMN "public"."sys_eval_record"."report_content" IS '最终渲染生成的完整专业测评报告内容/指导建议 (支持长文本或富文本)';
COMMENT ON COLUMN "public"."sys_eval_record"."is_private" IS '绝对私密开关 (1-绝对私密仅自己和监护人可见, 0-公开/全网授权)';
COMMENT ON COLUMN "public"."sys_eval_record"."del_flag" IS '逻辑删除标志 (0存在 2删除)';
COMMENT ON COLUMN "public"."sys_eval_record"."create_time" IS '提交答卷并生成报告的时间';


-- =============================================================================
-- 表十：用户答卷选项明细流水表 (sys_eval_detail)
-- =============================================================================
CREATE TABLE "public"."sys_eval_detail" (
                                            "id" int8 NOT NULL DEFAULT nextval('seq_eval_detail_id'::regclass),
                                            "record_id" int8 NOT NULL,
                                            "user_id" int8 NOT NULL,
                                            "question_id" int8 NOT NULL,
                                            "option_id" int8 NOT NULL,
                                            "score" numeric(5,2) NOT NULL,
                                            "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                            CONSTRAINT "pk_sys_eval_detail" PRIMARY KEY ("id")
) WITH (orientation=ROW) DISTRIBUTE BY HASH("record_id"); -- 以报告ID作为分布键，让单次交卷的所有明细死死聚合

-- 核心联合索引：用于在查看报告详情时，一键拉出这 90 道题用户分别勾选了什么，支持错题/答卷还原
CREATE UNIQUE INDEX "uk_eval_detail_lookup" ON "public"."sys_eval_detail" ("record_id", "question_id");

-- 表及全字段全路径注释覆盖
COMMENT ON TABLE "public"."sys_eval_detail" IS '用户答卷选项明细流水表 (记录每一道题的真实勾选轨迹，留存诊断证据)';
COMMENT ON COLUMN "public"."sys_eval_detail"."id" IS '流水明细唯一自增ID';
COMMENT ON COLUMN "public"."sys_eval_detail"."record_id" IS '关联的测评记录主表ID (关联sys_eval_record.id)';
COMMENT ON COLUMN "public"."sys_eval_detail"."user_id" IS '测评人用户UID (用于冗余加速，方便全网分析单人答卷多样本比对)';
COMMENT ON COLUMN "public"."sys_eval_detail"."question_id" IS '关联的题目ID (关联scale_question.id)';
COMMENT ON COLUMN "public"."sys_eval_detail"."option_id" IS '关联的用户勾选的选项ID (关联scale_question_option.id)';
COMMENT ON COLUMN "public"."sys_eval_detail"."score" IS '用户勾选该选项时的实际计分权重 (留存当时分值快照，防止后台改动选项分导致历史历史算分错乱)';
COMMENT ON COLUMN "public"."sys_eval_detail"."create_time" IS '答题写入时间';


-- =============================================================================
-- 表十一：测评数据动态授权表 (sys_eval_grant)
-- =============================================================================
CREATE TABLE "public"."sys_eval_grant" (
                                           "id" int8 NOT NULL DEFAULT nextval('seq_eval_grant_id'::regclass),
                                           "eval_record_id" int8 NOT NULL,
                                           "user_id" int8 NOT NULL,
                                           "grant_to_type" char(1) NOT NULL,
                                           "grant_to_id" int8 NOT NULL,
                                           "expire_time" timestamp(6) NOT NULL,
                                           "status" char(1) DEFAULT '0'::bpchar,
                                           "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                           CONSTRAINT "pk_sys_eval_grant" PRIMARY KEY ("id")
) WITH (orientation=ROW) DISTRIBUTE BY HASH("grant_to_id");

-- 黄金鉴权复合索引：咨询师点击查看报告详情时，后端框架强行在此索引执行 O(1) 秒级越权校验
CREATE INDEX "idx_grant_auth_check" ON "public"."sys_eval_grant" ("grant_to_id", "eval_record_id", "expire_time", "status");

-- 表及全字段全路径注释覆盖
COMMENT ON TABLE "public"."sys_eval_grant" IS '测评数据动态授权表 (心理隐私底层最高的防御铁门，控制咨询师/机构限时查看权)';
COMMENT ON COLUMN "public"."sys_eval_grant"."id" IS '授权流水自增ID';
COMMENT ON COLUMN "public"."sys_eval_grant"."eval_record_id" IS '关联的那份具体的隐私测评报告ID (关联sys_eval_record.id)';
COMMENT ON COLUMN "public"."sys_eval_grant"."user_id" IS '授权发起人UID (即来访者/孩子本人UID)';
COMMENT ON COLUMN "public"."sys_eval_grant"."grant_to_type" IS '授信对象类型 (1-特定咨询师用户UID, 2-特定机构租户ID)';
COMMENT ON COLUMN "public"."sys_eval_grant"."grant_to_id" IS '具体的被授权人ID/机构ID (对应上个字段的身份标识)';
COMMENT ON COLUMN "public"."sys_eval_grant"."expire_time" IS '授权截止过期时间 (心理咨询讲究时效隐私，过期后咨询师后台自动收回权限，无法再次窥探)';
COMMENT ON COLUMN "public"."sys_eval_grant"."status" IS '授权状态 (0-生效中, 1-用户主动提早撤销, 2-已被后台管理员强行熔断)';
COMMENT ON COLUMN "public"."sys_eval_grant"."create_time" IS '用户在小程序端点击授权并同意的精确时间';
