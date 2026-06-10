-- =============================================================================
-- 一、 创建量表模块专属的独立序列对象 (用于高性能发号，消灭表级锁)
-- =============================================================================
CREATE SEQUENCE "public"."seq_scale_id" INCREMENT 1 START 1000;
CREATE SEQUENCE "public"."seq_scale_factor_id" INCREMENT 1 START 1000;
CREATE SEQUENCE "public"."seq_scale_factor_rel_id" INCREMENT 1 START 1;
CREATE SEQUENCE "public"."seq_scale_question_id" INCREMENT 1 START 10000;
CREATE SEQUENCE "public"."seq_scale_option_id" INCREMENT 1 START 50000;
CREATE SEQUENCE "public"."seq_scale_norm_id" INCREMENT 1 START 1000;


-- =============================================================================
-- 二、 测评量表与专业题库常模层 (6张核心业务表 DDL)
-- =============================================================================

-- 1. 测评量表主表 (scale)
CREATE TABLE "public"."scale" (
                                  "id" int8 NOT NULL DEFAULT nextval('seq_scale_id'::regclass),
                                  "title" varchar(100) NOT NULL,
                                  "thumbnail" varchar(255) DEFAULT ''::character varying,
                                  "banner" varchar(255) DEFAULT ''::character varying,
                                  "description" text,
                                  "detail_content" text,
                                  "tips" varchar(500) DEFAULT ''::character varying,
                                  "status" char(1) DEFAULT '1'::bpchar,
                                  "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                  CONSTRAINT "pk_scale" PRIMARY KEY ("id")
) WITH (orientation=ROW) DISTRIBUTE BY HASH("id");

COMMENT ON TABLE "public"."scale" IS '测评量表主表';
COMMENT ON COLUMN "public"."scale"."id" IS '量表唯一自增ID';
COMMENT ON COLUMN "public"."scale"."title" IS '量表名称 (如: SCL-90症状自评量表)';
COMMENT ON COLUMN "public"."scale"."thumbnail" IS '量表缩略图URL (用于小程序列表展示)';
COMMENT ON COLUMN "public"."scale"."banner" IS '量表广告图/头图URL (用于详情页顶部Banner展示)';
COMMENT ON COLUMN "public"."scale"."description" IS '简要描述/指导语 (用户点进测评前的简短介绍)';
COMMENT ON COLUMN "public"."scale"."detail_content" IS '详细描述 (支持富文本或长文本，介绍背景、专业度等)';
COMMENT ON COLUMN "public"."scale"."tips" IS '测试提示信息 (如: 本测试大约需要20分钟，请在安静环境下作答)';
COMMENT ON COLUMN "public"."scale"."status" IS '上架运营状态 (0下架/停用, 1上架/启用)';
COMMENT ON COLUMN "public"."scale"."create_time" IS '量表创建时间';


-- 2. 公共因子表 (scale_factor)
CREATE TABLE "public"."scale_factor" (
                                         "id" int8 NOT NULL DEFAULT nextval('seq_scale_factor_id'::regclass),
                                         "parent_id" int8 DEFAULT 0,
                                         "name" varchar(50) NOT NULL,
                                         "code" varchar(10) DEFAULT ''::character varying,
                                         "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                         CONSTRAINT "pk_scale_factor" PRIMARY KEY ("id")
) WITH (orientation=ROW) DISTRIBUTE BY HASH("scale_id");

CREATE INDEX "idx_factor_parent_id" ON "public"."scale_factor" ("parent_id");
CREATE INDEX "idx_factor_scale_id" ON "public"."scale_factor" ("scale_id");

COMMENT ON TABLE "public"."scale_factor" IS '公共因子维度表 (支持树形大因子/小因子结构)';
COMMENT ON COLUMN "public"."scale_factor"."id" IS '因子维度唯一自增ID';
COMMENT ON COLUMN "public"."scale_factor"."parent_id" IS '上级因子ID (0表示顶层大因子，非0表示下属子维度)';
COMMENT ON COLUMN "public"."scale_factor"."name" IS '因子名称 (如: 躯体化、人际关系敏感、焦虑倾向)';
COMMENT ON COLUMN "public"."scale_factor"."code" IS '因子代码/缩写 (用于报告公式计算，如: som, anx)';
COMMENT ON COLUMN "public"."scale_factor"."create_time" IS '创建时间';


