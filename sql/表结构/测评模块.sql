-- 1. 测评量表主表
CREATE TABLE scale (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- 自增主键
                       title VARCHAR(100) NOT NULL,                       -- 量表名称
                       description TEXT,                                  -- 简介与指导语
                       status SMALLINT DEFAULT 1,                         -- 状态：0-禁用，1-启用
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP     -- 创建时间
);
COMMENT ON TABLE scale IS '测评量表主表';

-- 2. 独立因子表（公共资产，支持大因子/小因子树形结构）
CREATE TABLE scale_factor (
                              id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              parent_id BIGINT DEFAULT 0,                        -- 父因子ID（0表示大因子，非0表示小因子）
                              name VARCHAR(50) NOT NULL,                         -- 因子名称，如：躯体化、焦虑倾向
                              code VARCHAR(10),                                  -- 因子代码，如：A, B, C
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE scale_factor IS '公共因子表（树形结构）';
CREATE INDEX idx_factor_parent_id ON scale_factor(parent_id);

-- 3. 【多对多关系表】量表与因子的绑定关系（实现量表共享因子）
CREATE TABLE scale_factor_rel (
                                  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  scale_id BIGINT NOT NULL,                          -- 量表ID
                                  factor_id BIGINT NOT NULL,                         -- 因子ID
                                  CONSTRAINT fk_rel_scale FOREIGN KEY (scale_id) REFERENCES scale(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_rel_factor FOREIGN KEY (factor_id) REFERENCES scale_factor(id) ON DELETE CASCADE
);
COMMENT ON TABLE scale_factor_rel IS '量表与因子多对多关联表';
-- 唯一索引：防止同一个量表重复绑定同一个因子，同时加速检索
CREATE UNIQUE INDEX idx_scale_factor_link ON scale_factor_rel(scale_id, factor_id);

-- 4. 量表题目表（核心改动：题目一对多属于因子，通过外键约束）
CREATE TABLE scale_question (
                                id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                factor_id BIGINT NOT NULL,                         -- 强关联：题目死死属于一个因子
                                sort_order INT DEFAULT 0,                          -- 题目在前台显示的顺序
                                content TEXT NOT NULL,                             -- 题干内容
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_q_factor FOREIGN KEY (factor_id) REFERENCES scale_factor(id) ON DELETE CASCADE
);
COMMENT ON TABLE scale_question IS '量表题目表（属于特定因子）';
CREATE INDEX idx_question_factor ON scale_question(factor_id);

-- 5. 题目选项表（一对多属于题目，分值干净独立）
CREATE TABLE scale_question_option (
                                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                       question_id BIGINT NOT NULL,                       -- 所属题目
                                       option_label VARCHAR(10) NOT NULL,                 -- 选项标识，如 A, B, C
                                       option_text VARCHAR(255) NOT NULL,                 -- 选项文本
                                       score DECIMAL(5,2) NOT NULL,                       -- 选项实际得分
                                       CONSTRAINT fk_opt_question FOREIGN KEY (question_id) REFERENCES scale_question(id) ON DELETE CASCADE
);
COMMENT ON TABLE scale_question_option IS '题目选项分数表';
CREATE INDEX idx_option_question ON scale_question_option(question_id);

-- 6. 常模区间表（基于因子的公共常模）
CREATE TABLE scale_norm (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            factor_id BIGINT NOT NULL,                         -- 所属因子
                            region VARCHAR(50) DEFAULT '通用',                 -- 地域常模，如：江苏、江西
                            gender SMALLINT DEFAULT 0,                         -- 性别常模：0-通用，1-男，2-女
                            score_type SMALLINT DEFAULT 1,                     -- 分数类型：1-均分，2-总分
                            min_score DECIMAL(6,2) NOT NULL,                   -- 区间最小值（>=）
                            max_score DECIMAL(6,2) NOT NULL,                   -- 区间最大值（<）
                            level_name VARCHAR(20) NOT NULL,                   -- 严重程度：正常、轻度、中度、重度
                            report_template TEXT,                              -- 结论建议模版
                            CONSTRAINT fk_norm_factor FOREIGN KEY (factor_id) REFERENCES scale_factor(id) ON DELETE CASCADE
);
COMMENT ON TABLE scale_norm IS '公共因子常模区间表';
CREATE INDEX idx_norm_match ON scale_norm(factor_id, region, gender, score_type);
