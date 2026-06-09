-- =============================================================================
-- 一、 创建订单与财务模块全局序列 (消灭表级锁，支持高并发轻量级内存发号)
-- =============================================================================
CREATE SEQUENCE "public"."seq_sys_order_id" INCREMENT 1 START 1000000;
CREATE SEQUENCE "public"."seq_fin_account_id" INCREMENT 1 START 100000;
CREATE SEQUENCE "public"."seq_fin_split_id" INCREMENT 1 START 100000;
CREATE SEQUENCE "public"."seq_fin_ledger_id" INCREMENT 1 START 100000;


-- =============================================================================
-- 二、 订单中心核心表 (处理高并发下单业务与状态机流转)
-- =============================================================================

-- 1. 订单主表 (sys_order)
CREATE TABLE "public"."sys_order" (
                                      "order_id" int8 NOT NULL DEFAULT nextval('seq_sys_order_id'::regclass),
                                      "order_sn" varchar(32) NOT NULL,
                                      "tenant_id" int8 NOT NULL,
                                      "user_id" int8 NOT NULL,
                                      "business_type" varchar(20) NOT NULL,
                                      "business_id" int8 NOT NULL,
                                      "total_amount" numeric(10,2) NOT NULL,
                                      "pay_amount" numeric(10,2) NOT NULL,
                                      "pay_status" char(1) DEFAULT '0'::bpchar,
                                      "pay_type" varchar(20) DEFAULT ''::character varying,
                                      "out_trade_no" varchar(64) DEFAULT ''::character varying,
                                      "pay_time" timestamp(6) DEFAULT NULL,
                                      "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                      "cancel_time" timestamp(6) DEFAULT NULL,
                                      CONSTRAINT "pk_sys_order" PRIMARY KEY ("order_id")
) WITH (orientation=ROW, storetype=ustore) DISTRIBUTE BY HASH("user_id");

-- 黄金唯一索引：部分索引（Partial Index）技术
-- 1. 物理层100%封死“同一个咨询排班时段被两个人同时买单抢占”的高并发冲突Bug；
-- 2. 自动排除已取消的失效订单，允许用户取消后重新对该业务下单。
CREATE UNIQUE INDEX "uk_order_business" ON "public"."sys_order" ("business_type", "business_id") WHERE "pay_status" != '2';
-- 通用查询索引：加速多APP端用户调取“我的订单列表”以及机构后台审计
CREATE INDEX "idx_order_user_tenant" ON "public"."sys_order" ("user_id", "tenant_id", "create_time");

-- 表及字段全注释（严格适配openGauss全路径点路径语法）
COMMENT ON TABLE "public"."sys_order" IS '订单主表 (负责业务流转与支付状态控制，解耦具体商品属性)';
COMMENT ON COLUMN "public"."sys_order"."order_id" IS '订单内部分值自增ID';
COMMENT ON COLUMN "public"."sys_order"."order_sn" IS '对外暴露的商户唯一订单号 (用于配合微信/支付宝对账，如: SN2026xxxx)';
COMMENT ON COLUMN "public"."sys_order"."tenant_id" IS '所属机构租户ID (0代表平台公共大厅/独立专家的全网公共订单)';
COMMENT ON COLUMN "public"."sys_order"."user_id" IS '下单人的全局用户UID (关联sys_user.user_id)';
COMMENT ON COLUMN "public"."sys_order"."business_type" IS '关联的业务商品类型 (appointment:咨询预约, scale:付费量表, course:心理课程)';
COMMENT ON COLUMN "public"."sys_order"."business_id" IS '具体关联的业务主键ID (如具体哪一笔预约记录ID，或具体的量表配置ID)';
COMMENT ON COLUMN "public"."sys_order"."total_amount" IS '订单原始应付总金额';
COMMENT ON COLUMN "public"."sys_order"."pay_amount" IS '用户实际实付总金额';
COMMENT ON COLUMN "public"."sys_order"."pay_status" IS '支付状态 (0-待支付, 1-已支付, 2-已取消, 3-已退款)';
COMMENT ON COLUMN "public"."sys_order"."pay_type" IS '支付通道渠道 (wechat_pay:微信支付, ali_pay:支付宝, apple_pay:苹果内购)';
COMMENT ON COLUMN "public"."sys_order"."out_trade_no" IS '第三方支付平台返回的外部底层流水号/微信官方单号';
COMMENT ON COLUMN "public"."sys_order"."pay_time" IS '用户完成支付的精确时间';
COMMENT ON COLUMN "public"."sys_order"."create_time" IS '用户点击提交、生成订单的时间';
COMMENT ON COLUMN "public"."sys_order"."cancel_time" IS '订单关闭或超时未支付自动取消的时间';


