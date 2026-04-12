# TaoLink SaaS 开发跟踪文档

> 本文档用于持续跟踪开发进度，方便基于 AI 工具进行迭代。
> 每次开发新内容后更新本文件的 "开发进度" 部分。

- 文档版本：2.0
- 创建日期：2026-04-12
- 更新日期：2026-04-12（v2.0：共享库存 + 库存分析 + 代发结算 + 全局看板 + AI选品规划）
- 最近更新：2026-04-12（完成第1-6轮任务：SaaS 基础 + 商品管理 + 订单管理 + 库存分析 + 代发采购与结算 + 搜索中心 + 店铺监控）
- 上游 PRD：[PRD_TaoLink_SaaS_v0.3.0.md](./PRD_TaoLink_SaaS_v0.2.0.md)

---

## 技术栈速查

| 层 | 技术 |
|---|---|
| 后端 | Java 17 + Spring Boot 3.5.5 + MyBatis-Plus 3.5.12 + Shiro + Redis |
| 前端 | Vue 3.5 + Vite 6 + TypeScript + Ant Design Vue 4 + Pinia |
| 数据库 | MySQL 5.7+ |
| 外部API | Onebound（1688.item_get / taobao.item_get） |
| 调度 | Quartz / XXL-Job |
| 后端工作目录 | `jeecg-boot/jeecg-boot-module/jeecg-module-taolink/` |
| 前端工作目录 | `jeecgboot-vue3/` |

> **重要架构变更（v0.3.0）**：
> - 库存改为**全局共享（跨店铺共用）**，库存表不新增 `shop_id`
> - 仓库表 `taolink_warehouse` 也是租户级共享，不新增 `shop_id`
> - 新增代发采购结算流水表、库存预警表、全局看板

---

## 数据库变更清单

### A. 需修改的表（已有，需 ALTER）

| 表名 | 状态 | 新增字段 |
|------|:----:|------|
| `taolink_product` | 待修改 | 新增 `shop_id`, `listing_status`, `listed_at`, `delisted_at` |
| `taolink_source_offer` | 待修改 | 新增 `shop_id`（可选，1688货源可跨店共享） |
| `taolink_order` | 待修改 | 新增 `shop_id` |
| `taolink_purchase` | 待修改 | 新增 `shop_id` |
| `taolink_purchase_line` | 待修改 | 新增 `shop_id`, `source_order_id`, `source_tracking_company`, `source_tracking_no`, `shipped_at`, `freight_cost`, `total_cost` |
| `taolink_sku_binding` | 待修改 | 新增 `shop_id` |
| `taolink_ticket` | 待修改 | 新增 `shop_id` |
| `taolink_inventory` | 待修改 | **不加 shop_id**；新增 `warning_min`, `overstock_days` |

### B. 不需改动的表（租户隔离已有 tenant_id）

| 表名 | 原因 |
|------|------|
| `taolink_product_sku` | 继承父商品，无需单独 shop_id |
| `taolink_order_line` | 继承父订单，无需单独 shop_id |
| `taolink_inventory_movement` | **库存全局共享**，不加 shop_id |
| `taolink_warehouse` | **仓库租户级共享**，不加 shop_id |

### C. 需新建的表

| 表名 | 状态 | 说明 |
|------|:----:|------|
| `taolink_shop` | **待建** | 用户淘宝店铺绑定表 |
| `taolink_search_cache` | **待建** | 搜索缓存表 |
| `taolink_monitor_daily_snapshot` | **已建** | 店铺监控每日快照表 |
| `taolink_inventory_alert` | **待建** | 库存预警记录（低库存/积压） |
| `taolink_settlement_record` | **待建** | 代发结算流水表 |
| `taolink_selection_score` | **远期** | AI 选品评分记录 |
| `taolink_content_pack` | **远期** | 上架内容包（标题/卖点/详情） |

---

## 后端开发任务

