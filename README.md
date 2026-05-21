# 学生兴趣小组管理系统

基于 Java Spring Boot + MyBatis-Plus + MySQL + Vue 3 + Element Plus 的前后端分离 Web 管理系统。系统包含学生友好前台和负责人/管理员后台。

## 技术栈

- 后端：Java 17、Spring Boot 3.3、MyBatis-Plus、MySQL、JWT、Maven
- 前端：Vue 3、Vite、Element Plus、Axios、Vue Router
- 数据库脚本：`sql/schema.sql`、`sql/data.sql`

## 目录结构

```text
backend/   Spring Boot 后端
frontend/  Vue 3 + Element Plus 前端
sql/       建表和测试数据
```

后端主包为 `com.example.club`，包含 `controller`、`service`、`service.impl`、`mapper`、`entity`、`dto`、`vo`、`config`、`common`、`exception`、`utils`。

## 核心功能

- 用户注册、登录、JWT 鉴权、修改密码、用户启用禁用
- 兴趣小组新增、修改、分页查询、审核、成员管理
- 学生提交创建兴趣小组申请，管理员审核通过后自动升级为负责人
- 小组封面支持本地图片上传，上传文件保存在 `backend/uploads`
- 学生申请加入小组，负责人或管理员审核申请
- 审核通过后自动写入小组成员表
- 活动发布、修改、分页查询、学生报名
- 活动报名采用审核流程，学生提交申请后由负责人或管理员审核，通过后才计入报名人数
- 登录学生/负责人首页只展示自己已加入小组的近期活动，未登录首页展示公开近期活动
- 防重复申请、防重复报名、小组满员控制、活动满员控制、活动开始后禁止报名
- 系统公告和小组公告
- 站内消息通知：审核结果、活动报名、公告发布后自动通知相关用户
- 活动签到：负责人/管理员可为已通过报名的学生签到，学生可查看自己的签到状态
- 操作日志：记录后台关键操作，管理员可查询审计记录
- 运营统计图表：展示待处理事项、未读消息、签到数量和近 7 天运营趋势
- 分类管理、用户管理、基础数据统计
- 前台页面：小组浏览、搜索、申请加入、活动报名
- 后台页面：统计、小组、申请、活动、签到、公告、分类、用户、操作日志管理

## 数据库初始化

1. 创建 MySQL 数据库并执行：

```sql
source sql/schema.sql;
source sql/data.sql;
```

2. 修改 `backend/src/main/resources/application.yml` 中的数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/student_club?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
    username: root
    password: root
```

也可以不改文件，直接用环境变量覆盖：

```bash
DB_URL=jdbc:mysql://127.0.0.1:3306/student_club?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
DB_USERNAME=root
DB_PASSWORD=你的MySQL密码
```

测试账号默认密码均为 `123456`：

- 管理员：`admin`
- 负责人示例：`leader`，同时拥有学生和负责人角色
- 学生：`student`

## 启动方式

后端：

```bash
cd backend
mvn spring-boot:run
```

前端：

```bash
cd frontend
npm.cmd install
npm.cmd run dev
```

访问 Vite 输出的本地地址，默认接口代理到 `http://localhost:8080`。

## 核心接口

认证：

- `POST /api/auth/register` 注册学生账号
- `POST /api/auth/login` 登录并返回 JWT
- `GET /api/auth/me` 当前用户
- `POST /api/auth/password` 修改密码

兴趣小组：

- `GET /api/groups/public/page` 前台分页查询已审核小组
- `GET /api/groups/page` 后台分页查询
- `POST /api/groups` 创建小组
- 学生调用 `POST /api/groups` 会提交待审核建组申请；管理员创建的小组默认通过
- `PUT /api/groups` 修改小组
- `POST /api/groups/{id}/review?auditStatus=1` 审核小组
- 审核通过建组申请时，系统会自动为申请人分配 `LEADER` 角色并写入小组负责人成员记录
- `POST /api/upload/image` 上传图片，返回可访问的封面 URL
- `GET /api/groups/{id}/members` 小组成员
- `POST /api/groups/{id}/quit` 学生退出小组
- `DELETE /api/groups/{groupId}/members/{userId}` 移除成员

申请：

- `POST /api/applies` 提交加入申请
- `GET /api/applies/mine` 我的申请
- `GET /api/applies/group/{groupId}` 小组申请列表
- `POST /api/applies/{id}/review` 审核申请

活动：

- `GET /api/activities/public/page` 前台活动列表
- `GET /api/activities/visible` 当前登录用户已加入小组的近期活动
- `GET /api/activities/page` 后台活动列表
- `POST /api/activities` 发布活动
- `PUT /api/activities` 修改活动
- `POST /api/activities/{id}/signup` 学生报名
- `GET /api/activities/signups/mine` 我的活动报名申请
- `GET /api/activities/{id}/signups` 活动报名列表
- `POST /api/activities/signups/{signupId}/review` 审核活动报名申请
- `GET /api/activities/{id}/checkins` 活动签到列表
- `POST /api/activities/{id}/checkins/{userId}` 单个学生签到
- `POST /api/activities/{id}/checkins/batch` 批量签到
- `GET /api/activities/checkins/mine` 我的签到状态

公告、分类、统计：

- `GET /api/notices/public/system` 系统公告
- `GET /api/notices/visible` 当前用户可见公告
- `GET /api/notices/managed` 后台公告列表，负责人仅返回自己负责小组的 GROUP 公告
- `POST /api/notices` 发布公告
- `GET /api/categories` 分类列表
- `POST /api/categories` 新增分类
- `GET /api/stats/overview` 管理员统计概览
- `GET /api/stats/trends` 近 7 天运营趋势
- `GET /api/roles` 角色列表
- `POST /api/roles/assign?userId=1` 分配用户角色

消息与日志：

- `GET /api/messages/mine` 我的站内消息
- `POST /api/messages/{id}/read` 标记单条消息已读
- `POST /api/messages/read-all` 标记全部消息已读
- `GET /api/operation-logs/page` 操作日志分页查询，仅管理员可访问

## 升级功能规划

一期运营增强 MVP 已覆盖：

- 站内消息：加入小组审核、建组审核、活动报名审核、公告发布会生成系统内消息。
- 活动签到：签到基于已通过的活动报名记录，负责人只能管理自己小组的活动，管理员可管理全部活动。
- 操作日志：记录小组、活动、公告、分类、用户状态、审核、签到等关键后台动作。
- 数据图表：后台统计页展示核心指标和近 7 天小组、活动、申请、签到趋势。

二期可继续扩展：

- Spring Security 与菜单级权限，替换当前轻量 JWT 拦截器角色判断。
- 更完整的审批流配置，例如多级审核、撤回、转交、超时提醒。
- 消息中心增强为实时推送，支持 WebSocket、邮件或短信通知。
- 签到方式扩展为二维码、定位、签到码和补签审批。
- 数据可视化升级为 ECharts 仪表盘，增加分类分布、活跃排行和导出报表。

## 说明与扩展建议

- 当前权限采用 JWT + 拦截器 + 角色判断，适合课程设计和中小型系统演示。
- 后续可扩展为 Spring Security、菜单级权限、文件上传增强、站内实时消息、审批流、二维码签到、数据可视化报表。
- 前端后台已按 Element Plus 管理系统形态组织，前台使用更友好的门户布局。
