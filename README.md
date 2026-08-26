# YukiTrail

> 规划旅程，留下足迹。

YukiTrail 是一个旅行规划与足迹分享平台。当前开发范围聚焦首版 MVP：账户、私有旅行计划、每日行程，以及高德地图辅助选点。

项目已完成工程骨架和账户认证闭环：支持注册、登录、刷新、退出、当前用户识别和前端路由保护。产品边界、接口约定和后续验收场景见 [PROJECT_PLAN.md](./PROJECT_PLAN.md)。

## 仓库结构

```text
apps/web/   Vue 3 + TypeScript 单页应用
apps/api/   Spring Boot + MyBatis REST API
docs/       开发文档
infra/      MySQL 本地基础设施
```

## 环境要求

- Node.js 22.12+ 与 npm
- Java 21 与 Maven 3.6+
- MySQL 8.4（推荐通过 Docker Desktop 与 Docker Compose 启动）

## 本地启动

1. 安装依赖：

   ```powershell
   npm install
   ```

2. 启动数据库。推荐使用与项目计划一致的 MySQL 8.4：

   ```powershell
   Copy-Item infra/.env.example infra/.env
   docker compose --env-file infra/.env -f infra/compose.yml up -d
   ```

3. 启动 API。首次启动时 Flyway 会自动创建完整的 MVP 数据表：

   ```powershell
   npm run api:dev
   ```

4. 在另一个终端启动 Web：

   ```powershell
   npm run web:dev
   ```

Web 默认地址为 `http://localhost:5173`，API 健康检查为 `http://localhost:8080/api/v1/health`。

本地启动未配置 RSA 密钥时，API 会生成只在当前进程有效的临时密钥；重启后旧访问令牌失效，但有效刷新 Cookie 仍能换取新令牌。部署配置必须提供固定密钥，详情见启动文档。

本机已有 MySQL、环境变量配置、数据库验证、停止服务和常见问题见 [完整启动流程](./docs/STARTUP.md)。

## 检查

```powershell
npm run web:check
npm run web:e2e
npm run api:test
```

真实数据库密码、JWT 密钥和高德地图密钥不得提交到仓库。