### 层级1：SaaS 基础设施

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| T-001 | `TaolinkShop` 实体类 | `entity/TaolinkShop.java` | **已完成** | id + tenant_id + taobao_seller_nick + api_session_key + status 等 |
| T-002 | `TaolinkShop` Mapper/Service/Controller | 全部三层 | **已完成** | CRUD + bind/unbind 接口 |
| T-003 | 部分实体新增 `shop_id` | Product, Order, Purchase, SourceOffer, SkuBinding, Ticket 的 entity | **已完成** | 注意：Inventory/Warehouse/InventoryMovement **不加** |
| T-004 | `TaolinkInventory` 新增预警阈值字段 | `entity/TaolinkInventory.java` | **已完成** | `warning_min`, `overstock_days` |
| T-005 | `TaolinkPurchaseLine` 新增发货/成本字段 | `entity/TaolinkPurchaseLine.java` | **已完成** | 运单号/运费/总成本等 |
| T-006 | 租户/店铺数据隔离拦截器 | `config/TenantShopInterceptor.java` | **已完成** | MyBatis-Plus 拦截器，自动注入 WHERE tenant_id + shop_id |
| T-007 | 数据库迁移 SQL 脚本 | `db/taolink_migration_v2.sql` | **已完成** | ALTER + CREATE TABLE |
| T-008 | 店铺绑定的 OAuth 流程 | `integrations/taobao/TaobaoOauthClient.java` | **待开发** | 后续迭代（非首批） |

### 层级2：商品管理（含上下架）

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| T-010 | 商品上架/下架接口 | `controller/TaolinkProductController` | **已完成** | POST `/taolink/product/listed` + `/taolink/product/delisted` |
| T-011 | 上架自动创建初始库存记录 | `service/` 业务逻辑 | **已完成** | 上架时为每个SKU创建库存（on_hand=0, reserved=0） |
| T-012 | 商品列表按 `shop_id` 过滤 | Controller list 方法 | **已完成** | 依赖 T-006 拦截器 |

### 层级3：订单管理（库存联动）

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| T-020 | 订单支付后自动预留库存 | `service/ITaolinkInventoryService` | **已完成** | 调用 `reserve()` |
| T-021 | 发货后扣减库存 | `service/ITaolinkInventoryService` | **已完成** | 新增 `deduct()` 方法：on_hand减少, reserved减少, 写入 Movement |
| T-022 | 订单履约分配接口 | `controller/TaolinkOrderController` | **已完成** | POST `/taolink/order/assignFulfillment`（stock / dropship） |
| T-023 | 代发订单自动创建采购任务 | `service/` 业务逻辑 | **已完成** | dropship 模式的订单行自动生成采购单 |

### 层级4：代发采购与结算

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| T-030 | `TaolinkSettlementRecord` 实体 | `entity/TaolinkSettlementRecord.java` | **已完成** | settle_type + amount + supplier_name + status 等 |
| T-031 | 结算 Mapper/Service/Controller | 全部三层 | **已完成** | 结算记录 CRUD + 月度对账接口 |
| T-032 | 代发订单发货回填接口 | `controller/TaolinkPurchaseController` | **已完成** | POST `/taolink/purchase/{lineId}/fillTracking`：写入运单号 + 生成结算记录 |
| T-033 | 月度对账单汇总接口 | `controller/TaolinkSettlementController` | **已完成** | GET `/taolink/settlement/monthly?supplier=xxx&month=2026-04` |

### 层级5：搜索中心（带缓存）

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| T-040 | `TaolinkSearchCache` 实体 | `entity/TaolinkSearchCache.java` | **已完成** | cache_key + platform + search_params + result_json + hit_count + expires_at |
| T-041 | Redis 缓存层服务 | `service/TaolinkSearchCacheService.java` | **已完成** | Redis string key-value，TTL 24h |
| T-042 | 统一搜索接口（先1688） | `controller/TaolinkSearchController` | **已完成** | 三级缓存链：Redis → DB → Onebound API |
| T-043 | 搜索结果转 source_offer 落库 | `service/` 业务逻辑 | **已完成** | 搜索结果批量写入 source_offer 表 |
| T-044 | 搜索缓存命中统计 | `controller/TaolinkSearchController` GET `/stats` | **已完成** | 返回今日命中/未命中/总量 |

### 层级6：库存分析与预警

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| T-050 | `TaolinkInventoryAlert` 实体 | `entity/TaolinkInventoryAlert.java` | **已完成** | product_sku_id + alert_type + threshold + status |
| T-051 | 库存 Mapper/Service/Controller | 全部三层 | **已完成** | 预警记录 CRUD |
| T-052 | 库存分析总览接口 | `controller/TaolinkInventoryController` | **已完成** | GET `/taolink/inventory/analysis/overview` + `/metrics` |
| T-053 | 库存预警定时检查任务 | `job/InventoryAlertCheckJob.java` | **已完成** | 定时扫描所有SKU可用库存，触发阈值则生成预警记录 |
| T-054 | 设置SKU预警阈值接口 | `controller/TaolinkInventoryController` | **已完成** | PUT `/taolink/inventory/{id}/threshold` |