-- =============================================================================
-- 三、 财务中心核心表 (处理钱包账户余额、多方分成清算与终极对账流水)
-- =============================================================================

-- 2. 机构与咨询师财务账户表 (fin_account) —— 钱包底座
CREATE TABLE "public"."fin_account" (
                                        "account_id" int8 NOT NULL DEFAULT nextval('seq_fin_account_id'::regclass),
                                        "owner_type" char(1) NOT NULL,
                                        "owner_id" int8 NOT NULL,
                                        "total_balance" numeric(12,2) DEFAULT 0.00,
                                        "freeze_balance" numeric(12,2) DEFAULT 0.00,
                                        "available_balance" numeric(12,2) DEFAULT 0.00,
                                        "update_time" timestamp(6) DEFAULT pg_systimestamp(),
                                        CONSTRAINT "pk_fin_account" PRIMARY KEY ("account_id")
) WITH (orientation=ROW, storetype=ustore) DISTRIBUTE BY HASH("owner_id");

-- 联合唯一索引：物理层保证一个商户主体（独立专家或连锁机构）在系统内只有一个唯一的钱包，防多发钱包导致账目错乱
CREATE UNIQUE INDEX "uk_account_owner" ON "public"."fin_account" ("owner_type", "owner_id");

COMMENT ON TABLE "public"."fin_account" IS '机构与咨询师财务账户余额表 (钱包主体总账，高频扣减更新)';
COMMENT ON COLUMN "public"."fin_account"."account_id" IS '钱包账户内部唯一自增ID';
COMMENT ON COLUMN "public"."fin_account"."owner_type" IS '账户所有者主体类型 (1-连锁机构租户, 2-公共大厅独立咨询师个人UID)';
COMMENT ON COLUMN "public"."fin_account"."owner_id" IS '对应的机构租户ID 或 咨询师个人user_id';
COMMENT ON COLUMN "public"."fin_account"."total_balance" IS '钱包总资产 (始终等同于 可用余额 + 冻结金额)';
COMMENT ON COLUMN "public"."fin_account"."freeze_balance" IS '在途资产/冻结金额 (如：用户付了钱但尚未进行心理疏导的预约款，防违约退款风险)';
COMMENT ON COLUMN "public"."fin_account"."available_balance" IS '商户真正清算完成、可供随时发起提现到银行卡的可用余额';
COMMENT ON COLUMN "public"."fin_account"."update_time" IS '最后一次资金发生变动的时间';


-- 3. 订单多方分账明细表 (fin_order_split) —— 商业分成清算引擎
CREATE TABLE "public"."fin_order_split" (
                                            "split_id" int8 NOT NULL DEFAULT nextval('seq_fin_split_id'::regclass),
                                            "order_id" int8 NOT NULL,
                                            "party_type" char(1) NOT NULL,
                                            "party_id" int8 NOT NULL,
                                            "split_ratio" numeric(5,2) NOT NULL,
                                            "split_amount" numeric(10,2) NOT NULL,
                                            "is_cleared" char(1) DEFAULT '0'::bpchar,
                                            "clear_time" timestamp(6) DEFAULT NULL,
                                            "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                            CONSTRAINT "pk_fin_order_split" PRIMARY KEY ("split_id")
) WITH (orientation=ROW) DISTRIBUTE BY HASH("order_id");

-- 高效检索索引：用于定时结算任务(Settle Job)快速扫描哪些资金到期了需要执行解冻清算
CREATE INDEX "idx_split_party_lookup" ON "public"."fin_order_split" ("party_type", "party_id", "is_cleared");

