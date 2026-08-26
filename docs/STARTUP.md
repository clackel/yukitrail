# YukiTrail 完整启动流程

本文档适用于 Windows PowerShell。项目由 Vue Web、Spring Boot API 和 MySQL 三部分组成：

```text
浏览器 -> http://localhost:5173 -> /api/v1 -> http://localhost:8080 -> MySQL
```

## 1. 环境准备

请先确认以下命令可用：

```powershell
node --version
npm --version
java --version
mvn --version
```

项目要求 Node.js 22.12+、npm 10+、Java 21 和 Maven 3.6+。数据库推荐使用项目计划指定的 MySQL 8.4。

安装前端依赖：

```powershell
Set-Location C:\Workspaces\yukitrail
npm install
```

高德地图密钥不是当前工程骨架启动的必要条件。如果需要配置地图，可创建本地文件后填写浏览器端密钥：

```powershell
Copy-Item apps/web/.env.example apps/web/.env.local
```

不要把真实数据库密码或高德地图密钥提交到 Git。

## 2. 启动 MySQL

两种方式只选择一种。推荐方式 A，它能保证 MySQL 版本与项目计划一致。

### 方式 A：Docker MySQL 8.4（推荐）

```powershell
Copy-Item infra/.env.example infra/.env
docker compose --env-file infra/.env -f infra/compose.yml up -d
docker compose --env-file infra/.env -f infra/compose.yml ps
```

等待 `mysql` 服务变为 `healthy`。`infra/.env` 仅供本机使用，已经被 Git 忽略；如需修改其中的本地密码，请在第一次创建数据卷前完成。

### 方式 B：使用本机已有 MySQL

本项目当前本机开发库已初始化为：

| 配置 | 本地值 |
| --- | --- |
| 数据库 | `yukitrail` |
| 应用账号 | `yukitrail` |
| 应用密码 | `yukitrail-local` |
| 地址 | `localhost:3306` |
| 字符集 / 排序规则 | `utf8mb4` / `utf8mb4_0900_ai_ci` |

应用账号只拥有 `yukitrail` 数据库的权限。日常启动 API 不需要使用 MySQL root 账号。

如果要在另一台机器上重新初始化，请先用管理员账号进入 MySQL；使用 `-p` 让客户端交互式询问密码，不要把管理员密码写入命令、脚本或仓库：

```powershell
mysql -uroot -p
```

然后执行：

```sql
CREATE DATABASE IF NOT EXISTS yukitrail
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'yukitrail'@'localhost'
  IDENTIFIED BY 'yukitrail-local';
CREATE USER IF NOT EXISTS 'yukitrail'@'127.0.0.1'
  IDENTIFIED BY 'yukitrail-local';

GRANT ALL PRIVILEGES ON yukitrail.* TO 'yukitrail'@'localhost';
GRANT ALL PRIVILEGES ON yukitrail.* TO 'yukitrail'@'127.0.0.1';
FLUSH PRIVILEGES;
```

上面的密码仅是仓库已有的本地开发默认值，部署环境必须替换。

## 3. 启动 API

打开第一个 PowerShell 终端：

```powershell
Set-Location C:\Workspaces\yukitrail
npm run api:dev
```

API 会读取以下默认连接配置：

```text
DB_HOST=localhost
DB_PORT=3306
DB_NAME=yukitrail
DB_USERNAME=yukitrail
DB_PASSWORD=yukitrail-local
```

需要覆盖时，可以只在当前 PowerShell 会话中设置环境变量，再启动 API：

```powershell
$env:DB_HOST = 'localhost'
$env:DB_PORT = '3306'
$env:DB_NAME = 'yukitrail'
$env:DB_USERNAME = 'yukitrail'
$env:DB_PASSWORD = '替换为你的本地应用密码'
npm run api:dev
```

首次成功连接数据库时，Flyway 会自动执行 `apps/api/src/main/resources/db/migration` 下的迁移。目前会创建五张业务表：

- `users`
- `auth_sessions`
- `trips`
- `trip_days`
- `itinerary_items`

此外还会创建 `flyway_schema_history`，用于记录数据库版本。不要手工改动已经执行过的迁移文件；后续结构变更应新增下一版本迁移。

验证 API：

```powershell
Invoke-RestMethod http://localhost:8080/api/v1/health
```

返回内容中的 `code` 应为 `OK`，`data.status` 应为 `UP`。

## 4. 启动 Web

保持 API 运行，打开第二个 PowerShell 终端：

```powershell
Set-Location C:\Workspaces\yukitrail
npm run web:dev
```

打开：

- Web：`http://localhost:5173`
- API：`http://localhost:8080/api/v1/health`

开发环境中的 `/api/v1` 请求由 Vite 代理到 API，因此也可以验证完整代理链路：

```powershell
Invoke-RestMethod http://localhost:5173/api/v1/health
```

## 5. 验证数据库

用应用账号登录，客户端会交互式询问密码：

```powershell
mysql -uyukitrail -p -D yukitrail
```

进入 MySQL 后执行：

```sql
SHOW TABLES;
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

应看到五张业务表、`flyway_schema_history`，以及成功的 `V1` 迁移记录。

## 6. 运行项目检查

```powershell
npm run web:check
npm run api:test
```

`web:check` 会执行前端单元测试、类型检查和生产构建。`api:test` 会执行后端测试；其中真实 MySQL 集成测试依赖可用的 Docker 环境。

## 7. 停止项目

在 Web 和 API 各自的终端按 `Ctrl+C`。

如果使用 Docker MySQL，再执行：

```powershell
docker compose --env-file infra/.env -f infra/compose.yml down
```

这会停止容器但保留数据库卷。只有明确要删除所有本地数据库数据时才使用 `down -v`。

## 8. 常见问题

- `3306` 被占用：本机 MySQL 与 Docker MySQL 不能同时监听同一端口；停止其中一个，或修改 `infra/.env` 中的 `DB_PORT` 并同步设置 API 的 `DB_PORT`。
- `8080` 被占用：停止旧 API 进程，或在启动前设置 `$env:SERVER_PORT`。
- `5173` 被占用：Vite 会提示替代端口，但 API 的 CORS 默认只允许 `5173`；建议先停止占用进程。
- API 报数据库连接失败：确认 MySQL 已启动、应用账号可登录，并检查 `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`。
- 页面能打开但健康检查失败：先直接访问 `http://localhost:8080/api/v1/health`，确认 API 正常后再检查 Vite 代理。