### 层级7：店铺监控

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| T-060 | `TaolinkMonitorDailySnapshot` 实体 | `entity/TaolinkMonitorDailySnapshot.java` | **已完成** | |
| T-061 | 监控 Mapper/Service/Controller | 全部三层 | **已完成** | |
| T-062 | 监控概览数据接口 | `controller/TaolinkMonitorController` | **已完成** | GET `/taolink/monitor/{shopId}/overview` |
| T-063 | 监控趋势数据接口 | `controller/TaolinkMonitorController` | **已完成** | GET `/taolink/monitor/{shopId}/trend?days=7` |
| T-064 | 商品排行榜接口 | `controller/TaolinkMonitorController` | **已完成** | GET `/taolink/monitor/{shopId}/rankings` |
| T-065 | 每日快照定时任务 | `job/ShopMonitorSnapshotJob.java` | **已完成** | Quartz 每日凌晨1点执行 |

### 层级8：全局分析看板

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| T-070 | 全局总览指标接口 | `controller/TaolinkDashboardController` | **已完成** | GET `/taolink/dashboard/global/overview` |
| T-071 | 店铺排行接口 | `controller/TaolinkDashboardController` | **已完成** | GET `/taolink/dashboard/global/shop-ranking` |
| T-072 | 全局库存健康度接口 | `controller/TaolinkDashboardController` | **已完成** | GET `/taolink/dashboard/global/inventory-health` |
| T-073 | 全局预警汇总接口 | `controller/TaolinkDashboardController` | **已完成** | GET `/taolink/dashboard/global/alerts` |
| T-074 | 全局趋势接口 | `controller/TaolinkDashboardController` | **已完成** | GET `/taolink/dashboard/global/trend?days=30` |

### 层级9：AI 选品与上架（远期规划）

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| T-080 | `TaolinkSelectionScore` 实体 | `entity/` | **远期规划** | AI 选品评分记录 |
| T-081 | `TaolinkContentPack` 实体 | `entity/` | **远期规划** | 上架内容包（标题/卖点/详情） |
| T-082 | 选品评分引擎（规则+AI） | `service/` | **远期规划** | 规则引擎 + airag 模块调用 |
| T-083 | 内容包生成/审核接口 | `controller/` | **远期规划** | 通过/退回审核流 |
| T-084 | 内容包与商品关联 | `service/` | **远期规划** | 审核通过后生成 Product + ProductSku |

---

## 前端开发任务

### 层级1：基础框架

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| F-001 | Taolink API 定义层 | `jeecgboot-vue3/src/api/taolink/` | **已完成** | 所有 API 接口 TypeScript 定义 |
| F-002 | 全局店铺选择器组件 | `src/components/Taolink/ShopSelector.vue` | **已完成** | 顶部导航栏集成 |
| F-003 | 店铺状态 Pinia Store | `src/store/modules/taolink.ts` | **已完成** | 当前选中 shop_id |
| F-004 | Taolink 侧边栏菜单注册 | `router/`, 系统菜单配置 | **已完成** | 添加各模块路由 |

### 层级2：商铺管理

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| F-010 | 店铺列表页 | `src/views/taolink/shop/index.vue` | **已完成** | 表格 + 绑定/解绑 |
| F-011 | 店铺详情页 | `src/views/taolink/shop/detail.vue` | **待开发** | 信息 + API配额 + 监控摘要 |

### 层级3：商品管理

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| F-020 | 商品列表页（含上下架） | `src/views/taolink/product/index.vue` | **已完成** | 表格 + 上架/下架 + 搜索筛选 |
| F-021 | 商品新增/编辑页 | `src/views/taolink/product/form.vue` | **已完成** | 表单 + SKU编辑 |
| F-022 | 商品详情页 | `src/views/taolink/product/detail.vue` | **待开发** | SPU + SKU + 关联订单 + 货源 |