-- 3. 量表与因子多对多关联表 (scale_factor_rel)
CREATE TABLE "public"."scale_factor_rel" (
                                             "id" int8 NOT NULL DEFAULT nextval('seq_scale_factor_rel_id'::regclass),
                                             "scale_id" int8 NOT NULL,
                                             "factor_id" int8 NOT NULL,
                                             "is_marketing_display" char(1) DEFAULT '0'::bpchar,
                                             "sort_order" int4 DEFAULT 0,
                                             CONSTRAINT "pk_scale_factor_rel" PRIMARY KEY ("id")
) WITH (orientation=ROW) DISTRIBUTE BY HASH("scale_id");

CREATE UNIQUE INDEX "uk_scale_factor_link" ON "public"."scale_factor_rel" ("scale_id", "factor_id", "is_marketing_display");

COMMENT ON TABLE "public"."scale_factor_rel" IS '量表与因子多对多关联表 (实现核心算分因子与前端营销亮点标签的完美隔离)';
COMMENT ON COLUMN "public"."scale_factor_rel"."id" IS '关联记录唯一自增ID';
COMMENT ON COLUMN "public"."scale_factor_rel"."scale_id" IS '关联的量表主表ID';
COMMENT ON COLUMN "public"."scale_factor_rel"."factor_id" IS '关联的公共因子维度ID';
COMMENT ON COLUMN "public"."scale_factor_rel"."is_marketing_display" IS '营销展示开关 (0-纯算分因子/在前端卡片隐藏, 1-量表亮点核心维度/在前端卡片亮点展示)';
COMMENT ON COLUMN "public"."scale_factor_rel"."sort_order" IS '营销标签在前端卡片上的展示先后顺序 (从小到大排列)';


-- 4. 量表题目表 (scale_question)
CREATE TABLE "public"."scale_question" (
                                           "id" int8 NOT NULL DEFAULT nextval('seq_scale_question_id'::regclass),
                                           "scale_id" int8 NOT NULL,
                                           "factor_id" int8 DEFAULT NULL,
                                           "sort_order" int4 DEFAULT 0,
                                           "content" text NOT NULL,
                                           "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                           CONSTRAINT "pk_scale_question" PRIMARY KEY ("id")
) WITH (orientation=ROW, storetype=ustore) DISTRIBUTE BY HASH("scale_id");

CREATE INDEX "idx_question_scale_lookup" ON "public"."scale_question" ("scale_id", "sort_order");
CREATE INDEX "idx_question_factor_id" ON "public"."scale_question" ("factor_id");

COMMENT ON TABLE "public"."scale_question" IS '量表题目表';
COMMENT ON COLUMN "public"."scale_question"."id" IS '题目唯一自增ID';
COMMENT ON COLUMN "public"."scale_question"."scale_id" IS '所属量表主表ID (解决题目归属，支持无因子量表)';
COMMENT ON COLUMN "public"."scale_question"."factor_id" IS '归属的因子维度ID (可为空，空代表该题只算总分，不归属特定维度)';
COMMENT ON COLUMN "public"."scale_question"."sort_order" IS '题目在前台显示的序号 (从小到大排列)';
COMMENT ON COLUMN "public"."scale_question"."content" IS '题干内容 (如: 你是否经常觉得头痛？)';
COMMENT ON COLUMN "public"."scale_question"."create_time" IS '创建时间';


-- 5. 题目选项分数表 (scale_question_option)
CREATE TABLE "public"."scale_question_option" (
                                                  "id" int8 NOT NULL DEFAULT nextval('seq_scale_option_id'::regclass),
                                                  "question_id" int8 NOT NULL,
                                                  "option_label" varchar(10) NOT NULL,
                                                  "option_text" varchar(255) NOT NULL,
                                                  "score" numeric(5,2) NOT NULL,
                                                  CONSTRAINT "pk_scale_question_option" PRIMARY KEY ("id")
) WITH (orientation=ROW, storetype=ustore) DISTRIBUTE BY HASH("question_id");

