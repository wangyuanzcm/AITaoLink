# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AITaoLink is a full-stack enterprise low-code platform — a customized fork of JeecgBoot 3.9.1 (released 2026-01-28) with a custom e-commerce module called **Taolink** for TaoBao/1688 supply chain management. Also includes an AIGC platform (AI workflow orchestration, knowledge base/RAG, chat assistants, model management) as a built-in `airag` module.

Two tightly coupled applications:
- **Backend** (`jeecg-boot/`): Java Spring Boot 3.5.5 monolith (switchable to Spring Cloud Alibaba microservices)
- **Frontend** (`jeecgboot-vue3/`): Vue 3.5 + Vite 6 + TypeScript SPA

## Common Commands

### Backend (Java — `jeecg-boot/`)
```bash
cd jeecg-boot
mvn clean install                    # Build all modules
mvn spring-boot:run                  # Run monolith from jeecg-system-start module (port 8080)
mvn test                             # Run tests (disabled by default via skipTests=true)
```
Recommended: run `org.jeecg.JeecgSystemApplication` from `jeecg-module-system/jeecg-system-start/` directly in your IDE.

### Frontend (Vue 3 — `jeecgboot-vue3/`)
See `jeecgboot-vue3/CLAUDE.md` for full frontend guidance. Quick reference:
```bash
cd jeecgboot-vue3
pnpm install                         # Install dependencies
pnpm dev                             # Dev server on port 3100, proxied to localhost:8080
pnpm build                           # Production build (output: dist/)
```

### Docker (Full Stack)
```bash
# Single-stack mode
docker-compose up -d

# Microservice mode
docker-compose -f docker-compose-cloud.yml up -d
```

### Default Credentials
Username: `admin` | Password: `123456`

## Architecture

### Backend (jeecg-boot/)

Spring Boot 3.5.5, Java 17, Maven multi-module. Tech stack: MyBatis-Plus 3.5.12 (ORM), Shiro + JWT (security), Druid (connection pool), Redis (cache), Knife4j (API docs).

Key modules:
| Module | Purpose |
|--------|---------|
| `jeecg-boot-base-core/` | Shared core — utils, config, base controllers, security, Redis, file storage |
| `jeecg-module-system/jeecg-system-start/` | **Entry point** (port 8080) — user, role, menu, dict, tenant, log, tasks |
| `jeecg-boot-module/jeecg-boot-module-airag/` | AIGC platform — AI workflows, knowledge base, RAG, models, MCP, chat |
| `jeecg-boot-module/jeecg-module-taolink/` | **Custom** — Taolink e-commerce domain model |
| `jeecg-server-cloud/` | Microservice infrastructure — Gateway (9999), Nacos (8848), Sentinel (9000) |

### Taolink Custom Domain Model

Located in `jeecg-boot/jeecg-boot-module/jeecg-module-taolink/`. Standard layered: `entity/` → `mapper/` → `service/` → `controller/`.

Key entities:
| Entity | Description |
|--------|-------------|
| `TaolinkProduct` / `TaolinkProductSku` | Product catalog with SKU variants |
| `TaolinkOrder` / `TaolinkOrderLine` | Customer orders |
| `TaolinkPurchase` / `TaolinkPurchaseLine` | Supplier purchase orders |
| `TaolinkInventory` / `TaolinkInventoryMovement` | Stock tracking and movement logs |
| `TaolinkWarehouse` | Warehouse locations |
| `TaolinkSourceOffer` | Source offers from TaoBao/1688 |
| `TaolinkSkuBinding` | SKU mapping/binding between platforms |
| `TaolinkTicket` | Customer service tickets |
| OneboundClient | External API client for TaoBao/1688 data |

### Frontend (jeecgboot-vue3/)

Vue 3 SPA with Ant Design Vue 4, Pinia, TypeScript, Vite 6. See `jeecgboot-vue3/CLAUDE.md` for comprehensive frontend guidance. Key highlights:

- **Permission mode**: BACK — routes and menus fetched from backend API
- **Dynamic modules**: `src/views/super/` discovered via `import.meta.glob('./**/register.ts')`
- **API layer**: Custom axios wrapper (`defHttp`) with MD5 signing, tenant ID injection
- **Auto-import**: Ant Design Vue components auto-imported via `unplugin-vue-components`
- **Micro-frontend**: Qiankun support; can run as host or child
- **Electron**: Optional desktop packaging with hash router mode

## Important Notes

- This repo is a fork of JeecgBoot — upstream changes/fixes should be mindful of the custom Taolink module
- The `jeecgboot-vue3/CLAUDE.md` contains detailed frontend architecture (bootstrap sequence, routing, state management, icon system, theme system, build pipeline, code style)
- Database: MySQL 5.7+ with SQL migration scripts in `jeecg-boot/db/`
- Frontend proxy in dev mode: `localhost:3100` → `localhost:8080/jeecg-boot`
- Backend API docs available at `http://localhost:8080/jeecg-boot/doc.html` (Knife4j)
