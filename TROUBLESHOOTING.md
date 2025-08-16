# 登录问题故障排除指南

## 问题描述
登录成功后跳转到错误页面

## 已修复的问题

### 1. POM文件重复依赖
- **问题**: `spring-boot-starter-test` 依赖被重复声明
- **解决方案**: 删除了重复的依赖声明

### 2. 数据库角色字段不匹配
- **问题**: 数据库中的角色字段是 `ENUM('ADMIN', 'TEACHER', 'STUDENT')`，但代码中使用 `ROLE_ADMIN` 格式
- **解决方案**: 
  - 将数据库角色字段改为 `VARCHAR(20) DEFAULT 'ROLE_USER'`
  - 更新初始管理员用户的角色为 `ROLE_ADMIN`

### 3. Spring Security配置问题
- **问题**: 硬编码的用户配置可能与数据库用户冲突
- **解决方案**: 注释掉了 `application.properties` 中的硬编码用户配置

### 4. 异常处理器问题
- **问题**: `GlobalExceptionHandler` 可能捕获认证相关的异常
- **解决方案**: 添加了对认证异常的特殊处理，让Spring Security处理认证异常

### 5. 用户服务角色处理
- **问题**: 用户角色格式可能不正确
- **解决方案**: 在 `UserService.loadUserByUsername()` 中添加了角色格式检查和修正

## 测试步骤

1. **确保数据库运行**
   ```bash
   # 检查MySQL是否运行
   netstat -ano | findstr :3306
   ```

2. **创建数据库和表**
   ```sql
   -- 运行 database/exam.sql 脚本
   source database/exam.sql
   ```

3. **启动应用程序**
   ```bash
   mvn spring-boot:run
   ```

4. **测试登录**
   - 访问: http://localhost:8080/auth/login
   - 用户名: admin
   - 密码: admin123

5. **验证认证**
   - 访问: http://localhost:8080/test 查看认证信息

## 数据库连接信息
- Host: localhost
- Port: 3306
- Database: exam
- Username: root
- Password: 123456

## 默认用户
- 用户名: admin
- 密码: admin123
- 角色: ROLE_ADMIN

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否运行
- 验证数据库连接信息
- 确保数据库和表已创建

### 2. 用户认证失败
- 检查用户表中是否存在admin用户
- 验证密码是否正确加密
- 确认用户状态为激活状态

### 3. 权限问题
- 确保用户角色格式正确（ROLE_前缀）
- 检查Spring Security配置
- 验证角色权限映射

## 日志查看
应用程序启动时会显示数据库连接测试结果，包括：
- 数据库连接状态
- 用户表记录数
- admin用户存在状态

如果看到错误信息，请根据错误提示进行相应的修复。