### 层级4：订单管理

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| F-030 | 订单列表页 | `src/views/taolink/order/index.vue` | **已完成** | 状态Tabs + 筛选 + 履约操作 |
| F-031 | 订单详情页 | `src/views/taolink/order/detail.vue` | **已完成** | 订单信息 + 商品明细 + 物流 |
| F-032 | 发货回填页（可批量） | `src/views/taolink/order/shipment.vue` | **已完成** | 物流公司 + 运单号 + 代发费用录入 |

### 层级5：库存管理

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| F-040 | 全局库存台账页 | `src/views/taolink/inventory/index.vue` | **已完成** | SKU维度 + 在仓/预留/可用 |
| F-041 | 出入库流水日志页 | `src/views/taolink/inventory/movements.vue` | **已完成** | 按时间倒序 |
| **F-042** | **库存分析看板页** | `src/views/taolink/inventory-analysis/index.vue` | **已完成** | 预警列表、周转率图表、呆滞品、库存价值 |
| **F-043** | **库存预警操作** | 同上 | **已完成** | 预警确认/解决 |
| **F-044** | **设置预警阈值** | 库存台账页内 | **已完成** | 编辑 warning_min / overstock_days |

### 层级6：代发采购与结算

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| F-050 | 采购任务列表页 | `src/views/taolink/purchase/index.vue` | **已完成** | 按供应商汇总 + 状态筛选 |
| **F-051** | **结算记录页** | `src/views/taolink/settlement/index.vue` | **已完成** | 按订单/供应商查询结算记录 |
| **F-052** | **月度对账单页** | `src/views/taolink/settlement/monthly.vue` | **已完成** | 按供应商按月汇总，可导出 |

### 层级7：搜索中心

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| F-060 | 统一搜索页 | `src/views/taolink/search/index.vue` | **已完成** | 搜索框 + 平台切换 + 结果列表 |
| F-061 | 商品搜索详情页 | `src/views/taolink/search/item-detail.vue` | **已完成** | 详情 + 采购建议 + 一键导入 |
| F-062 | 缓存统计面板 | `src/views/taolink/search/stats.vue` | **已完成** | 命中率 + 配额展示 |

### 层级8：店铺监控

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| F-070 | 监控概览仪表盘 | `src/views/taolink/monitor/index.vue` | **已完成** | ECharts折线图 + KPI卡片 |
| F-071 | 商品排行榜 | `src/views/taolink/monitor/rankings.vue` | **已完成** | Top10 销量/金额 |
| F-072 | 异常告警列表 | `src/views/taolink/monitor/alerts.vue` | **已完成** | 异常事件列表 |

### 层级9：全局分析看板（SaaS 平台侧）

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| F-080 | 全局总览仪表盘 | `src/views/taolink/dashboard/index.vue` | **已完成** | 全局指标 + 预警汇总 |
| F-081 | 店铺排行页 | `src/views/taolink/dashboard/shops.vue` | **已完成** | 按订单量/GMV排名 |
| F-082 | 全局库存健康度 | `src/views/taolink/dashboard/inventory-health.vue` | **已完成** | 跨店库存汇总 |
| **F-083** | **全局预警中心** | `src/views/taolink/dashboard/alerts.vue` | **已完成** | 库存+监控+所有预警汇总 |

### 层级10：AI 选品与上架（远期规划）

| 任务ID | 任务 | 关联文件 | 状态 | 备注 |
|--------|------|---------|:----:|------|
| F-090 | 选品评分页 | `src/views/taolink/selection/score.vue` | **远期规划** | 评分列表 + 详情 |
| F-091 | 内容包编辑页 | `src/views/taolink/selection/content-pack.vue` | **远期规划** | 标题/卖点/详情编辑 |
| F-092 | 内容包审核页 | `src/views/taolink/selection/review.vue` | **远期规划** | 通过/退回 |

> 注：**加粗** 的任务是 v0.3.0 新增的，v0.2.0 中不存在。

---

## 开发进度总览

### 按模块统计

