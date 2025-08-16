# 考试座位管理系统数据库重建指南

## 数据库重建步骤

### 1. 创建数据库
打开MySQL命令行或MySQL Workbench，执行以下命令：

```sql
-- 登录MySQL
mysql -u root -p

-- 执行创建脚本
source d:/IdeaProjects/Exam/database/create_database.sql
```

### 2. 验证数据库创建
执行以下查询验证：
```sql
USE exam;
SHOW TABLES;
SELECT * FROM exam_seat_summary;
```

### 3. 测试连接
项目启动后，访问：http://localhost:8080

### 4. 管理员登录
- 用户名：admin
- 密码：admin123

## 数据库优化特性

### 1. 性能优化
- **连接池优化**：使用HikariCP连接池，最大连接数20
- **批处理优化**：支持批量插入/更新操作
- **索引优化**：为常用查询字段添加索引
- **字符集优化**：使用utf8mb4支持完整Unicode字符

### 2. 安全性增强
- **密码加密**：使用BCrypt加密存储用户密码
- **外键约束**：确保数据完整性
- **审计日志**：记录重要操作变更
- **SQL注入防护**：使用预编译语句

### 3. 数据完整性
- **唯一约束**：防止重复用户名和邮箱
- **非空约束**：确保关键字段不为空
- **外键约束**：维护表间关系完整性
- **状态管理**：使用枚举类型管理状态

### 4. 新增功能
- **审计日志**：记录所有数据变更
- **批量操作**：支持批量座位分配
- **统计视图**：提供实时考试统计
- **存储过程**：优化复杂业务逻辑

## 数据库结构

### 核心表
- **users**：用户表（管理员、教师、学生）
- **exam**：考试信息表
- **seat**：座位分配表
- **audit_log**：审计日志表

### 视图
- **exam_seat_summary**：考试座位统计视图

### 存储过程
- **update_seat_status**：批量更新座位状态

## 维护建议

### 1. 定期维护
```sql
-- 优化表
OPTIMIZE TABLE users, exam, seat, audit_log;

-- 更新统计信息
ANALYZE TABLE users, exam, seat, audit_log;
```

### 2. 备份策略
```bash
# 定期备份命令
mysqldump -u root -p exam > exam_backup_$(date +%Y%m%d).sql
```

### 3. 监控查询
```sql
-- 查看慢查询
SELECT * FROM mysql.slow_log;

-- 查看连接状态
SHOW PROCESSLIST;
```

## 故障排除

### 1. 连接问题
- 检查MySQL服务是否运行
- 验证用户名密码
- 检查防火墙设置

### 2. 字符集问题
- 确保MySQL配置文件中设置utf8mb4
- 检查客户端连接字符集

### 3. 性能问题
- 使用EXPLAIN分析慢查询
- 检查索引使用情况
- 监控连接池状态

## 扩展功能

### 1. 分库分表
当数据量增大时，可以考虑：
- 按时间分表存储考试数据
- 使用分区表优化查询性能

### 2. 读写分离
配置主从复制：
- 主库：处理写操作
- 从库：处理查询操作

### 3. 缓存集成
- 集成Redis缓存热点数据
- 使用Spring Cache优化查询性能