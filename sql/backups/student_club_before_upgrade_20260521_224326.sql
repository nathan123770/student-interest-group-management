-- MySQL dump 10.13  Distrib 8.0.27, for Win64 (x86_64)
--
-- Host: localhost    Database: student_club
-- ------------------------------------------------------
-- Server version	8.0.27

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL,
  `title` varchar(120) NOT NULL,
  `content` text,
  `location` varchar(120) DEFAULT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `max_participants` int NOT NULL DEFAULT '50',
  `current_participants` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1报名中 0取消 2结束',
  `creator_id` bigint NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_activity_group` (`group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity`
--

LOCK TABLES `activity` WRITE;
/*!40000 ALTER TABLE `activity` DISABLE KEYS */;
INSERT INTO `activity` VALUES (1,1,'Spring Boot 入门工作坊','从零搭建一个可运行的 Spring Boot 项目。','创新楼 302','2026-05-23 22:17:27','2026-05-23 22:17:27',30,2,1,2,'2026-05-16 22:17:27','2026-05-16 22:17:27',0),(2,2,'周末篮球友谊赛','轻松组队，欢迎报名。','体育馆','2026-05-26 22:17:27','2026-05-26 22:17:27',20,1,1,2,'2026-05-16 22:17:27','2026-05-16 22:17:27',0);
/*!40000 ALTER TABLE `activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_signup`
--

DROP TABLE IF EXISTS `activity_signup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_signup` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `signup_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审核 1通过 2拒绝',
  `reviewer_id` bigint DEFAULT NULL,
  `review_remark` varchar(500) DEFAULT NULL,
  `review_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_signup`
--

