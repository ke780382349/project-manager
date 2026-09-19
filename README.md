# Project Manager

项目管理系统，当前先实现用户模块和统一登录。

## 工程结构

```text
services/  Spring Boot + Java + MySQL 后端
web/       React + Vite 前端
```

后端使用 JWT 认证，令牌默认有效期为 30 天。后续的项目、需求、Bug 和任务模块都复用 `/api/auth/me` 识别当前用户。

## 启动数据库

```bash
cd services
docker compose up -d mysql
```

默认 MySQL 配置：

```text
数据库：project_manager
用户名：root
密码：root
端口：3306
```

## 启动后端

```bash
cd services
mvn spring-boot:run
```

后端地址：<http://localhost:8080>

## 启动前端

```bash
cd web
npm install
npm run dev
```

前端地址：<http://localhost:5173>

## 用户接口

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/me       Authorization: Bearer <token>
POST /api/auth/logout
GET  /api/dashboard     Authorization: Bearer <token>
```

开发阶段后端使用 `spring.jpa.hibernate.ddl-auto=update` 自动创建和更新 MySQL 表结构，暂时不引入 Redis 或 Flyway。

## 配置覆盖

后端可以通过环境变量覆盖：

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET       至少 32 个字符
JWT_EXPIRATION   默认 30d
FRONTEND_ORIGIN  默认 http://localhost:5173
```