| 模块 | 后端 | 前端 | 总计 | 完成率 |
|------|:----:|:----:|:----:|:------:|
| SaaS 基础设施 | 7 / 8T | -- | 8 | 87.5% |
| 商品管理 | 3 / 3T | 2 / 3F | 6 | 83.3% |
| 订单管理 | 4 / 4T | 3 / 3F | 7 | 100% |
| 代发采购与结算 | 4 / 4T | 3 / 3F | 7 | 100% |
| 搜索中心 | 5 / 5T | 3 / 3F | 8 | 100% |
| 库存分析与预警 | 5 / 5T | 5 / 5F | 10 | 100% |
| 店铺监控 | 6 / 6T | 3 / 3F | 9 | 100% |
| 全局分析看板 | 5 / 5T | 4 / 4F | 9 | 100% |
| AI 选品/上架 | 0 / 5T | 0 / 3F | 8 | **远期** |
| **合计** | **44** | **26** | **70** | **94.6%** |

### 当前开发状态

| 任务ID | 任务名称 | 状态 | 开始日期 | 完成日期 | 预计完成日期 | 备注 |
|--------|----------|------|----------|----------|--------------|------|
| T-007 | 数据库迁移 SQL 脚本 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-14 | 优先执行 |
| T-001 | TaolinkShop 实体类 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-15 | 实体类已存在且完整 |
| T-002 | TaolinkShop Mapper/Service/Controller | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-16 | 所有三层代码已存在且完整 |
| T-003 | 部分实体新增 shop_id | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-17 | 所有实体已添加 shop_id 字段 |
| T-004 | TaolinkInventory 新增预警阈值字段 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-18 | 预警阈值字段已添加 |
| T-005 | TaolinkPurchaseLine 新增发货/成本字段 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-19 | 发货/成本字段已添加 |
| T-006 | 租户/店铺数据隔离拦截器 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-20 | 已创建 TenantShopInterceptor 和 ShopIdFilterAspect |
| F-010 | 前端店铺列表页 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-21 | 前端店铺列表页已存在且完整 |
| T-010 | 商品上架/下架接口 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-22 | 接口已存在 |
| T-011 | 上架自动创建初始库存记录 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-23 | 功能已实现 |
| T-012 | 商品列表按 shop_id 过滤 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-24 | 功能已实现 |
| T-020 | 订单支付后自动预留库存 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-25 | 方法已实现 |
| T-021 | 发货后扣减库存 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-26 | 方法已实现 |
| T-022 | 订单履约分配接口 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-27 | 接口已添加 |
| T-023 | 代发订单自动创建采购任务 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-28 | 功能已实现 |
| T-030 | TaolinkSettlementRecord 实体 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-29 | 实体类已创建 |
| T-031 | 结算 Mapper/Service/Controller | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-30 | 所有三层代码已实现 |
| T-032 | 代发订单发货回填接口 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-01 | 接口已实现 |
| T-033 | 月度对账单汇总接口 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-02 | 接口已实现 |
| T-040 | TaolinkSearchCache 实体 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-03 | 实体类已创建 |
| T-041 | Redis 缓存层服务 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-04 | 服务已实现 |
| T-042 | 统一搜索接口（先1688） | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-05 | 接口已实现 |
| T-043 | 搜索结果转 source_offer 落库 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-06 | 功能已实现 |
| T-044 | 搜索缓存命中统计 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-07 | 接口已实现 |
| T-050 | TaolinkInventoryAlert 实体 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-08 | 实体类已创建 |
| T-051 | 库存 Mapper/Service/Controller | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-09 | 所有三层代码已实现 |
| T-052 | 库存分析总览接口 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-10 | 接口已实现 |
| T-053 | 库存预警定时检查任务 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-11 | 任务已实现 |
| T-054 | 设置SKU预警阈值接口 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-12 | 接口已实现 |
| F-001 | Taolink API 定义层 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-28 | 已创建 product、order、inventory API 定义 |
| F-002 | 全局店铺选择器组件 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-29 | 已创建 ShopSelector 组件 |
| F-003 | 店铺状态 Pinia Store | 已完成 | 2026-04-12 | 2026-04-12 | 2026-04-30 | 已创建 taolink store |
| F-004 | Taolink 侧边栏菜单注册 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-01 | 已配置路由 |
| F-020 | 商品列表页（含上下架） | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-02 | 页面已实现 |
| F-021 | 商品新增/编辑页 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-03 | 页面已实现 |
| F-030 | 订单列表页 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-04 | 页面已实现 |
| F-031 | 订单详情页 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-05 | 页面已实现 |
| F-032 | 发货回填页（可批量） | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-06 | 页面已实现 |
| F-040 | 全局库存台账页 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-07 | 页面已实现 |
| F-041 | 出入库流水日志页 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-08 | 页面已实现 |
| F-042 | 库存分析看板页 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-09 | 页面已实现 |
| F-043 | 库存预警操作 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-10 | 功能已实现 |
| F-044 | 设置预警阈值 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-11 | 功能已实现 |
| F-050 | 采购任务列表页 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-12 | 页面已实现 |
| F-051 | 结算记录页 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-13 | 页面已实现 |
| F-052 | 月度对账单页 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-14 | 页面已实现 |
| F-060 | 统一搜索页 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-15 | 页面已实现 |
| F-061 | 商品搜索详情页 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-16 | 页面已实现 |
| F-062 | 缓存统计面板 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-05-17 | 页面已实现 |
| T-060 | TaolinkMonitorDailySnapshot 实体 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-06-01 | 实体类已存在且完整 |
| T-061 | 监控 Mapper/Service/Controller | 已完成 | 2026-04-12 | 2026-04-12 | 2026-06-02 | 所有三层代码已实现 |
| T-062 | 监控概览数据接口 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-06-03 | 接口已实现 |
| T-063 | 监控趋势数据接口 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-06-04 | 接口已实现 |
| T-064 | 商品排行榜接口 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-06-05 | 接口已实现 |
| T-065 | 每日快照定时任务 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-06-06 | 任务已实现 |
| F-070 | 监控概览仪表盘 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-06-07 | 页面已实现 |
| F-071 | 商品排行榜 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-06-08 | 页面已实现 |
| F-072 | 异常告警列表 | 已完成 | 2026-04-12 | 2026-04-12 | 2026-06-09 | 页面已实现 |

