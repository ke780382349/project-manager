# Project Manager

项目管理系统，当前先实现用户模块和统一登录。

## 工程结构

```text
services/  Spring Boot + Java + MySQL 后端
web/       React + Vite 前端
```

后端使用 JWT 认证，令牌默认有效期为 30 天。后续的项目、需求、Bug 和任务模块都复用 `/api/auth/me` 识别当前用户。退出登录时会递增用户的令牌版本，使已有令牌失效。

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

## 用户与角色

系统不开放公开注册。管理员通过用户管理接口开通账号，并为账号分配角色：

- `ADMIN`：可管理用户，并拥有当前已定义的全部业务权限。
- `USER`：可访问普通工作区，不能访问用户管理接口。

角色保存在 `roles` 表，权限定义保存在 `permissions` 表，角色和权限通过 `role_permissions` 表的 `role_id`、`permission_id` 关联。权限只使用数据库 ID 作为唯一标识，没有单独的编码字段；新增权限只需填写名称和描述。

管理员可以使用以下接口开通和管理用户：

```text
POST /api/auth/login
GET  /api/auth/me       Authorization: Bearer <token>
POST /api/auth/logout
GET  /api/dashboard     Authorization: Bearer <token>
GET  /api/users         Authorization: Bearer <token>
POST /api/users         Authorization: Bearer <token>
PATCH /api/users/{id}/status  Authorization: Bearer <token>
PATCH /api/users/{id}/role    Authorization: Bearer <token>
GET  /api/roles        Authorization: Bearer <token>
GET  /api/roles/permissions  Authorization: Bearer <token>
POST /api/roles        Authorization: Bearer <token>
PATCH /api/roles/{id}    Authorization: Bearer <token>
DELETE /api/roles/{id}   Authorization: Bearer <token>
GET  /api/permissions  Authorization: Bearer <token>
POST /api/permissions  Authorization: Bearer <token>
PATCH /api/permissions/{id} Authorization: Bearer <token>
DELETE /api/permissions/{id} Authorization: Bearer <token>
```

`POST /api/users` 请求示例：

```json
{
  "username": "zhangsan",
  "email": "zhangsan@example.com",
  "displayName": "张三",
  "password": "至少 8 位的初始密码",
  "roleId": "角色 ID"
}
```

`POST /api/roles` 请求示例：

```json
{
  "name": "测试人员",
  "description": "负责测试和缺陷跟进",
  "permissionIds": ["权限 UUID"]
}
```

角色 ID 由系统自动生成，使用去掉横杠的 32 位 UUID。`ADMIN` 和 `USER` 是内置角色，不能删除或修改；自定义角色在没有分配给用户时才可以删除。

`POST /api/permissions` 请求示例：

```json
{
  "name": "导出项目",
  "description": "允许导出项目数据"
}
```

权限 ID 使用去掉横杠的 32 位 UUID。自定义权限的 ID 由系统自动生成；内置权限使用 `PermissionIds.java` 中的固定 UUID，保证新建数据库和重启后的鉴权规则一致。登录和 `/api/auth/me` 响应中的 `permissions` 是权限 ID 列表，前端菜单和后端接口都按这些 ID 判断权限。

管理员可以新增自定义权限，并通过 `permissionIds` 将它们分配给角色。后续业务接口需要校验对应权限 ID 才会生效。修改权限名称和描述不会改变 ID 或已有角色关联。

后端首次启动时会自动创建管理员测试账号：

```text
账号：admin
密码：admin
```

登录接口同时接受账号或邮箱，例如管理员可以提交 `{"account":"admin","password":"admin"}`。

开发阶段后端使用 `spring.jpa.hibernate.ddl-auto=update` 自动创建和更新 MySQL 表结构，暂时不引入 Redis 或 Flyway。

## 开发配置

当前开发阶段的数据库和服务配置直接写在 `services/src/main/resources/application.yml`：

```text
数据库：localhost:3306/project_manager
账号：root
密码：root
JWT 有效期：30 天
```