LOCK TABLES `activity_signup` WRITE;
/*!40000 ALTER TABLE `activity_signup` DISABLE KEYS */;
INSERT INTO `activity_signup` VALUES (1,1,3,'2026-05-16 22:17:27',1,NULL,NULL,NULL),(2,2,2,'2026-05-16 23:15:13',1,NULL,NULL,NULL),(3,1,2,'2026-05-16 23:50:52',1,NULL,NULL,NULL),(4,1,5,'2026-05-17 00:06:57',0,NULL,NULL,NULL);
/*!40000 ALTER TABLE `activity_signup` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_category`
--

DROP TABLE IF EXISTS `group_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(80) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `sort_order` int NOT NULL DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_category`
--

LOCK TABLES `group_category` WRITE;
/*!40000 ALTER TABLE `group_category` DISABLE KEYS */;
INSERT INTO `group_category` VALUES (1,'科技创新','编程、机器人、人工智能等科技类兴趣小组',1,1,'2026-05-16 22:17:27','2026-05-16 22:17:27',0),(2,'文艺体育','音乐、舞蹈、篮球、足球等文体类兴趣小组',1,2,'2026-05-16 22:17:27','2026-05-16 22:17:27',0),(3,'公益实践','志愿服务、社会实践与校园服务',1,3,'2026-05-16 22:17:27','2026-05-16 22:17:27',0);
/*!40000 ALTER TABLE `group_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_member`
--

DROP TABLE IF EXISTS `group_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `member_role` varchar(20) NOT NULL DEFAULT 'MEMBER',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_member` (`group_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_member`
--

LOCK TABLES `group_member` WRITE;
/*!40000 ALTER TABLE `group_member` DISABLE KEYS */;
INSERT INTO `group_member` VALUES (1,1,2,'LEADER','2026-05-16 22:17:27',1),(2,1,3,'MEMBER','2026-05-16 22:17:27',1),(3,2,2,'LEADER','2026-05-16 22:17:27',1),(4,1,5,'MEMBER','2026-05-16 23:39:22',0),(5,3,5,'LEADER','2026-05-17 14:22:39',1),(6,4,5,'LEADER','2026-05-17 14:52:28',1),(7,5,5,'LEADER','2026-05-17 14:52:28',1),(8,6,2,'LEADER','2026-05-17 14:52:28',1),(9,7,2,'LEADER','2026-05-17 14:52:28',1),(10,8,5,'LEADER','2026-05-17 14:52:28',1);
/*!40000 ALTER TABLE `group_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interest_group`
--

DROP TABLE IF EXISTS `interest_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interest_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `category_id` bigint NOT NULL,
  `leader_id` bigint NOT NULL,
  `cover_url` varchar(255) DEFAULT NULL,
  `description` text,
  `location` varchar(100) DEFAULT NULL,
  `max_members` int NOT NULL DEFAULT '50',
  `current_members` int NOT NULL DEFAULT '0',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审核 1通过 2拒绝',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1启用 0停用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_group_category` (`category_id`),
  KEY `idx_group_leader` (`leader_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interest_group`
--

LOCK TABLES `interest_group` WRITE;
/*!40000 ALTER TABLE `interest_group` DISABLE KEYS */;
INSERT INTO `interest_group` VALUES (1,'AI 编程社',1,2,'https://images.unsplash.com/photo-1515879218367-8466d910aaa4','一起学习 Java、Web 开发和人工智能应用。','创新楼 302',40,2,1,1,'2026-05-16 22:17:27','2026-05-16 22:17:27',0),(2,'校园篮球队',2,2,'https://images.unsplash.com/photo-1546519638-68e109498ffc','每周训练与校内联赛组织。','体育馆',30,1,1,1,'2026-05-16 22:17:27','2026-05-16 22:17:27',0),(3,'group1',1,5,'','group1','group1',30,1,1,1,'2026-05-17 14:22:04','2026-05-17 14:22:04',0),(4,'机器人创客社',1,5,'https://images.unsplash.com/photo-1518770660439-4636190af475','围绕机器人、传感器和智能硬件开展项目实践。','实验楼 204',35,1,1,1,'2026-05-17 14:52:28','2026-05-17 14:52:28',0),(5,'摄影与短片社',2,5,'https://images.unsplash.com/photo-1452587925148-ce544e77e70d','学习摄影构图、短片拍摄与校园影像创作。','艺术楼 105',30,1,1,1,'2026-05-17 14:52:28','2026-05-17 14:52:28',0),(6,'英语演讲社',2,2,'https://images.unsplash.com/photo-1475721027785-f74eccf877e2','通过主题演讲和辩论训练表达能力。','教学楼 B203',45,1,1,1,'2026-05-17 14:52:28','2026-05-17 14:52:28',0),(7,'绿色公益社',3,2,'https://images.unsplash.com/photo-1466611653911-95081537e5b7','组织环保宣传、志愿服务和校园公益行动。','综合楼 101',50,1,1,1,'2026-05-17 14:52:28','2026-05-17 14:52:28',0),(8,'数学建模社',1,5,'https://images.unsplash.com/photo-1509228468518-180dd4864904','一起训练建模思维、算法工具和竞赛协作。','创新楼 308',40,1,1,1,'2026-05-17 14:52:28','2026-05-17 14:52:28',0);
/*!40000 ALTER TABLE `interest_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `join_apply`
--

DROP TABLE IF EXISTS `join_apply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `join_apply` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审核 1通过 2拒绝',
  `reviewer_id` bigint DEFAULT NULL,
  `review_remark` varchar(500) DEFAULT NULL,
  `review_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_apply_group` (`group_id`),
  KEY `idx_apply_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `join_apply`
--

LOCK TABLES `join_apply` WRITE;
/*!40000 ALTER TABLE `join_apply` DISABLE KEYS */;
INSERT INTO `join_apply` VALUES (1,1,3,'希望学习更多 Web 开发知识。',1,2,'欢迎加入','2026-05-16 22:17:27','2026-05-16 22:17:27','2026-05-16 22:17:27',0),(2,1,5,'',1,2,'审核通过','2026-05-16 23:39:22','2026-05-16 23:38:52','2026-05-16 23:38:52',0),(3,4,6,'1',0,NULL,NULL,NULL,'2026-05-17 19:49:26','2026-05-17 19:49:26',0);
/*!40000 ALTER TABLE `join_apply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notice`
--

DROP TABLE IF EXISTS `notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(120) NOT NULL,
  `content` text,
  `notice_type` varchar(20) NOT NULL COMMENT 'SYSTEM或GROUP',
  `group_id` bigint DEFAULT NULL,
  `publisher_id` bigint NOT NULL,
  `top_flag` tinyint NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_notice_group` (`group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notice`
--

LOCK TABLES `notice` WRITE;
/*!40000 ALTER TABLE `notice` DISABLE KEYS */;
INSERT INTO `notice` VALUES (1,'系统上线通知','学生兴趣小组管理系统试运行，欢迎体验。','SYSTEM',NULL,1,1,1,'2026-05-16 22:17:27','2026-05-16 22:17:27',0),(2,'AI 编程社本周安排','本周六下午进行 Spring Boot 工作坊，请成员准时参加。','GROUP',1,2,0,1,'2026-05-16 22:17:27','2026-05-16 22:17:27',0),(3,'测试','123','SYSTEM',4,1,0,1,'2026-05-17 20:23:27','2026-05-17 20:23:27',0);
/*!40000 ALTER TABLE `notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(30) NOT NULL,
  `name` varchar(50) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (1,'STUDENT','学生','2026-05-16 22:17:27','2026-05-16 22:17:27',0),(2,'LEADER','小组负责人/指导老师','2026-05-16 22:17:27','2026-05-16 22:17:27',0),(3,'ADMIN','系统管理员','2026-05-16 22:17:27','2026-05-16 22:17:27',0);
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `student_no` varchar(50) DEFAULT NULL,
  `phone` varchar(30) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','{noop}123456','系统管理员',NULL,'13800000001','admin@example.com',NULL,1,'2026-05-16 22:17:27','2026-05-16 22:19:48',0),(2,'leader','{noop}123456','李老师',NULL,'13800000002','leader@example.com',NULL,1,'2026-05-16 22:17:27','2026-05-16 22:19:48',0),(3,'student','{noop}123456','张同学','20260001','13800000003','student@example.com',NULL,1,'2026-05-16 22:17:27','2026-05-16 22:19:48',0),(5,'user','{bcrypt}$2a$10$bmhqAvSaxLwDTkb9RNrpL..IuA8qhC/37KHGlu5r7LH4MIWPQlZv6','1','1','1','1',NULL,1,'2026-05-16 22:24:42','2026-05-16 22:24:42',0),(6,'user2','{bcrypt}$2a$10$cgF8SvVNqYSD/uW5XV.Hb.Zk1RUqoyx/3QHoiin1d/AV8akCRWobq','user2','user2','user2','user2',NULL,1,'2026-05-17 19:48:05','2026-05-17 19:48:05',0),(7,'user5','{bcrypt}$2a$10$jOIZTBKRxmtAZq.EQcAi4u3/FUfJ6gVif/fqLCCKJFqr8W5EAVIWi','user5','user5','user5','user5',NULL,1,'2026-05-17 22:31:15','2026-05-17 22:31:15',0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,1,3),(6,2,1),(2,2,2),(3,3,1),(4,4,1),(5,5,1),(7,5,2),(8,6,1),(9,7,1);
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-21 22:43:26
