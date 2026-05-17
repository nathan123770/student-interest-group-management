USE student_club;

INSERT INTO role (id, code, name) VALUES
(1, 'STUDENT', '学生'),
(2, 'LEADER', '小组负责人/指导老师'),
(3, 'ADMIN', '系统管理员');

-- 默认密码均为 123456；仅用于开发测试，{noop} 表示明文密码，新注册用户仍使用 BCrypt 加密
INSERT INTO `user` (id, username, password, real_name, student_no, phone, email, status) VALUES
(1, 'admin', '{noop}123456', '系统管理员', NULL, '13800000001', 'admin@example.com', 1),
(2, 'leader', '{noop}123456', '李老师', NULL, '13800000002', 'leader@example.com', 1),
(3, 'student', '{noop}123456', '张同学', '20260001', '13800000003', 'student@example.com', 1);

INSERT INTO user_role (user_id, role_id) VALUES (1, 3), (2, 1), (2, 2), (3, 1);

INSERT INTO group_category (id, name, description, status, sort_order) VALUES
(1, '科技创新', '编程、机器人、人工智能等科技类兴趣小组', 1, 1),
(2, '文艺体育', '音乐、舞蹈、篮球、足球等文体类兴趣小组', 1, 2),
(3, '公益实践', '志愿服务、社会实践与校园服务', 1, 3);

INSERT INTO interest_group (id, name, category_id, leader_id, cover_url, description, location, max_members, current_members, audit_status, status) VALUES
(1, 'AI 编程社', 1, 2, 'https://images.unsplash.com/photo-1515879218367-8466d910aaa4', '一起学习 Java、Web 开发和人工智能应用。', '创新楼 302', 40, 2, 1, 1),
(2, '校园篮球队', 2, 2, 'https://images.unsplash.com/photo-1546519638-68e109498ffc', '每周训练与校内联赛组织。', '体育馆', 30, 1, 1, 1),
(3, '机器人创客社', 1, 2, 'https://images.unsplash.com/photo-1518770660439-4636190af475', '围绕机器人、传感器和智能硬件开展项目实践。', '实验楼 204', 35, 1, 1, 1),
(4, '摄影与短片社', 2, 2, 'https://images.unsplash.com/photo-1452587925148-ce544e77e70d', '学习摄影构图、短片拍摄与校园影像创作。', '艺术楼 105', 30, 1, 1, 1),
(5, '英语演讲社', 2, 2, 'https://images.unsplash.com/photo-1475721027785-f74eccf877e2', '通过主题演讲和辩论训练表达能力。', '教学楼 B203', 45, 1, 1, 1),
(6, '绿色公益社', 3, 2, 'https://images.unsplash.com/photo-1466611653911-95081537e5b7', '组织环保宣传、志愿服务和校园公益行动。', '综合楼 101', 50, 1, 1, 1),
(7, '数学建模社', 1, 2, 'https://images.unsplash.com/photo-1509228468518-180dd4864904', '一起训练建模思维、算法工具和竞赛协作。', '创新楼 308', 40, 1, 1, 1);

INSERT INTO group_member (group_id, user_id, member_role, status) VALUES
(1, 2, 'LEADER', 1),
(1, 3, 'MEMBER', 1),
(2, 2, 'LEADER', 1),
(3, 2, 'LEADER', 1),
(4, 2, 'LEADER', 1),
(5, 2, 'LEADER', 1),
(6, 2, 'LEADER', 1),
(7, 2, 'LEADER', 1);

INSERT INTO join_apply (group_id, user_id, reason, status, reviewer_id, review_remark, review_time) VALUES
(1, 3, '希望学习更多 Web 开发知识。', 1, 2, '欢迎加入', NOW());

INSERT INTO activity (id, group_id, title, content, location, start_time, end_time, max_participants, current_participants, status, creator_id) VALUES
(1, 1, 'Spring Boot 入门工作坊', '从零搭建一个可运行的 Spring Boot 项目。', '创新楼 302', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 30, 1, 1, 2),
(2, 2, '周末篮球友谊赛', '轻松组队，欢迎报名。', '体育馆', DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 20, 0, 1, 2);

INSERT INTO activity_signup (activity_id, user_id, status, reviewer_id, review_remark, review_time) VALUES (1, 3, 1, 2, '报名通过', NOW());

INSERT INTO notice (title, content, notice_type, group_id, publisher_id, top_flag, status) VALUES
('系统上线通知', '学生兴趣小组管理系统试运行，欢迎体验。', 'SYSTEM', NULL, 1, 1, 1),
('AI 编程社本周安排', '本周六下午进行 Spring Boot 工作坊，请成员准时参加。', 'GROUP', 1, 2, 0, 1);
