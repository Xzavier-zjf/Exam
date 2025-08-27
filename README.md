# 考试时钟电子座位表项目

## 项目简介
考试时钟电子座位表是一款专为考试场景设计的工具，旨在为考场提供数字化支持。通过此项目，考场管理者可以轻松管理考场座位安排，同时提供实时考试时钟显示，提升考试管理的效率和准确性。

## 特性
- **电子座位表管理**：支持快速生成和调整考场座位表。
- **考试时钟显示**：提供清晰的考试倒计时，确保考试时间管理规范。
- **多端支持**：支持桌面端和移动端访问。
- **可定制化**：支持按需定制考试规则和配置。

## 技术栈
该项目基于以下技术栈开发：
- **后端**：Java
- **前端**：HTML, JavaScript
- **脚本**：Batchfile

## 项目结构
```
Exam/
├── backend/       # 后端代码
├── frontend/      # 前端代码
├── scripts/       # 自动化脚本
└── README.md      # 项目说明文件
```

## 安装指南
以下是安装和运行项目的步骤：

### 1. 克隆项目代码
```bash
git clone https://github.com/Xzavier-zjf/Exam.git
cd Exam
```

### 2. 安装依赖
```bash
# 安装后端依赖
cd backend
mvn install

# 安装前端依赖
cd ../frontend
npm install
```

### 3. 运行项目
```bash
# 启动后端服务
cd backend
java -jar target/backend.jar

# 启动前端服务
cd ../frontend
npm run serve
```

### 4. 访问项目
在浏览器中访问 `http://localhost:8080`。

## 使用说明
### 1. 配置考场座位表
- 登录系统后，选择“座位表管理”模块。
- 上传座位表文件或手动编辑座位表。

### 2. 启动考试倒计时
- 在“考试设置”模块中配置考试时间。
- 启动倒计时，系统会自动显示剩余考试时间。

### 3. 导出数据
- 可导出考场数据，包括座位安排和考试时间记录。

## 贡献指南
欢迎社区开发者参与到考试时钟电子座位表项目的建设中：
1. 提交问题（Issues）或功能建议。
2. 创建分支开发新功能。
3. 提交合并请求（Pull Requests）。

贡献步骤：
```bash
# 创建新分支
git checkout -b feature-branch

# 提交修改
git commit -m "描述修改内容"

# 推送到远程仓库
git push origin feature-branch

# 创建 Pull Request
```

## 许可证
该项目采用 MIT 许可证。详情请参考 [LICENSE](./LICENSE) 文件。

## 联系方式
如有任何问题或建议，请联系项目维护者：
- GitHub: [Xzavier-zjf](https://github.com/Xzavier-zjf)
- 邮箱: 2114086570@qq.com
