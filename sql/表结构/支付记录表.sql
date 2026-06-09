-- 创建支付流水专属序列
CREATE SEQUENCE "public"."seq_sys_pay_log_id" INCREMENT 1 START 1000000;

-- =============================================================================
-- 表二十：第三方支付流水记录表 (sys_pay_log) ── 财务中心的“外围盾牌”
-- =============================================================================
CREATE TABLE "public"."sys_pay_log" (
                                        "pay_log_id" int8 NOT NULL DEFAULT nextval('seq_sys_pay_log_id'::regclass),
                                        "order_id" int8 NOT NULL,
                                        "order_sn" varchar(32) NOT NULL,
                                        "out_trade_no" varchar(64) NOT NULL,
                                        "pay_channel" varchar(20) NOT NULL,
                                        "trade_type" char(1) NOT NULL,
                                        "total_amount" numeric(10,2) NOT NULL,
                                        "trade_status" varchar(20) NOT NULL,
                                        "openid" varchar(128) DEFAULT NULL::character varying,
                                        "notify_raw_data" text,
                                        "create_time" timestamp(6) DEFAULT pg_systimestamp(),
                                        "success_time" timestamp(6) DEFAULT NULL,
                                        CONSTRAINT "pk_sys_pay_log" PRIMARY KEY ("pay_log_id")
) WITH (orientation=ROW, storetype=ustore) DISTRIBUTE BY HASH("order_id"); -- 跟着订单分布

-- 黄金唯一索引：防止微信/支付宝因为网络超时重复发送回调通知（幂等性防线）
CREATE UNIQUE INDEX "uk_pay_out_trade" ON "public"."sys_pay_log" ("out_trade_no", "trade_type");
CREATE INDEX "idx_pay_log_order" ON "public"."sys_pay_log" ("order_id", "create_time");

-- 表及全字段注释覆盖（严格适配openGauss全路径点路径语法）
COMMENT ON TABLE "public"."sys_pay_log" IS '第三方支付流水记录表 (记录微信/支付宝的正逆向真实资金轨迹，用于财务终极对账)';
COMMENT ON COLUMN "public"."sys_pay_log"."pay_log_id" IS '支付流水内部分值自增ID';
COMMENT ON COLUMN "public"."sys_pay_log"."order_id" IS '关联的订单表内部ID (关联sys_order.order_id)';
COMMENT ON COLUMN "public"."sys_pay_log"."order_sn" IS '关联的商户唯一订单号快照';
COMMENT ON COLUMN "public"."sys_pay_log"."out_trade_no" IS '第三方支付官方返回的唯一订单号/流水号 (如微信支付单号，对账核心)';
COMMENT ON COLUMN "public"."sys_pay_log"."pay_channel" IS '支付渠道 (wechat_pay:微信支付, ali_pay:支付宝)';
COMMENT ON COLUMN "public"."sys_pay_log"."trade_type" IS '交易流向类型 (1-正向支付充值, 2-逆向维权退款)';
COMMENT ON COLUMN "public"."sys_pay_log"."total_amount" IS '本次支付或退款的真实发生金额';
COMMENT ON COLUMN "public"."sys_pay_log"."trade_status" IS '第三方支付平台的原始状态 (如微信的 SUCCESS, REFUND, USERPAYING)';
COMMENT ON COLUMN "public"."sys_pay_log"."openid" IS '付款人在微信或支付宝侧的唯一付款身份标识';
COMMENT ON COLUMN "public"."sys_pay_log"."notify_raw_data" IS '第三方官方发回的完整加密XML/JSON原始报文 (留存最原始的排错铁证)';
COMMENT ON COLUMN "public"."sys_pay_log"."create_time" IS '发起支付拉起收银台 或 发起退款申请的时间';
COMMENT ON COLUMN "public"."sys_pay_log"."success_time" IS '微信/支付宝回调确认到账或退款成功的铁证时间';
