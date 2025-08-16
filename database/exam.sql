-- 创建考试座位管理系统数据库
-- 数据库：exam
-- 创建时间：2024年
-- 版本：MySQL 8.0+

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS exam
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE exam;

-- 2. 创建用户表（优化版）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名，唯一',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    email VARCHAR(100) UNIQUE COMMENT '邮箱地址',
    full_name VARCHAR(100) COMMENT '真实姓名',
    role VARCHAR(20) DEFAULT 'ROLE_USER' COMMENT '用户角色',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB COMMENT='用户表';

-- 3. 创建考试表（优化版）
CREATE TABLE IF NOT EXISTS exam (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject VARCHAR(100) NOT NULL COMMENT '考试科目',
    exam_type VARCHAR(50) NOT NULL COMMENT '考试类型（期中、期末等）',
    room VARCHAR(50) NOT NULL COMMENT '考试教室',
    exam_date DATE NOT NULL COMMENT '考试日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    start_end_time VARCHAR(50) NOT NULL COMMENT '时间段描述',
    notes TEXT COMMENT '备注信息',
    total_seats INT DEFAULT 0 COMMENT '总座位数',
    occupied_seats INT DEFAULT 0 COMMENT '已占座位数',
    status ENUM('SCHEDULED', 'ONGOING', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED' COMMENT '考试状态',
    created_by BIGINT COMMENT '创建人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_subject (subject),
    INDEX idx_exam_date (exam_date),
    INDEX idx_room_date (room, exam_date),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by),
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='考试信息表';

-- 4. 创建座位表（优化版）
CREATE TABLE IF NOT EXISTS seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL COMMENT '关联的考试ID',
    seat_number INT NOT NULL COMMENT '座位号',
    student_name VARCHAR(100) COMMENT '学生姓名',
    student_id VARCHAR(50) COMMENT '学号',
    available BOOLEAN DEFAULT TRUE COMMENT '座位是否可用',
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED', 'DISABLED') DEFAULT 'AVAILABLE' COMMENT '座位状态',
    notes VARCHAR(255) COMMENT '座位备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_exam_seat (exam_id, seat_number),
    INDEX idx_exam_id (exam_id),
    INDEX idx_student_name (student_name),
    INDEX idx_student_id (student_id),
    INDEX idx_available (available),
    INDEX idx_status (status),
    
    FOREIGN KEY (exam_id) REFERENCES exam(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='考试座位表';

-- 5. 创建审计日志表（新增）
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL COMMENT '操作的表名',
    record_id BIGINT NOT NULL COMMENT '记录ID',
    action ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL COMMENT '操作类型',
    old_values JSON COMMENT '旧值',
    new_values JSON COMMENT '新值',
    changed_by BIGINT COMMENT '操作人ID',
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_table_record (table_name, record_id),
    INDEX idx_changed_by (changed_by),
    INDEX idx_changed_at (changed_at),
    
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='审计日志表';

-- 6. 插入初始管理员用户（密码：admin123，已加密）
INSERT IGNORE INTO users (username, password, email, full_name, role, is_active) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaUKk7h.T0mJ2', 'admin@exam.com', '系统管理员', 'ROLE_ADMIN', TRUE);

-- 7. 插入示例考试数据
INSERT IGNORE INTO exam (subject, exam_type, room, exam_date, start_time, end_time, start_end_time, notes, total_seats, created_by) VALUES
('高等数学', '期末考试', 'A101', '2024-01-15', '09:00:00', '11:00:00', '09:00-11:00', '高等数学期末考试', 50, 1),
('大学英语', '期中考试', 'B205', '2024-01-16', '14:00:00', '16:00:00', '14:00-16:00', '英语期中考试', 40, 1),
('数据结构', '期末考试', 'C301', '2024-01-17', '08:30:00', '10:30:00', '08:30-10:30', '数据结构期末考试', 35, 1);

-- 8. 插入示例座位数据
-- 为高等数学考试创建座位
INSERT IGNORE INTO seat (exam_id, seat_number, available, status) 
SELECT 1, seat_num, TRUE, 'AVAILABLE' 
FROM (SELECT @row := @row + 1 AS seat_num FROM 
    (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a,
    (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5) b
    CROSS JOIN (SELECT @row := 0) r
) AS numbers
WHERE seat_num <= 50;

-- 为大学英语考试创建座位
INSERT IGNORE INTO seat (exam_id, seat_number, available, status) 
SELECT 2, seat_num, TRUE, 'AVAILABLE' 
FROM (SELECT @row := @row + 1 AS seat_num FROM 
    (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a,
    (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) b
    CROSS JOIN (SELECT @row := 0) r
) AS numbers
WHERE seat_num <= 40;

-- 为数据结构考试创建座位
INSERT IGNORE INTO seat (exam_id, seat_number, available, status) 
SELECT 3, seat_num, TRUE, 'AVAILABLE' 
FROM (SELECT @row := @row + 1 AS seat_num FROM 
    (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7) b
    CROSS JOIN (SELECT @row := 0) r
) AS numbers
WHERE seat_num <= 35;

-- 9. 创建视图（方便查询）
CREATE OR REPLACE VIEW exam_seat_summary AS
SELECT 
    e.id as exam_id,
    e.subject,
    e.exam_date,
    e.room,
    e.total_seats,
    COUNT(s.id) as actual_seats,
    SUM(CASE WHEN s.status = 'OCCUPIED' THEN 1 ELSE 0 END) as occupied_seats,
    SUM(CASE WHEN s.status = 'AVAILABLE' THEN 1 ELSE 0 END) as available_seats,
    CONCAT(e.start_time, '-', e.end_time) as exam_time
FROM exam e
LEFT JOIN seat s ON e.id = s.exam_id
GROUP BY e.id, e.subject, e.exam_date, e.room, e.total_seats, e.start_time, e.end_time;

-- 10. 创建存储过程（批量更新座位状态）
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS update_seat_status(
    IN p_exam_id BIGINT,
    IN p_seat_numbers TEXT,
    IN p_new_status VARCHAR(20),
    IN p_student_name VARCHAR(100),
    IN p_student_id VARCHAR(50)
)
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE seat_num INT;
    DECLARE seat_cursor CURSOR FOR 
        SELECT CAST(TRIM(seat_num) AS UNSIGNED) 
        FROM JSON_TABLE(CONCAT('[', p_seat_numbers, ']'), '$[*]' COLUMNS(seat_num INT PATH '$')) AS jt;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN seat_cursor;
    
    read_loop: LOOP
        FETCH seat_cursor INTO seat_num;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        UPDATE seat 
        SET 
            status = p_new_status,
            student_name = p_student_name,
            student_id = p_student_id,
            available = CASE WHEN p_new_status = 'AVAILABLE' THEN TRUE ELSE FALSE END,
            updated_at = NOW()
        WHERE exam_id = p_exam_id AND seat_number = seat_num;
    END LOOP;
    
    CLOSE seat_cursor;
END//
DELIMITER ;

-- 11. 设置外键检查（确保数据完整性）
SET FOREIGN_KEY_CHECKS = 1;

-- 12. 查看创建结果
SELECT '数据库创建完成！' AS message;
SELECT '表结构：' AS info;
SHOW TABLES;

SELECT '考试统计：' AS info;
SELECT * FROM exam_seat_summary;