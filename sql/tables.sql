USE career;

-- 岗位表
CREATE TABLE IF NOT EXISTS `job` (
    `id`           INT          NOT NULL AUTO_INCREMENT,
    `company_id`   INT          NOT NULL COMMENT '发布企业ID',
    `title`        VARCHAR(100) NOT NULL COMMENT '岗位名称',
    `city`         VARCHAR(50)           DEFAULT NULL COMMENT '工作城市',
    `salary_min`   INT                   DEFAULT NULL COMMENT '薪资下限（K）',
    `salary_max`   INT                   DEFAULT NULL COMMENT '薪资上限（K）',
    `education`    VARCHAR(20)           DEFAULT NULL COMMENT '学历要求',
    `job_type`     VARCHAR(20)           DEFAULT '全职' COMMENT '全职/实习',
    `skills`       VARCHAR(500)          DEFAULT NULL COMMENT '技能要求（逗号分隔）',
    `description`  TEXT                  DEFAULT NULL COMMENT '岗位描述',
    `status`       TINYINT               DEFAULT 0 COMMENT '0待审核 1通过 2拒绝 3已下线',
    `create_at`    DATETIME              DEFAULT NULL,
    `updated_at`   DATETIME              DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '岗位表';

-- 简历表
CREATE TABLE IF NOT EXISTS `resume` (
    `id`             INT  NOT NULL AUTO_INCREMENT,
    `student_id`     INT  NOT NULL COMMENT '学生ID',
    `avatar`         VARCHAR(200) DEFAULT NULL,
    `job_intention`  VARCHAR(100) DEFAULT NULL COMMENT '求职意向',
    `city_intention` VARCHAR(50)  DEFAULT NULL COMMENT '意向城市',
    `education`      VARCHAR(20)  DEFAULT NULL COMMENT '最高学历',
    `skills`         VARCHAR(500) DEFAULT NULL COMMENT '技能标签（逗号分隔）',
    `self_eval`      TEXT         DEFAULT NULL COMMENT '自我评价',
    `edu_history`    TEXT         DEFAULT NULL COMMENT '教育经历（JSON）',
    `work_history`   TEXT         DEFAULT NULL COMMENT '工作/实习经历（JSON）',
    `updated_at`     DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_student_id` (`student_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '简历表';

-- 应聘记录表
CREATE TABLE IF NOT EXISTS `application` (
    `id`             INT NOT NULL AUTO_INCREMENT,
    `student_id`     INT NOT NULL,
    `job_id`         INT NOT NULL,
    `company_id`     INT NOT NULL,
    `status`         TINYINT  DEFAULT 0 COMMENT '0待查看 1已查看 2已邀请 3已录用 4已拒绝 5待面试（学生已确认） 6面试不通过',
    `interview_time` DATETIME DEFAULT NULL COMMENT '面试时间',
    `apply_time`     DATETIME DEFAULT NULL COMMENT '投递时间',
    `updated_at`     DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '应聘记录表';

-- 指导帖子
CREATE TABLE IF NOT EXISTS `guidance_post` (
    `id`           INT NOT NULL AUTO_INCREMENT,
    `counselor_id` INT NOT NULL,
    `title`        VARCHAR(200) NOT NULL,
    `content`      TEXT,
    `create_at`    DATETIME DEFAULT NULL,
    `updated_at`   DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '指导帖子';

-- 公开课
CREATE TABLE IF NOT EXISTS `course` (
    `id`           INT NOT NULL AUTO_INCREMENT,
    `counselor_id` INT NOT NULL,
    `title`        VARCHAR(200) NOT NULL COMMENT '课程名称',
    `description`  TEXT,
    `start_time`   DATETIME DEFAULT NULL COMMENT '开课时间',
    `max_students` INT      DEFAULT 50 COMMENT '报名上限',
    `enrolled`     INT      DEFAULT 0  COMMENT '已报名人数',
    `status`       TINYINT  DEFAULT 1  COMMENT '1进行中 0已结束',
    `create_at`    DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公开课';

-- 公开课报名
CREATE TABLE IF NOT EXISTS `course_enrollment` (
    `id`          INT NOT NULL AUTO_INCREMENT,
    `course_id`   INT NOT NULL,
    `student_id`  INT NOT NULL,
    `enroll_time` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_student` (`course_id`, `student_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公开课报名';

-- 交流中心帖子
CREATE TABLE IF NOT EXISTS `forum_post` (
    `id`          INT NOT NULL AUTO_INCREMENT,
    `student_id`  INT NOT NULL,
    `title`       VARCHAR(200) NOT NULL,
    `content`     TEXT,
    `reply_count` INT      DEFAULT 0,
    `create_at`   DATETIME DEFAULT NULL,
    `updated_at`  DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '交流中心帖子';

-- 帖子回复
CREATE TABLE IF NOT EXISTS `forum_reply` (
    `id`        INT NOT NULL AUTO_INCREMENT,
    `post_id`   INT NOT NULL,
    `user_id`   INT NOT NULL COMMENT '回复者ID',
    `user_role` VARCHAR(20) NOT NULL COMMENT '回复者角色',
    `user_name` VARCHAR(50) DEFAULT NULL COMMENT '回复者姓名',
    `content`   TEXT,
    `create_at` DATETIME DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '帖子回复';

-- 数据字典
CREATE TABLE IF NOT EXISTS `dict` (
    `id`          INT NOT NULL AUTO_INCREMENT,
    `dict_type`   VARCHAR(50)  NOT NULL COMMENT '字典类型',
    `dict_key`    VARCHAR(50)  NOT NULL COMMENT '字典键',
    `dict_value`  VARCHAR(200) NOT NULL COMMENT '字典值',
    `description` VARCHAR(200) DEFAULT NULL,
    `sort`        INT          DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '数据字典';

-- 学生信息表
CREATE TABLE IF NOT EXISTS `student` (
    `id`             INT          NOT NULL AUTO_INCREMENT COMMENT '主键，学生编号',
    `user_name`      VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`       VARCHAR(100) NOT NULL COMMENT '登录密码（加密存储）',
    `real_name`      VARCHAR(50)           DEFAULT NULL COMMENT '真实姓名',
    `school`         VARCHAR(100)          DEFAULT NULL COMMENT '所在学校',
    `major`          VARCHAR(50)           DEFAULT NULL COMMENT '所学专业',
    `department`     VARCHAR(50)           DEFAULT NULL COMMENT '所在院系',
    `job_intention`  VARCHAR(100)          DEFAULT NULL COMMENT '求职意向',
    `city_intention` VARCHAR(50)           DEFAULT NULL COMMENT '意向城市',
    `phone`          VARCHAR(20)           DEFAULT NULL COMMENT '联系电话',
    `email`          VARCHAR(100)          DEFAULT NULL COMMENT '电子邮箱',
    `address`        VARCHAR(200)          DEFAULT NULL COMMENT '家庭地址',
    `counselor_id`   INT                   DEFAULT NULL COMMENT '外键，关联辅导员表',
    `is_del`         TINYINT               DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
    `create_at`      DATETIME              DEFAULT NULL COMMENT '创建时间',
    `updated_at`     DATETIME              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学生信息表';

-- 辅导员信息表
CREATE TABLE IF NOT EXISTS `counselor` (
    `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '主键，辅导员编号',
    `user_name`  VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`   VARCHAR(100) NOT NULL COMMENT '登录密码（加密存储）',
    `real_name`  VARCHAR(50)           DEFAULT NULL COMMENT '真实姓名',
    `department` VARCHAR(50)           DEFAULT NULL COMMENT '所属院系',
    `phone`      VARCHAR(20)           DEFAULT NULL COMMENT '联系电话',
    `email`      VARCHAR(100)          DEFAULT NULL COMMENT '电子邮箱',
    `is_del`     TINYINT               DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
    `create_at`  DATETIME              DEFAULT NULL COMMENT '创建时间',
    `updated_at` DATETIME              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '辅导员信息表';

-- 企业信息表
CREATE TABLE IF NOT EXISTS `company` (
    `id`           INT          NOT NULL AUTO_INCREMENT COMMENT '主键，企业编号',
    `user_name`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`     VARCHAR(100) NOT NULL COMMENT '登录密码（加密存储）',
    `company_name` VARCHAR(100) NOT NULL COMMENT '企业名称',
    `industry`     VARCHAR(50)           DEFAULT NULL COMMENT '所属行业',
    `scale`        VARCHAR(50)           DEFAULT NULL COMMENT '企业规模',
    `address`      VARCHAR(200)          DEFAULT NULL COMMENT '企业地址',
    `phone`        VARCHAR(20)           DEFAULT NULL COMMENT '联系电话',
    `email`        VARCHAR(100)          DEFAULT NULL COMMENT '电子邮箱',
    `description`  TEXT                  DEFAULT NULL COMMENT '企业简介',
    `status`       TINYINT               DEFAULT 0 COMMENT '审核状态：0待审核 1通过 2拒绝',
    `is_del`       TINYINT               DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
    `create_at`    DATETIME              DEFAULT NULL COMMENT '创建时间',
    `updated_at`   DATETIME              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '企业信息表';

-- 管理员信息表
CREATE TABLE IF NOT EXISTS `admin` (
    `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '主键，管理员编号',
    `user_name`  VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`   VARCHAR(100) NOT NULL COMMENT '登录密码（加密存储）',
    `real_name`  VARCHAR(50)           DEFAULT NULL COMMENT '真实姓名',
    `email`      VARCHAR(100)          DEFAULT NULL COMMENT '电子邮箱',
    `is_del`     TINYINT               DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
    `create_at`  DATETIME              DEFAULT NULL COMMENT '创建时间',
    `updated_at` DATETIME              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '管理员信息表';