COMMENT ON TABLE "public"."fin_order_split" IS '订单多方分账明细表 (清算引擎拆账分成结算的底层逻辑数据核心)';
COMMENT ON COLUMN "public"."fin_order_split"."split_id" IS '分账分成流水自增ID';
COMMENT ON COLUMN "public"."fin_order_split"."order_id" IS '关联的订单主表ID (关联sys_order.order_id)';
COMMENT ON COLUMN "public"."fin_order_split"."party_type" IS '参与本次分账分成利益方 (1-平台官方/公共自营大厅, 2-加盟连锁机构租户, 3-咨询师专家个人UID)';
COMMENT ON COLUMN "public"."fin_order_split"."party_id" IS '对应的利益方标识 (分别存储：0、租户ID、或用户个人user_id)';
COMMENT ON COLUMN "public"."fin_order_split"."split_ratio" IS '分成比例快照记录 (如 15.50 代表本订单对该利益方抽成 15.5%)';
COMMENT ON COLUMN "public"."fin_order_split"."split_amount" IS '该利益方本次分得的真实净金额数';
COMMENT ON COLUMN "public"."fin_order_split"."is_cleared" IS '清算状态 (0-待结算/在途冻结中, 1-已解冻清算转入可用余额, 2-用户发生逆向维权退款导致的分成扣减)';
COMMENT ON COLUMN "public"."fin_order_split"."clear_time" IS '咨询师真正履约完成、资金正式解冻划入可用余额的结算结算时间';
COMMENT ON COLUMN "public"."fin_order_split"."create_time" IS '用户支付成功、系统自动计算并拆账生成分成单的时间';


-- 4. 财务资金账目流水账本 (fin_ledger) —— 复式记账审计线
CREATE TABLE "public"."fin_ledger" (
                                       "ledger_id" int8 NOT NULL DEFAULT nextval('seq_fin_ledger_id'::regclass),
                                       "account_id" int8 NOT NULL,
                                       "order_id" int8 DEFAULT NULL,
                                       "trade_type" char(1) NOT NULL,
                                       "amount" numeric(10,2) NOT NULL,
                                       "before_balance" numeric(12,2) NOT NULL,
                                       "after_balance" numeric(12,2) NOT NULL,
                                       "remark" varchar(255) DEFAULT NULL::character varying,
                                       "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                       CONSTRAINT "pk_fin_ledger" PRIMARY KEY ("ledger_id")
) WITH (orientation=ROW) DISTRIBUTE BY HASH("account_id");

-- 黄金对账索引：用于商户在后台极速加载自己的“资金流水对账单”
CREATE INDEX "idx_ledger_account_time" ON "public"."fin_ledger" ("account_id", "create_time");

COMMENT ON TABLE "public"."fin_ledger" IS '财务资金账目流水账本 (只写不改的冷数据账本，用于每一分钱变动的绝对历史倒查审计)';
COMMENT ON COLUMN "public"."fin_ledger"."ledger_id" IS '账本流水记录唯一主键ID';
COMMENT ON COLUMN "public"."fin_ledger"."account_id" IS '关联的钱包账户表ID (关联fin_account.account_id)';
COMMENT ON COLUMN "public"."fin_ledger"."order_id" IS '触发这笔资金变动的源头业务订单ID (若由提现或平台调账触发则可为空)';
COMMENT ON COLUMN "public"."fin_ledger"."trade_type" IS '交易账目变动方向 (1-在途资产入账增加, 2-在途完成清算划入可用, 3-发起提现扣减, 4-逆向退款扣减可用)';
COMMENT ON COLUMN "public"."fin_ledger"."amount" IS '本次资金发生变动的绝对金额数';
COMMENT ON COLUMN "public"."fin_ledger"."before_balance" IS '本次变动发生前，该钱包对应的真实可用/冻结余额快照';
COMMENT ON COLUMN "public"."fin_ledger"."after_balance" IS '本次变动成功执行后，该钱包对应的最新余额快照';
COMMENT ON COLUMN "public"."fin_ledger"."remark" IS '交易备注信息 (如: 关联订单SN2026完成履约清算解冻分账款入账)';
COMMENT ON COLUMN "public"."fin_ledger"."create_time" IS '流水落账、资金发生变动的铁证时间';