CREATE INDEX "idx_option_question_id" ON "public"."scale_question_option" ("question_id");

COMMENT ON TABLE "public"."scale_question_option" IS '题目选项分数表';
COMMENT ON COLUMN "public"."scale_question_option"."id" IS '选项唯一自增ID';
COMMENT ON COLUMN "public"."scale_question_option"."question_id" IS '所属题目ID';
COMMENT ON COLUMN "public"."scale_question_option"."option_label" IS '选项标识 (如: A, B, C, D)';
COMMENT ON COLUMN "public"."scale_question_option"."option_text" IS '选项文本内容 (如: 从不, 轻度, 中度, 严重)';
COMMENT ON COLUMN "public"."scale_question_option"."score" IS '该选项的实际计分权重 (支持正向计分、反向扣分、或小数得分)';


-- 6. 公共因子常模区间表 (scale_norm)
CREATE TABLE "public"."scale_norm" (
                                       "id" int8 NOT NULL DEFAULT nextval('seq_scale_norm_id'::regclass),
                                       "scale_id" int8 NOT NULL,
                                       "factor_id" int8 NOT NULL,
                                       "region" varchar(50) DEFAULT '通用'::character varying,
                                       "gender" char(1) DEFAULT '0'::bpchar,
                                       "score_type" char(1) DEFAULT '1'::bpchar,
                                       "norm_mean" numeric(6,3) DEFAULT 0.000,
                                       "norm_sd" numeric(6,3) DEFAULT 1.000,
                                       "min_score" numeric(6,2) NOT NULL,
                                       "max_score" numeric(6,2) NOT NULL,
                                       "level_name" varchar(20) NOT NULL,
                                       "report_template" text,
                                       "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                       CONSTRAINT "pk_scale_norm" PRIMARY KEY ("id")
) WITH (orientation=ROW) DISTRIBUTE BY HASH("scale_id");

CREATE INDEX "idx_norm_match_v2" ON "public"."scale_norm" ("scale_id", "factor_id", "region", "gender", "score_type");

COMMENT ON TABLE "public"."scale_norm" IS '公共因子常模区间表 (支持区间匹配与标准T分公式)';
COMMENT ON COLUMN "public"."scale_norm"."id" IS '常模规则唯一自增ID';
COMMENT ON COLUMN "public"."scale_norm"."scale_id" IS '所属量表主表ID';
COMMENT ON COLUMN "public"."scale_norm"."factor_id" IS '所属因子维度ID (0代表全表总分常模，非0代表特定维度常模)';
COMMENT ON COLUMN "public"."scale_norm"."region" IS '地域常模标签 (如: 通用、江苏、全国常模)';
COMMENT ON COLUMN "public"."scale_norm"."gender" IS '性别常模 (0通用, 1男, 2女)';
COMMENT ON COLUMN "public"."scale_norm"."score_type" IS '算分类型 (1均分常模, 2总分常模)';
COMMENT ON COLUMN "public"."scale_norm"."norm_mean" IS '该常模群体的分数平均值 (Mean，用于计算标准T分公式)';
COMMENT ON COLUMN "public"."scale_norm"."norm_sd" IS '该常模群体的标准差 (Standard Deviation，用于计算标准T分公式)';
COMMENT ON COLUMN "public"."scale_norm"."min_score" IS '区间判定最小值 (大于等于 >=)';
COMMENT ON COLUMN "public"."scale_norm"."max_score" IS '区间判定最大值 (小于 <)';
COMMENT ON COLUMN "public"."scale_norm"."level_name" IS '心理危机/严重程度名称 (如: 正常, 临界预警, 轻度焦虑, 重度抑郁)';
COMMENT ON COLUMN "public"."scale_norm"."report_template" IS '该严重程度对应的专业测评报告/指导建议模版';
COMMENT ON COLUMN "public"."scale_norm"."create_time" IS '创建时间';
