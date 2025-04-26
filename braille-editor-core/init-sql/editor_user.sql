/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : braille

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 23/04/2025 21:35:08
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
  `sys_role` enum('ROLE_USER','ROLE_ADMIN','ROLE_SUPER_ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ROLE_USER',
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
) ENGINE = InnoDB AUTO_INCREMENT = 10000005 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of editor_user
-- ----------------------------
INSERT INTO `editor_user` VALUES (1, 'admin1', 'b633cbd8-11c9-11f0-8762-00ff45ebc924', '系统管理员1', 'ROLE_SUPER_ADMIN', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678901', 'admin1@example.com', '2025-04-05 10:57:29', '2025-04-17 02:18:51', 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (2, 'admin2', 'b63a9617-11c9-11f0-8762-00ff45ebc924', '系统管理员2', 'ROLE_ADMIN', '$2a$10$IKl/iwyjzRqpR7y04cDNEuIdK1Rlq/RZKLApAqJUWCfGyueVlJWkm', '132890132321', 'admin2@example.com', '2025-04-05 10:57:29', '2025-04-23 19:55:25', 102400, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (4, 'admin4', 'b64118eb-11c9-11f0-8762-00ff45ebc924', '系统管理员4', 'ROLE_ADMIN', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678904', 'admin4@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (5, 'admin5', 'b643a78a-11c9-11f0-8762-00ff45ebc924', '系统管理员5', 'ROLE_ADMIN', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678905', 'admin5@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (6, 'admin6', 'b647f2b3-11c9-11f0-8762-00ff45ebc924', '系统管理员6', 'ROLE_ADMIN', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678906', 'admin6@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (7, 'admin7', 'b64a6fb3-11c9-11f0-8762-00ff45ebc924', '系统管理员7', 'ROLE_ADMIN', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678907', 'admin7@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (8, 'admin8', 'b64cc365-11c9-11f0-8762-00ff45ebc924', '系统管理员8', 'ROLE_ADMIN', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678908', 'admin8@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (9, 'admin9', 'b64f626f-11c9-11f0-8762-00ff45ebc924', '系统管理员9', 'ROLE_ADMIN', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678909', 'admin9@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (10, 'admin10', 'b652695a-11c9-11f0-8762-00ff45ebc924', '系统管理员10', 'ROLE_ADMIN', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678910', 'admin10@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (11, 'user11', 'b65504fc-11c9-11f0-8762-00ff45ebc924', '普通用户11', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678911', 'user11@example.com', '2025-04-05 10:57:29', '2025-04-23 21:13:31', 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (12, 'user12', 'b6578c4e-11c9-11f0-8762-00ff45ebc924', '普通用户12', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678912', 'user12@example.com', '2025-04-05 10:57:29', '2025-04-23 17:06:19', 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (13, 'user13', 'b65a7dd7-11c9-11f0-8762-00ff45ebc924', '普通用户13', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678913', 'user13@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (14, 'user14', 'b65ce0d9-11c9-11f0-8762-00ff45ebc924', '普通用户14', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678914', 'user14@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (15, 'user15', 'b661661a-11c9-11f0-8762-00ff45ebc924', '普通用户15', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678915', 'user15@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (16, 'user16', 'b66474ec-11c9-11f0-8762-00ff45ebc924', '普通用户16', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678916', 'user16@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (17, 'user17', 'b6679083-11c9-11f0-8762-00ff45ebc924', '普通用户17', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678917', 'user17@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (18, 'user18', 'b66a04da-11c9-11f0-8762-00ff45ebc924', '普通用户18', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678918', 'user18@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (19, 'user19', 'b66c6bc2-11c9-11f0-8762-00ff45ebc924', '普通用户19', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678919', 'user19@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (20, 'user20', 'b66f0977-11c9-11f0-8762-00ff45ebc924', '普通用户20', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678920', 'user20@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (21, 'user21', 'b6727027-11c9-11f0-8762-00ff45ebc924', '普通用户21', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678921', 'user21@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (22, 'user22', 'b674c13a-11c9-11f0-8762-00ff45ebc924', '普通用户22', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678922', 'user22@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (23, 'user23', 'b679122d-11c9-11f0-8762-00ff45ebc924', '普通用户23', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678923', 'user23@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (24, 'user24', 'b67c0477-11c9-11f0-8762-00ff45ebc924', '普通用户24', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678924', 'user24@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (25, 'user25', 'b67e58f0-11c9-11f0-8762-00ff45ebc924', '普通用户25', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678925', 'user25@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (26, 'user26', 'b68138ce-11c9-11f0-8762-00ff45ebc924', '普通用户26', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678926', 'user26@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (27, 'user27', 'b683e0da-11c9-11f0-8762-00ff45ebc924', '普通用户27', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678927', 'user27@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (28, 'user28', 'b6865724-11c9-11f0-8762-00ff45ebc924', '普通用户28', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678928', 'user28@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (29, 'user29', 'b6890250-11c9-11f0-8762-00ff45ebc924', '普通用户29', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678929', 'user29@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (30, 'user30', 'b68b2165-11c9-11f0-8762-00ff45ebc924', '普通用户30', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678930', 'user30@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (31, 'user31', 'b68d9340-11c9-11f0-8762-00ff45ebc924', '普通用户31', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678931', 'user31@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (32, 'user32', 'b6906b41-11c9-11f0-8762-00ff45ebc924', '普通用户32', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678932', 'user32@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (33, 'user33', 'b6934f3b-11c9-11f0-8762-00ff45ebc924', '普通用户33', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678933', 'user33@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (34, 'user34', 'b69713dd-11c9-11f0-8762-00ff45ebc924', '普通用户34', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678934', 'user34@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (35, 'user35', 'b69d2460-11c9-11f0-8762-00ff45ebc924', '普通用户35', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678935', 'user35@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (36, 'user36', 'b6a0ac35-11c9-11f0-8762-00ff45ebc924', '普通用户36', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678936', 'user36@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (37, 'user37', 'b6a4066c-11c9-11f0-8762-00ff45ebc924', '普通用户37', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678937', 'user37@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (38, 'user38', 'b6a965ca-11c9-11f0-8762-00ff45ebc924', '普通用户38', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678938', 'user38@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (39, 'user39', 'b6ad23d9-11c9-11f0-8762-00ff45ebc924', '普通用户39', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678939', 'user39@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (40, 'user40', 'b6afb50c-11c9-11f0-8762-00ff45ebc924', '普通用户40', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678940', 'user40@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (41, 'user41', 'b6b231cd-11c9-11f0-8762-00ff45ebc924', '普通用户41', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678941', 'user41@example.com', '2025-04-05 10:57:29', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (42, 'user42', 'b6bb0a9a-11c9-11f0-8762-00ff45ebc924', '普通用户42', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678942', 'user42@example.com', '2025-04-05 10:57:30', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (43, 'user43', 'b6bdc684-11c9-11f0-8762-00ff45ebc924', '普通用户43', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678943', 'user43@example.com', '2025-04-05 10:57:30', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (44, 'user44', 'b6c00b76-11c9-11f0-8762-00ff45ebc924', '普通用户44', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678944', 'user44@example.com', '2025-04-05 10:57:30', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (45, 'user45', 'b6c31c68-11c9-11f0-8762-00ff45ebc924', '普通用户45', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678945', 'user45@example.com', '2025-04-05 10:57:30', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (46, 'user46', 'b6c59afc-11c9-11f0-8762-00ff45ebc924', '普通用户46', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678946', 'user46@example.com', '2025-04-05 10:57:30', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (47, 'user47', 'b6c9907b-11c9-11f0-8762-00ff45ebc924', '普通用户47', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678947', 'user47@example.com', '2025-04-05 10:57:30', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (48, 'user48', 'b6cc02a8-11c9-11f0-8762-00ff45ebc924', '普通用户48', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678948', 'user48@example.com', '2025-04-05 10:57:30', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (49, 'user49', 'b6ce8e6d-11c9-11f0-8762-00ff45ebc924', '普通用户49', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678949', 'user49@example.com', '2025-04-05 10:57:30', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (50, 'user50', 'b6d0b3da-11c9-11f0-8762-00ff45ebc924', '普通用户50', 'ROLE_USER', '$2a$10$GbkN3wOvp0xP0Bz2qptJYuXPWdo3UQyvSdVd.eeqw0rZo5a8.hTgG', '12345678950', 'user50@example.com', '2025-04-05 10:57:30', NULL, 10737418240, 1, 1, 1, 1);
INSERT INTO `editor_user` VALUES (10000002, 'usertest1042', 'e38dcab3-7ef9-4f76-b009-19e2ca14480d', '普通用户test1042', 'ROLE_USER', '$2a$10$OiI1BkWAmRD6i3/0S.Fzuu1XSrwAHwBUZ1VVuj/qwup.8H1GwM4DG', '1334567891113', 'usertest1042@example.com', '2025-04-05 15:50:17', NULL, 10737418240, 1, 1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