### 开发计划

| 迭代轮次 | 目标 | 任务范围 | 预计时间 |
|----------|------|----------|----------|
| 第1轮 | SaaS 基础 + 数据库迁移 | T-007 → T-001 → T-002 → T-003 → T-004 → T-005 → T-006 → F-010 | 2026-04-13 至 2026-04-21 |
| 第2轮 | 商品管理 + 订单管理 | T-010 → T-011 → T-012 → T-020 → T-021 → T-022 → F-001 → F-002 → F-003 → F-004 → F-020 → F-021 → F-030 → F-031 | 2026-04-22 至 2026-05-05 |
| 第3轮 | 库存分析（核心新增） | T-050 → T-051 → T-052 → T-053 → T-054 → F-040 → F-041 → F-042 → F-043 → F-044 | 2026-05-06 至 2026-05-15 |
| 第4轮 | 代发采购与结算 | T-030 → T-031 → T-032 → T-033 → F-032 → F-050 → F-051 → F-052 | 2026-05-16 至 2026-05-23 |
| 第5轮 | 搜索中心 | T-040 → T-041 → T-042 → T-043 → T-044 → F-060 → F-061 → F-062 | 2026-05-24 至 2026-05-31 |
| 第6轮 | 店铺监控 | T-060 → T-061 → T-062 → T-063 → T-064 → T-065 → F-070 → F-071 → F-072 | 2026-06-01 至 2026-06-08 |
| 第7轮 | 全局分析看板 | T-070 → T-071 → T-072 → T-073 → T-074 → F-080 → F-081 → F-082 → F-083 | 2026-06-09 至 2026-06-16 |

---

## 已开发内容（基础底座）

以下内容在代码库中已存在，是 SaaS 化的基础：

| 内容 | 状态 | 说明 |
|------|:----:|------|
| Taolink 12个基础实体 | 已存在 | 含 Product, ProductSku, SourceOffer, Order, OrderLine, Inventory, InventoryMovement, Warehouse, Purchase, PurchaseLine, SkuBinding, Ticket |
| 5个基础 Controller | 已存在 | Inventory, Order, Purchase, SourceOffer, Ticket |
| 12个 Mapper | 已存在 | 纯 BaseMapper CRUD |
| 12个 Service | 已存在 | 11薄CRUD + InventoryService（reserve/release/adjust） |
| OneboundClient | 已存在 | 1688/淘宝商品详情 + SourceOffer Ingest 流程 |
| SourceOffer Ingest | 已存在 | fetchAndUpsert 流程 |
| airag 模块 | 已存在 | AI 工作流/知识库/对话——可用于后续 AI 选品 |
| 前端 Taolink 页面 | **不存在** | 全部待开发 |

