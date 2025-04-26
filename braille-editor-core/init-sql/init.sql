/*
 Navicat Premium Data Transfer

 Source Server         : 1
 Source Server Type    : MySQL
 Source Server Version : 80037
 Source Host           : localhost:3306
 Source Schema         : braille

 Target Server Type    : MySQL
 Target Server Version : 80037
 File Encoding         : 65001

 Date: 16/03/2025 21:03:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


-- ----------------------------
-- Table structure for editor_user
-- ----------------------------
DROP TABLE IF EXISTS `editor_user`;
CREATE TABLE `editor_user`  (
                                         `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
                                         `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                         `uuid` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                         `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                         `sys_role` enum('ROLE_USER','ROLE_ADMIN','ROLE_SUPER_ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'user',
                                         `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                         `phone` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                         `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                         `register_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                         `last_login_at` datetime NULL DEFAULT NULL,
                                         `storage_quota` bigint UNSIGNED NULL DEFAULT 10737418240,
                                         `account_non_expired` tinyint(1) NULL DEFAULT 1,
                                         `account_non_locked` tinyint(1) NULL DEFAULT 1,
                                         `credentials_non_expired` tinyint(1) NULL DEFAULT 1,
                                         `enabled` tinyint(1) NULL DEFAULT 1,
                                         PRIMARY KEY (`id`) USING BTREE,
                                         UNIQUE INDEX `uuid`(`uuid` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC AUTO_INCREMENT = 10000000;



-- ----------------------------
-- Table structure for editor_team
-- ----------------------------
DROP TABLE IF EXISTS `editor_team`;
CREATE TABLE `editor_team`  (
                                `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
                                `owner` bigint UNSIGNED NOT NULL,
                                `team_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                `uuid` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                `storage_quota` bigint UNSIGNED NULL DEFAULT 10737418240,
                                `default_read_permission` tinyint(1) NULL DEFAULT 1,
                                `default_write_permission` tinyint(1) NULL DEFAULT 0,
                                `max_members` tinyint UNSIGNED NULL DEFAULT 50,
                                `is_active` tinyint(1) NULL DEFAULT 1,
                                `create_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                `update_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `uuid`(`uuid` ASC) USING BTREE,
                                UNIQUE INDEX `idx_uuid`(`uuid` ASC) USING BTREE,
                                INDEX `idx_owner`(`owner` ASC) USING BTREE,
                                CONSTRAINT `editor_team_ibfk_1` FOREIGN KEY (`owner`) REFERENCES `editor_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                CONSTRAINT `chk_storage_quota` CHECK (`storage_quota` <= 1099511627776)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic AUTO_INCREMENT = 10000000;


-- ----------------------------
-- Table structure for oss_file_metadata
-- ----------------------------
DROP TABLE IF EXISTS `oss_file_metadata`;
CREATE TABLE `oss_file_metadata`  (
                                       `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
                                       `real_file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                       `path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                       `s3_bucket` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                       `s3_key` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                                       `file_size` bigint NOT NULL DEFAULT 0,
                                       `create_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                       `create_by` bigint UNSIGNED NULL,
                                       `last_modified_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       `last_modified_by` bigint UNSIGNED NULL,
                                       `parent_path` varchar(512) NULL,
                                       `is_dir` tinyint(1) NOT NULL DEFAULT 0,
                                       `file_hash` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                       `mime_type` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
                                       `hidden` tinyint(1) NOT NULL DEFAULT 0,
                                       `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
                                       `deleted_at` datetime NULL DEFAULT NULL,
                                       `deleted_by` bigint UNSIGNED NULL,
                                       PRIMARY KEY (`id`) USING BTREE,
                                       CONSTRAINT `fk_create_by` FOREIGN KEY (`create_by`) REFERENCES `editor_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
                                       CONSTRAINT `fk_last_update_by` FOREIGN KEY (`last_modified_by`) REFERENCES `editor_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
                                       CONSTRAINT `fk_last_delete_by` FOREIGN KEY (`deleted_by`) REFERENCES `editor_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
                                       INDEX `idx_s3_bucket` (`s3_bucket`),
                                       INDEX `idx_path` (`path`(512))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for oss_file_recycle
-- ----------------------------
DROP TABLE IF EXISTS `oss_file_recycle`;
CREATE TABLE `oss_file_recycle`  (
                                     `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
                                     `s3_key_prefix` varchar(255) NOT NULL,
                                     `origin_file_id` bigint UNSIGNED NOT NULL,
                                     `origin_file_path` bigint UNSIGNED NOT NULL,
                                     `operated_by` bigint UNSIGNED NOT NULL,
                                     `recycle_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                     `remaining_days` tinyint UNSIGNED NULL DEFAULT 30,
                                     PRIMARY KEY (`id`) USING BTREE,
                                     INDEX `fk_recycle_file`(`origin_file_id` ASC) USING BTREE,
                                     INDEX `fk_recycle_operator`(`operated_by` ASC) USING BTREE,
                                     CONSTRAINT `fk_recycle_file` FOREIGN KEY (`origin_file_id`) REFERENCES `oss_file_metadata` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                     CONSTRAINT `fk_recycle_operator` FOREIGN KEY (`operated_by`) REFERENCES `editor_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                     CONSTRAINT `chk_remaining_days` CHECK (`remaining_days` between 1 and 255)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for team_member_role_mapping
-- ----------------------------
DROP TABLE IF EXISTS `editor_team_member`;
CREATE TABLE editor_team_member  (
                                             `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
                                             `team_id` bigint UNSIGNED NOT NULL,
                                             `user_id` bigint UNSIGNED NOT NULL,
                                             `team_role` enum('owner','admin','member') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'member',
                                             `join_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                             PRIMARY KEY (`id`) USING BTREE,
                                             UNIQUE INDEX `idx_team_user`(`team_id` ASC, `user_id` ASC) USING BTREE,
                                             INDEX `fk_member_user`(`user_id` ASC) USING BTREE,
                                             CONSTRAINT `fk_member_team` FOREIGN KEY (`team_id`) REFERENCES `editor_team` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                             CONSTRAINT `fk_member_user` FOREIGN KEY (`user_id`) REFERENCES `editor_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
