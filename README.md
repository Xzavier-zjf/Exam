# 考试时钟电子座位表 (Exam)

## 项目简介
考试时钟电子座位表是一款专注于考试场景的管理工具，旨在提供考试安排、座位分配和实时时钟显示的综合解决方案。通过此系统，考场管理者可以轻松管理考试资源，同时提升效率和规范性。

## 特性
- **考试管理**：支持考试的创建、编辑和删除。
- **座位分配**：提供灵活的座位分配和状态管理。
- **考试时钟**：显示实时倒计时，便于考生和监考人员掌握时间。
- **错误处理**：通过全局错误处理脚本确保系统稳定。
- **角色权限**：支持管理员和用户的角色划分。

## 技术栈
- **前端**：HTML, JavaScript, CSS
- **后端**：Java (Spring Boot)
- **数据库**：MySQL 8.0
- **依赖管理**：Maven

## 项目结构
```
Exam/
├── database/                # 数据库相关文件
│   ├── exam.sql             # 数据库初始化脚本
│   ├── README.md            # 数据库说明文档
├── src/
│   ├── main/
│   │   ├── java/com/example/exam/
│   │   │   ├── controller/  # 控制器
│   │   │   ├── model/       # 实体类
│   │   │   ├── repository/  # 数据库访问层
│   │   │   └── service/     # 业务逻辑层
│   │   ├── resources/
│   │   │   ├── templates/   # HTML模板文件
│   │   │   └── static/js/   # JavaScript文件
│   └── test/                # 单元测试
└── README.md                # 项目说明文件
```

## 安装与运行

### 1. 克隆代码
```bash
git clone https://github.com/Xzavier-zjf/Exam.git
cd Exam
```

### 2. 数据库配置
1. 确保已安装 MySQL 8.0+。
2. 使用 `database/exam.sql` 初始化数据库：
   ```sql
   -- 登录 MySQL
   mysql -u root -p
   -- 执行脚本
   source path/to/database/exam.sql;
   ```

### 3. 启动后端服务
1. 导入 Maven 项目。
2. 修改 `application.properties` 中的数据库连接：
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/exam
   spring.datasource.username=root
   spring.datasource.password=yourpassword
   ```
3. 启动服务：
   ```bash
   mvn spring-boot:run
   ```

### 4. 访问系统
在浏览器中访问 [http://localhost:8080](http://localhost:8080)。

## 系统功能
### 1. 考试管理
- 添加考试：输入考试科目、时间和地点。
- 更新考试：修改考试信息。
- 删除考试：移除不需要的考试记录。

### 2. 座位分配
- 分配座位：根据考试信息自动或手动分配座位。
- 状态管理：更新座位状态为可用、已占用等。

### 3. 实时时钟
- 倒计时显示：在考试页面实时显示剩余时间。

### 4. 错误处理
- 全局错误处理：通过 `error-handler.js` 统一处理网络、权限等异常。

## 开发指南
### 前端开发
1. 修改 HTML 模板文件：
   ```bash
   src/main/resources/templates/
   ```
2. 更新 JavaScript 文件：
   ```bash
   src/main/resources/static/js/
   ```

### 后端开发
1. 扩展控制器：
   ```bash
   src/main/java/com/example/exam/controller/
   ```
2. 添加新功能到服务层：
   ```bash
   src/main/java/com/example/exam/service/
   ```

### 数据库操作
- 定期优化表：
   ```sql
   OPTIMIZE TABLE users, exam, seat;
   ```

## 已知问题与修复
- **问题**：座位分配页面加载缓慢。
  **修复**：优化 SQL 查询，添加索引。
- **问题**：考试记录时间显示格式不统一。
  **修复**：统一使用 `LocalDateTime` 格式。

## 联系方式
如有问题，请联系项目负责人：
- GitHub: [Xzavier-zjf](https://github.com/Xzavier-zjf)
- 邮箱: 2114086570@qq.com