---

## 推荐开发顺序（迭代路线 v2.0）

### 第 1 轮：SaaS 基础 + 数据库迁移
> 目标：建表、定义实体、完成店铺 CRUD

1. **T-007** → 数据库迁移 SQL（CREATE + ALTER）
2. **T-001** → `TaolinkShop` 实体
3. **T-002** → 店铺 Mapper/Service/Controller
4. **T-003/T-004/T-005** → 实体字段扩展
5. **T-006** → 租户/店铺数据隔离拦截器
6. **F-010** → 前端店铺列表页

### 第 2 轮：商品管理 + 订单管理
> 目标：店铺内商品上下架、订单查看

7. **T-010/T-011/T-012** → 商品上下架
8. **T-020/T-021/T-022** → 订单库存联动
9. **F-001/F-002/F-003/F-004** → 前端基础框架
10. **F-020/F-021** → 前端商品页
11. **F-030/F-031** → 前端订单页

### 第 3 轮：库存分析（核心新增）
> 目标：全局库存视图 + 低库存/积压双预警 + 健康度分析

12. **T-050/T-051/T-052/T-053/T-054** → 库存分析后端
13. **F-040/F-041/F-042/F-043/F-044** → 前端库存页

### 第 4 轮：代发采购与结算
> 目标：发货回填 + 自动记录结算 + 月度对账

14. **T-030/T-031/T-032/T-033** → 结算后端
15. **F-032** → 发货回填页
16. **F-050/F-051/F-052** → 前端采购结算页

### 第 5 轮：搜索中心（带缓存）
> 目标：1688搜索带三级缓存

17. **T-040/T-041/T-042/T-043/T-044** → 搜索缓存
18. **F-060/F-061/F-062** → 前端搜索页

### 第 6 轮：店铺监控
> 目标：店铺仪表盘

19. **T-060/T-061/T-062/T-063/T-064/T-065** → 监控后端
20. **F-070/F-071/F-072** → 前端监控页

### 第 7 轮：全局分析看板
> 目标：跨店铺总览

21. **T-070/T-071/T-072/T-073/T-074** → 全局看板后端
22. **F-080/F-081/F-082/F-083** → 前端全局看板

### 远期：AI 选品 + 上架
23. **T-080~T-084** + **F-090~F-092**

---

## 使用 AI 开发的最佳实践

### 给 AI 的上下文提示
每次让 AI 协助开发时，请提供：

1. 当前要开发的**任务 ID**（如 T-001）
2. 对应的**关联文件路径**
3. 本文件中的**任务说明**
4. 现有代码的相关部分（读 entity/Controller/Service）

### 开发流程建议

1. **后端先行**：先完成实体 + 接口，再做前端页面
2. **小步快跑**：每次完成 1-2 个任务，避免大段提交
3. **测试验证**：每个任务完成后验证接口和前端页面
4. **及时更新本文档**：修改状态为"已开发"，填写完成日期

### 常用命令速查

```bash
cd jeecg-boot && mvn clean install     # 后端构建
cd jeecgboot-vue3 && pnpm dev          # 前端开发服务器 (port 3100)
cd jeecgboot-vue3 && pnpm build        # 前端生产构建
docker-compose up -d                   # Docker 全栈
```

### 关键路径

```
后端: jeecg-boot/jeecg-boot-module/jeecg-module-taolink/src/main/java/org/jeecg/modules/taolink/
      ├── entity/          （实体定义）
      ├── mapper/          （MyBatis Mapper）
      ├── service/
      │     ├── ITaolink*Service.java    （接口）
      │     └── impl/
      │           └── Taolink*ServiceImpl.java （实现）
      ├── controller/      （REST API）
      ├── integrations/    （外部API客户端）
      ├── config/          （拦截器等配置，新增）
      └── job/             （定时任务，新增）

前端: jeecgboot-vue3/
      ├── src/api/taolink/              （API定义，待创建）
      ├── src/components/Taolink/       （店铺选择器等组件）
      └── src/views/taolink/            （页面组件，待创建）
          ├── shop/
          ├── product/
          ├── order/
          ├── inventory/
          ├── settlement/
          ├── search/
          ├── monitor/
          ├── dashboard/
          └── selection/（远期）
```
