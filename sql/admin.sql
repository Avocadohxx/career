CREATE DATABASE IF NOT EXISTS career DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE career;

CREATE TABLE IF NOT EXISTS `admin` (
    `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '主键，管理员编号',
    `user_name`  VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`   VARCHAR(100) NOT NULL COMMENT '登录密码（BCrypt加密存储）',
    `real_name`  VARCHAR(50)           DEFAULT NULL COMMENT '真实姓名',
    `email`      VARCHAR(100)          DEFAULT NULL COMMENT '电子邮箱',
    `create_at`  DATETIME              DEFAULT NULL COMMENT '创建时间',
    `updated_at` DATETIME              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '管理员信息表';

-- 默认管理员账号由后端 DataInitializer 在首次启动时自动创建
-- 账号：admin   密码：admin123
