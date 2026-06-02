/*
 Navicat MySQL Dump SQL

 Source Server         : sky
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : pet_adoption

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 28/05/2026 15:56:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for adoption_applications
-- ----------------------------
DROP TABLE IF EXISTS `adoption_applications`;
CREATE TABLE `adoption_applications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `apply_time` datetime(6) NULL DEFAULT NULL,
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `review_time` datetime(6) NULL DEFAULT NULL,
  `status` enum('APPROVED','PENDING','REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `adopter_id` bigint NULL DEFAULT NULL,
  `pet_id` bigint NULL DEFAULT NULL,
  `contact` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `family_situation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `has_other_pets` bit(1) NULL DEFAULT NULL,
  `housing_area` int NULL DEFAULT NULL,
  `other_pets_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `pet_experience` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `residence_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `reject_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKnna75sksvo5ph9ue2q72u7cql`(`adopter_id` ASC) USING BTREE,
  INDEX `FKngtgolq4ramak2fvoc9ebm9qm`(`pet_id` ASC) USING BTREE,
  CONSTRAINT `FKngtgolq4ramak2fvoc9ebm9qm` FOREIGN KEY (`pet_id`) REFERENCES `pets` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKnna75sksvo5ph9ue2q72u7cql` FOREIGN KEY (`adopter_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of adoption_applications
-- ----------------------------
INSERT INTO `adoption_applications` VALUES (1, '2026-04-05 23:42:14.241940', '理由测试', '2026-04-06 00:10:25.547913', 'APPROVED', 3, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (2, '2026-04-06 23:33:02.998888', 'aaaa', '2026-04-06 23:33:48.883555', 'APPROVED', 2, 2, 'ssss', 'ccc', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (3, '2026-04-06 23:43:04.257083', 'a', '2026-04-07 23:15:11.766383', 'APPROVED', 2, 3, 'a', 'a', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (4, '2026-04-06 23:43:17.545950', 'b', '2026-04-07 23:23:07.713957', 'REJECTED', 2, 4, 'b', 'b', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (5, '2026-04-06 23:43:43.306385', 'nnn', '2026-04-07 23:15:14.217590', 'REJECTED', 3, 3, 'nn', 'nn', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (6, '2026-04-06 23:43:48.787634', 'jj', '2026-04-07 23:23:07.720178', 'APPROVED', 3, 4, 'jj', 'jj', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (7, '2026-04-07 23:49:42.293429', 'sdasd', '2026-04-07 23:50:20.779651', 'APPROVED', 3, 5, 'sdad', 'dasdsa', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (8, '2026-04-07 23:50:00.582222', 'rewr', '2026-04-07 23:50:22.470956', 'APPROVED', 3, 7, 'wrwe', 'rwer', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (9, '2026-04-07 23:55:35.352179', 'gdf', '2026-04-07 23:55:56.797452', 'APPROVED', 3, 18, 'dasd', 'fdsf', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (10, '2026-04-08 00:10:07.453090', 'gdfgfdg', '2026-05-01 16:28:45.397909', 'REJECTED', 3, 8, 'sdfadfa', 'dfsdfsd', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (11, '2026-04-08 00:10:27.544745', 'sdfsdfsd', NULL, 'PENDING', 2, 8, 'dfdafff', 'fgfdgdf', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (12, '2026-04-08 00:10:33.297363', 'dqewd', NULL, 'PENDING', 2, 9, 'rdfsdf', 'weqwr', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (13, '2026-04-08 00:28:11.749496', 'fdffgdsg', NULL, 'PENDING', 2, 10, 'hfghfg', 'gfdgdfg', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (14, '2026-04-09 20:33:29.193536', '删除', '2026-04-09 20:33:55.796158', 'REJECTED', 2, 20, '123456', '删除', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (15, '2026-04-09 22:09:23.987113', 'fdsfsd', '2026-04-28 13:04:14.503714', 'REJECTED', 1, 23, 'fsfdf', 'fsdfsdf', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `adoption_applications` VALUES (17, '2026-04-10 21:19:57.087270', '领养测试', '2026-04-28 13:04:14.508975', 'REJECTED', 1, 23, '12345578922', '测试', b'0', NULL, '', '无经验', '自有住房', NULL);
INSERT INTO `adoption_applications` VALUES (18, '2026-04-10 22:20:04.946308', 'fsdfsdfsd', '2026-04-28 13:04:14.512464', 'REJECTED', 1, 23, 'sdasdasf', 'sfdfdsf', b'1', NULL, '两只猫', '丰富经验', '自有住房', NULL);
INSERT INTO `adoption_applications` VALUES (19, '2026-04-10 22:32:38.005149', '优化测试', '2026-04-28 13:04:14.517601', 'REJECTED', 1, 23, '1234534533', '测试', b'1', NULL, '两只猫咪', '丰富经验', '自有住房', NULL);
INSERT INTO `adoption_applications` VALUES (20, '2026-04-18 21:31:53.511487', 'dasdasd', '2026-04-18 21:34:17.051901', 'APPROVED', 4, 24, 'dasd', 'asdsa', b'1', NULL, '111', '丰富经验', '自有住房', NULL);
INSERT INTO `adoption_applications` VALUES (21, '2026-04-18 21:34:27.806907', 'xasdsa', '2026-04-18 21:34:31.172476', 'REJECTED', 4, 23, 'dsdasd', 'dasadfad', b'0', NULL, '', '有过经验', '租房', NULL);
INSERT INTO `adoption_applications` VALUES (25, '2026-04-28 13:03:00.235411', '111111', '2026-04-28 13:04:14.521405', 'APPROVED', 1, 23, '111111', '11111', b'0', NULL, '', '无经验', '自有住房', NULL);
INSERT INTO `adoption_applications` VALUES (26, '2026-04-30 02:33:59.701797', '而当时的', '2026-04-30 02:37:02.730390', 'REJECTED', 4, 37, '13123', '13213', b'0', NULL, '', '无经验', '自有住房', NULL);
INSERT INTO `adoption_applications` VALUES (27, '2026-04-30 02:34:12.554675', '1431', '2026-04-30 02:37:04.913016', 'REJECTED', 4, 34, '3131', '132', b'0', NULL, '', '无经验', '自有住房', NULL);
INSERT INTO `adoption_applications` VALUES (28, '2026-04-30 02:37:21.399775', '水水水水', '2026-04-30 02:40:49.191535', 'REJECTED', 4, 37, '111', '水水', b'0', NULL, '', '无经验', '自有住房', NULL);
INSERT INTO `adoption_applications` VALUES (29, '2026-04-30 02:40:57.168050', '111', '2026-05-01 16:08:59.764839', 'APPROVED', 4, 37, '11', '11', b'0', NULL, '', '无经验', '自有住房', NULL);
INSERT INTO `adoption_applications` VALUES (30, '2026-04-30 02:41:08.455167', '111', '2026-05-01 15:40:52.066976', 'APPROVED', 4, 34, '111', '1111', b'0', NULL, '', '有过经验', '租房', NULL);
INSERT INTO `adoption_applications` VALUES (31, '2026-04-30 07:32:45.219269', '测试测试测试', '2026-05-01 16:28:30.238247', 'REJECTED', 4, 17, '13253327845', '无无无', b'0', NULL, '', '无经验', '租房', NULL);
INSERT INTO `adoption_applications` VALUES (32, '2026-05-06 23:25:57.343448', '231232', NULL, 'PENDING', 4, 32, '32131231', '131231', b'0', NULL, '', '有过经验', '租房', NULL);
INSERT INTO `adoption_applications` VALUES (33, '2026-05-06 23:26:31.575570', '23213', '2026-05-06 23:27:51.578569', 'REJECTED', 4, 48, '3123123', '12312321', b'0', NULL, '', '有过经验', '租房', '拒绝测试');
INSERT INTO `adoption_applications` VALUES (34, '2026-05-07 22:42:45.714985', '用户领养', '2026-05-11 21:13:24.009645', 'APPROVED', 1, 30, 'weixin_user1', '家庭状态良好', b'0', NULL, '', '无经验', '自有住房', NULL);
INSERT INTO `adoption_applications` VALUES (36, '2026-05-11 21:12:35.599823', '12121', '2026-05-11 21:13:15.069348', 'REJECTED', 1, 30, '1212121222', '21212', b'0', NULL, '', '无经验', '自有住房', '1111111');
INSERT INTO `adoption_applications` VALUES (37, '2026-05-11 21:20:08.858899', '12121', '2026-05-11 21:27:49.075229', 'APPROVED', 1, 29, '1212', '21212', b'0', NULL, '', '有过经验', '租房', NULL);
INSERT INTO `adoption_applications` VALUES (38, '2026-05-11 21:20:33.704181', '45435', '2026-05-11 21:27:49.038276', 'REJECTED', 2, 29, '55', '34534', b'0', NULL, '', '有过经验', '租房', '该宠物已被其他用户领养，感谢您的关注！');
INSERT INTO `adoption_applications` VALUES (39, '2026-05-11 21:27:27.728723', 'sdasdad', '2026-05-11 21:27:45.181384', 'REJECTED', 5, 29, 'dasdasd', 'asdasdas', b'0', NULL, '', '有过经验', '宿舍', '111');
INSERT INTO `adoption_applications` VALUES (40, '2026-05-12 00:36:50.931252', '111222', NULL, 'PENDING', 4, 39, '111', '111222', b'0', NULL, '', '无经验', '自有住房', NULL);

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `parent_id` bigint NULL DEFAULT NULL,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKlri30okf66phtcgbe5pok7cc0`(`parent_id` ASC) USING BTREE,
  INDEX `FK97vrdo0bohplxhj8o2hych8ag`(`post_id` ASC) USING BTREE,
  INDEX `FK8omq0tc18jd43bu5tjh6jvraq`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK8omq0tc18jd43bu5tjh6jvraq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK97vrdo0bohplxhj8o2hych8ag` FOREIGN KEY (`post_id`) REFERENCES `help_posts` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKlri30okf66phtcgbe5pok7cc0` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comments
-- ----------------------------
INSERT INTO `comments` VALUES (1, '测试', '2026-04-07 23:39:02.709023', NULL, 2, 4);
INSERT INTO `comments` VALUES (2, '测试测试', '2026-04-07 23:39:08.357133', 1, 2, 4);
INSERT INTO `comments` VALUES (3, '测试', '2026-04-07 23:39:13.209214', NULL, 1, 4);
INSERT INTO `comments` VALUES (4, '再次测试', '2026-04-07 23:39:16.600090', 3, 1, 4);
INSERT INTO `comments` VALUES (5, '测试', '2026-04-08 00:25:36.194491', NULL, 3, 4);
INSERT INTO `comments` VALUES (6, '测试', '2026-04-10 22:23:11.768875', NULL, 7, 1);
INSERT INTO `comments` VALUES (8, '测试', '2026-04-10 22:36:38.632054', NULL, 19, 1);
INSERT INTO `comments` VALUES (9, '测试', '2026-04-10 22:37:07.555366', NULL, 4, 1);
INSERT INTO `comments` VALUES (10, '测试', '2026-04-10 22:38:47.186780', NULL, 4, 1);
INSERT INTO `comments` VALUES (11, 'test', '2026-04-10 22:40:50.108584', NULL, 21, 4);
INSERT INTO `comments` VALUES (12, 'test', '2026-04-10 22:43:36.117909', 3, 1, 4);
INSERT INTO `comments` VALUES (13, 'test', '2026-04-12 14:14:20.759141', NULL, 21, 4);
INSERT INTO `comments` VALUES (14, 'test', '2026-04-12 14:14:26.005262', 13, 21, 4);
INSERT INTO `comments` VALUES (15, 'test', '2026-04-12 14:41:02.431043', 11, 21, 4);
INSERT INTO `comments` VALUES (18, 'sadasjfha', '2026-04-18 21:31:24.934756', NULL, 21, 4);
INSERT INTO `comments` VALUES (19, 'dasdasd', '2026-04-18 21:31:28.967309', 18, 21, 4);
INSERT INTO `comments` VALUES (21, '1111', '2026-04-20 15:16:47.710306', NULL, 22, 4);
INSERT INTO `comments` VALUES (22, '2222', '2026-04-20 15:16:51.970515', 21, 22, 4);
INSERT INTO `comments` VALUES (23, '1111', '2026-04-28 13:22:56.690432', NULL, 24, 4);
INSERT INTO `comments` VALUES (24, '111111', '2026-05-04 00:45:20.120911', NULL, 27, 4);
INSERT INTO `comments` VALUES (25, '2222', '2026-05-04 00:45:23.978433', 24, 27, 4);
INSERT INTO `comments` VALUES (26, '33333', '2026-05-04 01:11:34.632783', NULL, 27, 4);
INSERT INTO `comments` VALUES (27, '4444', '2026-05-04 01:31:49.732102', NULL, 27, 4);
INSERT INTO `comments` VALUES (28, '44444', '2026-05-04 01:34:22.852263', NULL, 27, 4);
INSERT INTO `comments` VALUES (29, '11111', '2026-05-04 19:09:31.778845', NULL, 32, 4);
INSERT INTO `comments` VALUES (30, '11111', '2026-05-06 20:47:58.409430', NULL, 35, 4);
INSERT INTO `comments` VALUES (31, '11111', '2026-05-07 22:41:58.184782', NULL, 33, 1);
INSERT INTO `comments` VALUES (32, 'test\ntest', '2026-05-08 18:53:55.433312', NULL, 33, 1);
INSERT INTO `comments` VALUES (33, '1111', '2026-05-08 18:54:00.053925', 31, 33, 1);
INSERT INTO `comments` VALUES (34, '2222222', '2026-05-08 18:54:48.867479', 31, 33, 4);
INSERT INTO `comments` VALUES (35, '33333', '2026-05-08 18:54:56.747094', 31, 33, 4);

-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `target_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `target_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  CONSTRAINT `favorites_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorites
-- ----------------------------
INSERT INTO `favorites` VALUES (3, 4, 'POST', 35, '2026-05-06 20:25:16');
INSERT INTO `favorites` VALUES (5, 4, 'PET', 42, '2026-05-06 20:42:51');
INSERT INTO `favorites` VALUES (6, 4, 'PET', 41, '2026-05-06 20:42:52');
INSERT INTO `favorites` VALUES (8, 4, 'PET', 45, '2026-05-06 20:47:33');
INSERT INTO `favorites` VALUES (9, 4, 'PET', 32, '2026-05-06 21:02:24');
INSERT INTO `favorites` VALUES (10, 4, 'PET', 47, '2026-05-06 22:34:43');
INSERT INTO `favorites` VALUES (11, 1, 'POST', 33, '2026-05-07 22:41:54');
INSERT INTO `favorites` VALUES (12, 1, 'PET', 48, '2026-05-07 23:27:17');
INSERT INTO `favorites` VALUES (13, 1, 'PET', 47, '2026-05-07 23:27:18');
INSERT INTO `favorites` VALUES (14, 1, 'PET', 45, '2026-05-07 23:27:19');
INSERT INTO `favorites` VALUES (15, 1, 'PET', 42, '2026-05-07 23:27:20');
INSERT INTO `favorites` VALUES (17, 4, 'PET', 48, '2026-05-09 19:53:13');
INSERT INTO `favorites` VALUES (18, 4, 'PET', 40, '2026-05-09 19:53:18');
INSERT INTO `favorites` VALUES (23, 4, 'POST', 33, '2026-05-21 20:24:54');

-- ----------------------------
-- Table structure for help_posts
-- ----------------------------
DROP TABLE IF EXISTS `help_posts`;
CREATE TABLE `help_posts`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `status` enum('APPROVED','PENDING','REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `related_pet_id` bigint NULL DEFAULT NULL,
  `city` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `photo_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `reject_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK1iwg80tbxonhc5ae2dcso9vx9`(`user_id` ASC) USING BTREE,
  INDEX `FKn9oao91qyo7sgyq4d4ey16ju6`(`related_pet_id` ASC) USING BTREE,
  CONSTRAINT `FK1iwg80tbxonhc5ae2dcso9vx9` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKn9oao91qyo7sgyq4d4ey16ju6` FOREIGN KEY (`related_pet_id`) REFERENCES `pets` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of help_posts
-- ----------------------------
INSERT INTO `help_posts` VALUES (1, '物资共享', '帖子测试', '2026-04-07 00:44:10.258291', '帖子测试', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (2, '医疗咨询', '帖子测试', '2026-04-07 00:50:02.941971', '再次测试', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (3, '物资共享', '多余物资分享，有意者请联系：ddddddd', '2026-04-08 00:11:27.191777', '多余物资分享', 1, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (4, '医疗咨询', '111111', '2026-04-09 21:53:12.720558', '1111', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (5, '物资共享', '222222', '2026-04-09 21:53:20.230512', '2222', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (6, '物资共享', '3333333', '2026-04-09 21:53:29.334716', '3333', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (7, '经验分享', '555555', '2026-04-09 21:54:58.985430', '5555', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (8, '物资共享', '666666', '2026-04-09 22:07:01.321204', '66666', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (9, '物资共享', '77777777', '2026-04-09 22:07:07.568883', '77777', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (10, '医疗咨询', '8888888', '2026-04-09 22:07:13.705480', '8888', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (11, '物资共享', '999999', '2026-04-09 22:07:18.840937', '9999', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (12, '物资共享', '3412432', '2026-04-09 22:07:26.512376', '13234', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (13, '物资共享', '3321321321', '2026-04-09 22:07:40.673781', '2312312', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (14, '物资共享', '3123123213', '2026-04-09 22:07:46.954487', '1231231', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (15, '物资共享', '435345', '2026-04-09 22:07:52.706596', '542545', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (16, '经验分享', '6575677', '2026-04-09 22:07:57.538503', '65765', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (17, '医疗咨询', '143143124', '2026-04-10 18:59:06.314976', '332525', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (18, '物资共享', 'asfasfdas', '2026-04-10 20:12:46.871569', 'asdasfsd', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (19, '医疗咨询', 'dasdasdas', '2026-04-10 20:12:52.127664', 'asdasd', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (21, '医疗咨询', 'dSDAS', '2026-04-10 20:13:04.808129', 'fsdfsd', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (22, '物资共享', '测试测试', '2026-04-18 23:34:48.654186', '测试', 4, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (24, '经验分享', '测试测试测试', '2026-04-28 12:51:11.668398', '审核测试', 1, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (25, '经验分享', '测试', '2026-04-28 12:52:38.871113', '拒绝测试', 2, 'REJECTED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (26, '物资共享', '测试', '2026-04-28 13:03:41.983039', '二次测试', 1, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (27, '物资共享', '111', '2026-04-28 13:56:50.542534', '通过测试', 1, 'APPROVED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (28, '物资共享', '222', '2026-04-28 13:56:58.182034', '拒绝测试', 1, 'REJECTED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (29, '物资共享', '111111', '2026-04-28 14:58:18.991331', '二次通过测试', 1, 'PENDING', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (30, '物资共享', '22222', '2026-04-28 14:58:26.519545', '二次拒绝测试', 1, 'REJECTED', NULL, NULL, NULL, NULL);
INSERT INTO `help_posts` VALUES (31, '经验分享', '发布逻辑更新', '2026-05-04 16:46:43.461705', '更新发布逻辑', 4, 'APPROVED', NULL, '湖南省/湘潭市/雨湖区', '[\"/uploads/0ed94afa-103f-4d73-b1ee-12380a731877.png\"]', NULL);
INSERT INTO `help_posts` VALUES (32, '经验分享', '多图排版测试', '2026-05-04 17:47:43.422768', '多图测试', 4, 'APPROVED', NULL, '北京市/市辖区/东城区', '[\"/uploads/e4f05dcf-934a-4c42-9cdd-c5be15d635fc.png\",\"/uploads/77ce0250-7c08-4092-b094-4c048b77b602.png\",\"/uploads/a517f2c1-b8e2-4f83-92ac-d8303a0977ad.png\",\"/uploads/b6f97f48-01ed-4c83-9e5d-5c811410c290.png\",\"/uploads/86b23d68-a513-49eb-aa29-6ed571122996.png\"]', NULL);
INSERT INTO `help_posts` VALUES (33, '物资共享', '关联测试', '2026-05-05 21:30:20.006739', '模块关联测试', 4, 'APPROVED', 45, '北京市/市辖区/东城区', '[\"/uploads/ff302863-2b4c-4ba8-89d3-fa5f2c0af835.png\"]', NULL);
INSERT INTO `help_posts` VALUES (34, '寻宠启事', '测试', '2026-05-05 21:56:18.382143', '二次多图测试', 4, 'REJECTED', 45, '内蒙古自治区/通辽市/开鲁县', '[\"/uploads/5a728f07-e687-41e0-b5d7-e1fda77cb228.png\"]', NULL);
INSERT INTO `help_posts` VALUES (35, '求助信息', '测试', '2026-05-05 21:57:11.730761', '二次多图测试', 4, 'APPROVED', 45, '北京市/市辖区/朝阳区', '[\"/uploads/50f2ca88-18ea-4ea8-baea-1239233c7b25.png\",\"/uploads/c069cfa0-453e-4bd3-b319-4b7d4fcae51e.png\"]', NULL);
INSERT INTO `help_posts` VALUES (36, '物资共享', '1111111', '2026-05-06 23:11:02.035819', '11111', 4, 'REJECTED', NULL, '内蒙古自治区/通辽市/库伦旗', '[\"/uploads/421534ad-dd5e-46b7-9d4e-5ec9dfe8c431.png\"]', '图片不合理');
INSERT INTO `help_posts` VALUES (37, '物资共享', '22222222', '2026-05-06 23:24:01.240255', '2222222', 4, 'REJECTED', NULL, '河北省/秦皇岛市/北戴河区', '[\"/uploads/d094cfd6-d059-41b0-a302-b18002c9eda4.png\",\"/uploads/8a5d770e-04e9-4a56-a80a-1723b8924058.png\",\"/uploads/75cf8475-1a41-4e9c-ab3b-dda01580d5a9.png\"]', '拒绝测试');
INSERT INTO `help_posts` VALUES (38, '经验分享', '测试', '2026-05-06 23:52:50.006979', '完结测试', 4, 'PENDING', NULL, '湖南省/湘潭市/雨湖区', '[\"/uploads/b087c93b-51cf-415b-8ed7-02ea7dd5a25f.png\",\"/uploads/25326d07-e764-407b-bbbc-85e3a932a1d3.png\",\"/uploads/51085c74-0118-4407-a705-ac7d1964d79e.png\",\"/uploads/52fffe35-12ed-4043-9c09-40858b754695.png\"]', NULL);

-- ----------------------------
-- Table structure for like_records
-- ----------------------------
DROP TABLE IF EXISTS `like_records`;
CREATE TABLE `like_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `target_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `target_id` bigint NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  CONSTRAINT `like_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of like_records
-- ----------------------------
INSERT INTO `like_records` VALUES (3, 4, 'POST', 35, '2026-05-06 20:25:15');
INSERT INTO `like_records` VALUES (4, 1, 'POST', 33, '2026-05-07 22:41:53');
INSERT INTO `like_records` VALUES (7, 4, 'POST', 33, '2026-05-21 20:24:53');

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint NOT NULL,
  `receiver_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `read` tinyint(1) NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT NULL,
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK_messages_sender`(`sender_id` ASC) USING BTREE,
  INDEX `FK_messages_receiver`(`receiver_id` ASC) USING BTREE,
  CONSTRAINT `FK_messages_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK_messages_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES (1, 1, 4, '111111', 0, '2026-05-07 23:55:06', NULL);
INSERT INTO `messages` VALUES (2, 1, 4, '2222222', 0, '2026-05-08 00:25:16', NULL);
INSERT INTO `messages` VALUES (3, 4, 1, '111111', 0, '2026-05-08 00:30:54', NULL);
INSERT INTO `messages` VALUES (4, 1, 4, '21212121212', 0, '2026-05-08 18:45:42', NULL);
INSERT INTO `messages` VALUES (5, 1, 4, '测试测试测试', 0, '2026-05-08 18:53:26', NULL);
INSERT INTO `messages` VALUES (7, 4, 1, '', 0, '2026-05-11 23:43:16', '/uploads/1df35cc2-d951-4474-ab22-31f2a33cdf6b.jpg');
INSERT INTO `messages` VALUES (8, 4, 1, '', 0, '2026-05-11 23:43:54', '/uploads/625c9112-0449-43c5-8530-20e95aa06db7.jpg');
INSERT INTO `messages` VALUES (9, 4, 1, '', 0, '2026-05-11 23:50:24', '/uploads/48421b25-5f5c-4da8-b1c8-306dc1d2850e.jpg');
INSERT INTO `messages` VALUES (10, 4, 1, '11111', 0, '2026-05-11 23:50:52', '/uploads/272f9d39-f8ab-42c1-8087-7608ce2a11da.jpg');
INSERT INTO `messages` VALUES (11, 4, 1, '2222', 0, '2026-05-11 23:50:56', NULL);
INSERT INTO `messages` VALUES (12, 4, 1, '1111', 0, '2026-05-11 23:59:54', NULL);

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `read` bit(1) NULL DEFAULT NULL,
  `related_id` bigint NULL DEFAULT NULL,
  `sender_username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK9y21adhxn0ayjhfocscqox7bh`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK9y21adhxn0ayjhfocscqox7bh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notifications
-- ----------------------------
INSERT INTO `notifications` VALUES (1, 'user1 给你发了一条消息：2222222', '2026-05-08 00:25:15.690307', b'1', 2, 'user1', '收到新私信', 'MESSAGE', 4);
INSERT INTO `notifications` VALUES (2, 'admin 给你发了一条消息：111111', '2026-05-08 00:30:53.568773', b'1', 3, 'admin', '收到新私信', 'MESSAGE', 1);
INSERT INTO `notifications` VALUES (3, 'user1 给你发了一条消息：21212121212', '2026-05-08 18:45:41.806810', b'1', 4, 'user1', '收到新私信', 'MESSAGE', 4);
INSERT INTO `notifications` VALUES (4, 'user1 给你发了一条消息：测试测试测试', '2026-05-08 18:53:25.840485', b'1', 5, 'user1', '收到新私信', 'MESSAGE', 4);
INSERT INTO `notifications` VALUES (5, 'user1 评论了你的帖子《模块关联测试》：test\ntest', '2026-05-08 18:53:55.441306', b'1', 32, 'user1', '收到新评论', 'COMMENT', 4);
INSERT INTO `notifications` VALUES (6, 'user1 评论了你的帖子《模块关联测试》：1111', '2026-05-08 18:54:00.064597', b'1', 33, 'user1', '收到新评论', 'COMMENT', 4);
INSERT INTO `notifications` VALUES (7, '删除 评论了你的帖子《测试》：1111111', '2026-05-10 21:01:44.776708', b'1', 36, '删除', '收到新评论', 'COMMENT', 4);
INSERT INTO `notifications` VALUES (8, '删除 给你发了一条消息：hhh\n', '2026-05-10 21:02:07.402137', b'1', 6, '删除', '收到新私信', 'MESSAGE', 4);
INSERT INTO `notifications` VALUES (9, '您提交的领养申请未通过审核。拒绝理由：111', '2026-05-11 21:27:45.131732', b'0', 29, '系统管理员', '领养申请未通过', 'ADOPTION_REVIEW', 5);
INSERT INTO `notifications` VALUES (10, '您申请的宠物【测试标签宠物】已被其他用户领养，感谢您的关注！', '2026-05-11 21:27:49.059103', b'1', 29, '系统管理员', '领养申请未通过', 'ADOPTION_REVIEW', 2);
INSERT INTO `notifications` VALUES (11, '恭喜！您的领养申请已通过，请联系救助者完成领养手续。', '2026-05-11 21:27:49.071157', b'1', 29, '系统管理员', '领养申请通过', 'ADOPTION_REVIEW', 1);
INSERT INTO `notifications` VALUES (12, '您发布的宠物【12121】未通过审核。拒绝理由：图片不清晰', '2026-05-11 21:28:34.791791', b'1', 56, '系统管理员', '宠物审核未通过', 'PET_REVIEW', 1);
INSERT INTO `notifications` VALUES (13, '【user1】提交了宠物【测试3】的回访反馈', '2026-05-11 22:15:03.318946', b'1', 9, '系统', '新回访反馈', 'VISIT_NOTICE', 4);
INSERT INTO `notifications` VALUES (14, 'admin 给你发了一条消息：[图片]', '2026-05-11 23:43:16.369201', b'0', 7, 'admin', '收到新私信', 'MESSAGE', 1);
INSERT INTO `notifications` VALUES (15, 'admin 给你发了一条消息：[图片]', '2026-05-11 23:43:54.362253', b'0', 8, 'admin', '收到新私信', 'MESSAGE', 1);
INSERT INTO `notifications` VALUES (16, 'admin 给你发了一条消息：[图片]', '2026-05-11 23:50:23.674665', b'0', 9, 'admin', '收到新私信', 'MESSAGE', 1);
INSERT INTO `notifications` VALUES (17, 'admin 给你发了一条消息：[图片]', '2026-05-11 23:50:51.836402', b'0', 10, 'admin', '收到新私信', 'MESSAGE', 1);
INSERT INTO `notifications` VALUES (18, 'admin 给你发了一条消息：2222', '2026-05-11 23:50:56.158684', b'0', 11, 'admin', '收到新私信', 'MESSAGE', 1);
INSERT INTO `notifications` VALUES (19, 'admin 给你发了一条消息：1111', '2026-05-11 23:59:54.115274', b'0', 12, 'admin', '收到新私信', 'MESSAGE', 1);

-- ----------------------------
-- Table structure for pets
-- ----------------------------
DROP TABLE IF EXISTS `pets`;
CREATE TABLE `pets`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `age` int NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gender` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `photo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` enum('ADOPTED','AVAILABLE') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `rescuer_id` bigint NULL DEFAULT NULL,
  `photo_urls` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `review_status` enum('APPROVED','PENDING','REJECTED') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `dewormed` bit(1) NOT NULL,
  `neutered` bit(1) NOT NULL,
  `vaccinated` bit(1) NOT NULL,
  `reject_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKeurwemc7qrjepbga0s897fc4`(`rescuer_id` ASC) USING BTREE,
  CONSTRAINT `FKeurwemc7qrjepbga0s897fc4` FOREIGN KEY (`rescuer_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 57 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pets
-- ----------------------------
INSERT INTO `pets` VALUES (1, 2, '2026-04-03 12:43:36.004348', '性格温顺，已绝育，健康良好', '公', '小狗土豆', NULL, 'ADOPTED', '狗', 1, NULL, NULL, 'APPROVED', b'1', b'0', b'1', NULL);
INSERT INTO `pets` VALUES (2, 2, '2026-04-03 23:19:40.547925', '性格温顺，已绝育，健康良好', '公', '小猫番茄', NULL, 'ADOPTED', '狗', 1, NULL, NULL, 'APPROVED', b'0', b'0', b'1', NULL);
INSERT INTO `pets` VALUES (3, 2, '2026-04-05 12:59:29.717616', '温顺亲人', '公', '面包', NULL, 'ADOPTED', '猫', 1, NULL, NULL, 'APPROVED', b'0', b'1', b'0', NULL);
INSERT INTO `pets` VALUES (4, 3, '2026-04-05 13:06:13.874128', '温顺哈士奇', '母', '发呆', NULL, 'ADOPTED', '狗', 1, NULL, NULL, 'APPROVED', b'1', b'0', b'0', NULL);
INSERT INTO `pets` VALUES (5, 2, '2026-04-05 23:54:39.144680', '性格温顺，已绝育，健康良好', '公', '小狗豆豆', NULL, 'ADOPTED', '狗', 1, NULL, NULL, 'APPROVED', b'0', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (7, 3, '2026-04-06 00:48:29.674901', '很好', '公', '某某', NULL, 'ADOPTED', '猫', 1, NULL, NULL, 'APPROVED', b'0', b'0', b'0', NULL);
INSERT INTO `pets` VALUES (8, 4, '2026-04-06 01:23:44.728467', '健康，性格温和', '公', '土豆', '/uploads/d7e6fb6c-5f36-4c07-a7f2-9d36417d29a7.jpg', 'AVAILABLE', '猫', 4, '[\"/uploads/d7e6fb6c-5f36-4c07-a7f2-9d36417d29a7.jpg\",\"/uploads/614b9165-4e14-4e5b-ba1a-a50695ed7a91.jpg\"]', NULL, 'APPROVED', b'0', b'1', b'0', NULL);
INSERT INTO `pets` VALUES (9, 4, '2026-04-06 23:39:53.108765', '无恙', '公', '一', '/uploads/28dce1fb-c5ab-469f-aefa-e3a81a22b28e.jpg', 'AVAILABLE', '猫', 4, '[\"/uploads/28dce1fb-c5ab-469f-aefa-e3a81a22b28e.jpg\",\"/uploads/ab053c8b-35c1-4a65-a462-8c006fdba1dc.jpg\",\"/uploads/d324c345-96ed-4925-b5ac-5690ef1df181.jpg\"]', NULL, 'APPROVED', b'0', b'0', b'1', NULL);
INSERT INTO `pets` VALUES (10, 2, '2026-04-06 23:40:21.320181', '测试', '公', '二', '/uploads/3be84060-ce12-4bee-8d01-f1d3c01271f9.jpg', 'AVAILABLE', '猫', 4, '[\"/uploads/3be84060-ce12-4bee-8d01-f1d3c01271f9.jpg\"]', NULL, 'APPROVED', b'1', b'0', b'0', NULL);
INSERT INTO `pets` VALUES (11, 4, '2026-04-06 23:41:01.152733', 'aaaa', '公', '三', '/uploads/f3fc6f66-59bb-47a4-a492-d2df70e62fea.jpg', 'AVAILABLE', '猫', 4, '[\"/uploads/f3fc6f66-59bb-47a4-a492-d2df70e62fea.jpg\"]', NULL, 'APPROVED', b'0', b'1', b'0', NULL);
INSERT INTO `pets` VALUES (12, 1, '2026-04-07 23:30:00.389843', '444', '公', '四', '/uploads/eebdd4e9-3d1d-4d42-903a-f93dea806c72.jpg', 'AVAILABLE', '猫', 4, '[\"/uploads/eebdd4e9-3d1d-4d42-903a-f93dea806c72.jpg\"]', NULL, 'APPROVED', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (13, 2, '2026-04-07 23:30:23.496375', '555', '公', '五', '/uploads/3d2e1e9f-0154-4015-bab6-db03ad000ea0.jpg', 'AVAILABLE', '狗', 4, '[\"/uploads/3d2e1e9f-0154-4015-bab6-db03ad000ea0.jpg\",\"/uploads/6dae0a66-f656-42ea-9d6d-bec575848669.jpg\"]', NULL, 'APPROVED', b'0', b'0', b'1', NULL);
INSERT INTO `pets` VALUES (14, 1, '2026-04-07 23:30:51.519922', '666', '公', '六', '/uploads/ef5a5ee9-9b2e-473c-8670-addc50968874.jpg', 'AVAILABLE', '狗', 4, '[\"/uploads/ef5a5ee9-9b2e-473c-8670-addc50968874.jpg\"]', NULL, 'APPROVED', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (15, 1, '2026-04-07 23:31:16.122596', '777', '公', '七', '/uploads/4a1e6fdb-2813-4923-98e9-50b8ed7dcf6d.jpg', 'AVAILABLE', '猫', 4, '[\"/uploads/4a1e6fdb-2813-4923-98e9-50b8ed7dcf6d.jpg\"]', NULL, 'APPROVED', b'0', b'0', b'1', NULL);
INSERT INTO `pets` VALUES (16, 2, '2026-04-07 23:31:35.418069', '888', '公', '八', '/uploads/0f05501b-8252-4c43-8c6e-07559751119a.jpg', 'AVAILABLE', '猫', 4, '[\"/uploads/0f05501b-8252-4c43-8c6e-07559751119a.jpg\"]', NULL, 'APPROVED', b'1', b'1', b'0', NULL);
INSERT INTO `pets` VALUES (17, 2, '2026-04-07 23:33:21.197997', '999', '公', '九', '/uploads/784e6502-2d0a-4793-bb24-25f2dc7bee68.jpg', 'AVAILABLE', '猫', 4, '[\"/uploads/784e6502-2d0a-4793-bb24-25f2dc7bee68.jpg\"]', NULL, 'APPROVED', b'0', b'0', b'1', NULL);
INSERT INTO `pets` VALUES (18, 1, '2026-04-07 23:55:17.650678', '测试', '公', '测试', NULL, 'ADOPTED', '猫', 4, NULL, NULL, 'APPROVED', b'1', b'0', b'0', NULL);
INSERT INTO `pets` VALUES (20, 2, '2026-04-09 20:32:41.034717', '删除', '公', '删除', NULL, 'AVAILABLE', '猫', 1, NULL, NULL, 'APPROVED', b'0', b'1', b'0', NULL);
INSERT INTO `pets` VALUES (21, 1, '2026-04-09 21:35:40.852807', '测试1', '公', '测试1', NULL, 'AVAILABLE', '猫', 4, NULL, NULL, 'APPROVED', b'1', b'0', b'1', NULL);
INSERT INTO `pets` VALUES (23, 1, '2026-04-09 21:36:00.516663', '测试3', '公', '测试3', NULL, 'ADOPTED', '猫', 4, NULL, NULL, 'APPROVED', b'0', b'1', b'0', NULL);
INSERT INTO `pets` VALUES (24, 3, '2026-04-10 22:40:25.445588', '无', '公', '测试测试', NULL, 'ADOPTED', '猫', 1, NULL, NULL, 'APPROVED', b'0', b'0', b'1', NULL);
INSERT INTO `pets` VALUES (25, 2, '2026-04-12 15:47:32.076708', 'test222', '公', 'test22', '/uploads/96f2645c-fe62-4d37-bcd6-5e11d142c0c7.png', 'AVAILABLE', '其他', 4, '[\"/uploads/96f2645c-fe62-4d37-bcd6-5e11d142c0c7.png\"]', NULL, 'APPROVED', b'0', b'0', b'0', NULL);
INSERT INTO `pets` VALUES (29, 3, '2026-04-23 17:21:56.963678', '活泼亲人测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试', '公', '测试标签宠物', NULL, 'ADOPTED', '猫', 4, NULL, '[\"活泼\", \"亲人\"]', 'APPROVED', b'0', b'0', b'0', NULL);
INSERT INTO `pets` VALUES (30, 3, '2026-04-28 12:49:34.799634', '测试测试测试', '公', '宠物审核测试', NULL, 'ADOPTED', '猫', 1, NULL, '[\"高冷\",\"活泼\"]', 'APPROVED', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (31, 3, '2026-04-28 12:52:26.428674', '111', '母', '宠物拒绝测试', NULL, 'AVAILABLE', '狗', 2, NULL, '[\"活泼\"]', 'REJECTED', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (32, 2, '2026-04-28 13:03:28.806314', '324', '公', '二次测试', NULL, 'AVAILABLE', '猫', 1, NULL, '[\"活泼\"]', 'APPROVED', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (33, 2, '2026-04-28 13:25:04.450766', '111222', '公', 'test', '/uploads/f50b26c4-8e92-43e7-9296-9f32ce732c51.png', 'AVAILABLE', '猫', 4, '[\"/uploads/f50b26c4-8e92-43e7-9296-9f32ce732c51.png\"]', '[\"活泼\"]', 'APPROVED', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (34, 3, '2026-04-28 13:56:24.781504', '111', '公', '通过测试', NULL, 'ADOPTED', '猫', 1, NULL, '[\"安静\"]', 'APPROVED', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (35, 3, '2026-04-28 13:56:39.588589', '222', '母', '拒绝测试', NULL, 'AVAILABLE', '狗', 1, NULL, '[\"亲人\"]', 'REJECTED', b'0', b'0', b'0', NULL);
INSERT INTO `pets` VALUES (37, 2, '2026-04-28 14:57:52.918329', '21212', '公', '二次通过测试', NULL, 'ADOPTED', '猫', 1, NULL, '[\"活泼\"]', 'APPROVED', b'0', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (38, 2, '2026-04-28 14:58:06.558725', '132423', '公', '二次拒绝测试', NULL, 'AVAILABLE', '狗', 1, NULL, '[\"亲人\"]', 'REJECTED', b'1', b'1', b'0', NULL);
INSERT INTO `pets` VALUES (39, 2, '2026-04-30 02:51:05.768867', '新版页面测试', '公', '新版测试', '/uploads/32d5398c-74d6-4461-b3c4-859d65dce71b.png', 'AVAILABLE', '狗', 4, '[\"/uploads/32d5398c-74d6-4461-b3c4-859d65dce71b.png\"]', '[\"贪吃\"]', 'APPROVED', b'0', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (40, 4, '2026-04-30 09:39:12.792499', '活泼', '公', '旺财', '/uploads/f2fa78e9-1c37-4148-a70d-1a6e6a080b58.png', 'AVAILABLE', '狗', 4, '[\"/uploads/f2fa78e9-1c37-4148-a70d-1a6e6a080b58.png\"]', '[\"爱跑\"]', 'APPROVED', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (41, 1, '2026-04-30 09:39:52.870535', '温顺小猫', '母', '小咪', '/uploads/a49832b6-e40b-43ed-ae7a-3a123de3ea93.png', 'AVAILABLE', '猫', 4, '[\"/uploads/a49832b6-e40b-43ed-ae7a-3a123de3ea93.png\"]', '[\"安静\"]', 'APPROVED', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (42, 0, '2026-04-30 09:40:25.481947', '很乖的一只小猫', '母', '小乖', '/uploads/243d6b5d-2168-4124-b055-62c753b89e66.png', 'AVAILABLE', '猫', 4, '[\"/uploads/243d6b5d-2168-4124-b055-62c753b89e66.png\"]', '[\"亲人\"]', 'APPROVED', b'1', b'0', b'1', NULL);
INSERT INTO `pets` VALUES (43, 3, '2026-05-01 02:41:36.722532', '1111', '公', 'testcat', NULL, 'AVAILABLE', '猫', 4, NULL, '[\"亲人\"]', 'REJECTED', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (45, 2, '2026-05-05 15:01:20.332426', '关联测试', '母', '关联测试', '/uploads/9b96d27b-a8f8-4a94-9c81-84e77f5a75c7.png', 'AVAILABLE', '狗', 4, '[\"/uploads/9b96d27b-a8f8-4a94-9c81-84e77f5a75c7.png\"]', '[]', 'APPROVED', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (46, 1, '2026-05-05 15:15:54.046860', '111', '公', '111', NULL, 'AVAILABLE', '11111', 4, NULL, '[\"亲人\"]', 'PENDING', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (47, 4, '2026-05-05 15:18:10.674215', '内容更新测试', '公', '222', '/uploads/d1c63d7e-ba75-430f-845b-2026be4d2005.png', 'AVAILABLE', '猫', 4, NULL, '[]', 'APPROVED', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (48, 0, '2026-05-06 22:41:17.571213', '温顺', '母', '西', '/uploads/eec5f676-7391-4ec8-94ac-8db41eeb09c1.png', 'AVAILABLE', '猫', 4, '[\"/uploads/eec5f676-7391-4ec8-94ac-8db41eeb09c1.png\"]', '[\"安静\"]', 'APPROVED', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (49, 2, '2026-05-06 22:59:25.373994', '3213', '母', '28338', NULL, 'AVAILABLE', '2314', 4, NULL, '[\"高冷\"]', 'PENDING', b'0', b'0', b'1', NULL);
INSERT INTO `pets` VALUES (50, 2, '2026-05-06 23:52:04.942797', '测试', '公', '完结测试', '/uploads/688a9638-635e-42f6-8258-b9d9e19a45f5.png', 'AVAILABLE', '猫', 4, '[\"/uploads/688a9638-635e-42f6-8258-b9d9e19a45f5.png\"]', '[\"测试\"]', 'PENDING', b'1', b'1', b'1', NULL);
INSERT INTO `pets` VALUES (56, 2, '2026-05-11 21:19:51.402567', '21212', '母', '12121', NULL, 'AVAILABLE', '1212', 1, NULL, '[\"高冷\"]', 'REJECTED', b'0', b'1', b'0', '图片不清晰');

-- ----------------------------
-- Table structure for user_follows
-- ----------------------------
DROP TABLE IF EXISTS `user_follows`;
CREATE TABLE `user_follows`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `follower_id` bigint NOT NULL,
  `following_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKqx9mu1fniaua5jfe1cdyspxdt`(`follower_id` ASC) USING BTREE,
  INDEX `FKp1rxuw1ulwo6mu84qaajuttrk`(`following_id` ASC) USING BTREE,
  CONSTRAINT `FKp1rxuw1ulwo6mu84qaajuttrk` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKqx9mu1fniaua5jfe1cdyspxdt` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_follows
-- ----------------------------
INSERT INTO `user_follows` VALUES (3, '2026-05-07 23:13:13.212674', 1, 4);
INSERT INTO `user_follows` VALUES (4, '2026-05-08 00:31:37.601175', 4, 1);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `real_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `role` enum('RESCUER','ADOPTER','ADMIN','USER') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `bio` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKr43af9ap4edm43mmtq01oddj6`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, NULL, '2026-04-03 12:32:59.084825', '$2a$10$spSJyjdbaUjYJ.eQbfBPbuvTHEDiD1H5MAYTVEn/FLDNn8SPSHIv.', '13800138001', '张一', 'USER', 'user1', '/uploads/d1070856-e4ec-49d0-acee-941f8984ab64.png', '用户1个人简介');
INSERT INTO `users` VALUES (2, NULL, '2026-04-03 12:32:59.692373', '$2a$10$tJPZoOw2zxSuU.QN33WqpuOylg5RZCIqab8Mv5Hw/AmPY5SM.PDWK', '13800138002', '李二', 'USER', 'user2', NULL, NULL);
INSERT INTO `users` VALUES (3, NULL, '2026-04-05 23:22:19.604283', '$2a$10$jUqVay1nxQZdYIb9QEJ.feJq9DtHLpLyRjYDDia/g4r6sr9TDrbNe', '12345678912', '测试', 'USER', 'testuser', NULL, NULL);
INSERT INTO `users` VALUES (4, NULL, '2026-04-05 23:54:39.058756', '$2a$10$CEbv1Op0bzKQHGA6GmRJqedGfLIF0W9Poe9JMvADOMfwpJLUoejdC', '13800138000', '系统管理员', 'ADMIN', 'admin', '/uploads/b416caf4-aeea-4759-aa27-a1381b4da6f8.jpg', '管理员个人简介');
INSERT INTO `users` VALUES (5, NULL, '2026-04-09 20:38:32.762881', '$2a$10$.myypVSxIca9t./JipilUOz45rzuWHUvd3heNs2NZnbqGFDncl/SS', '13156458765', '李测试', 'USER', 'user3', NULL, NULL);
INSERT INTO `users` VALUES (6, NULL, '2026-04-18 19:45:04.867264', '$2a$10$AiGl1oAqtkFNzfjl/4kzou7uqSc5jXrLWJEFNCAjLqcQNCi1E.LD.', '12345241461', 'user99', 'USER', 'user99', NULL, NULL);
INSERT INTO `users` VALUES (7, NULL, '2026-04-18 22:49:48.028218', '$2a$10$TZjj1jQV2/5YT/xupjCmhuBJi2dxFWC2BYQ.dsdunCrpTY60XhjF.', '18357265878', 'Jack', 'USER', 'user4', NULL, NULL);
INSERT INTO `users` VALUES (10, NULL, '2026-04-18 23:06:37.241143', '$2a$10$/DZ9p/chKhHyBQDEmYGoJeWvDa13cRINP/0f6ctTvIQBBRoiDqRxG', '14369235631', '程某', 'USER', 'user5', NULL, NULL);

-- ----------------------------
-- Table structure for visit_records
-- ----------------------------
DROP TABLE IF EXISTS `visit_records`;
CREATE TABLE `visit_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `feedback` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `need_follow_up` bit(1) NULL DEFAULT NULL,
  `next_visit_time` datetime(6) NULL DEFAULT NULL,
  `pet_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `visit_time` datetime(6) NOT NULL,
  `visit_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `application_id` bigint NOT NULL,
  `visitor_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK7soekd7uxl5y8juxmpqcuiqvb`(`application_id` ASC) USING BTREE,
  INDEX `FK20gs2knq8pbv5xhtmvmw2jva2`(`visitor_id` ASC) USING BTREE,
  CONSTRAINT `FK20gs2knq8pbv5xhtmvmw2jva2` FOREIGN KEY (`visitor_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FK7soekd7uxl5y8juxmpqcuiqvb` FOREIGN KEY (`application_id`) REFERENCES `adoption_applications` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of visit_records
-- ----------------------------
INSERT INTO `visit_records` VALUES (1, '回访测试', '2026-04-10 20:05:07.636271', '回访测试', b'0', NULL, 'HEALTHY', '2026-04-10 09:10:00.000000', 'PHONE', 9, 4);
INSERT INTO `visit_records` VALUES (5, '111', '2026-04-18 21:50:28.749392', '1111111', b'0', NULL, 'ISSUES', '2026-04-18 13:50:23.000000', 'ONLINE', 20, 4);
INSERT INTO `visit_records` VALUES (7, '无事', '2026-04-30 03:12:27.143155', '无意见', b'0', NULL, 'HEALTHY', '2026-04-29 19:12:04.000000', 'ONLINE', 1, 4);
INSERT INTO `visit_records` VALUES (8, '1111', '2026-05-11 21:13:55.948018', '1111', b'1', '2026-05-13 00:00:00.000000', 'ADAPTING', '2026-05-11 13:13:30.000000', 'ONLINE', 34, 4);
INSERT INTO `visit_records` VALUES (9, '很好', '2026-05-11 22:15:03.275625', '暂无', b'0', NULL, 'HEALTHY', '2026-05-11 22:15:03.275625', 'USER_FEEDBACK', 25, 1);

SET FOREIGN_KEY_CHECKS = 1;
