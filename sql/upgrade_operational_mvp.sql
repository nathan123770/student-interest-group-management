USE student_club;

CREATE TABLE IF NOT EXISTS activity_checkin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1已签到',
  checkin_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  checkin_method VARCHAR(30) NOT NULL DEFAULT 'MANUAL',
  operator_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_checkin_activity_user (activity_id, user_id),
  KEY idx_checkin_activity (activity_id),
  KEY idx_checkin_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  receiver_id BIGINT NOT NULL,
  title VARCHAR(120) NOT NULL,
  content TEXT,
  business_type VARCHAR(40),
  business_id BIGINT,
  read_status TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_message_receiver (receiver_id, read_status),
  KEY idx_message_business (business_type, business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id BIGINT,
  operator_roles VARCHAR(120),
  module VARCHAR(40) NOT NULL,
  action VARCHAR(40) NOT NULL,
  business_id BIGINT,
  result VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
  ip VARCHAR(64),
  detail VARCHAR(500),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_operation_module (module, action),
  KEY idx_operation_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
