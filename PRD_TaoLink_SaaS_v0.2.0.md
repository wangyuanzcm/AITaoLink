# 《TaoLink SaaS 平台产品需求文档》

- 产品代号：TaoLink SaaS
- 文档版本：0.3.0（SaaS 化 + 共享库存 + AI 选品规划）
- 状态：Draft（评审通过后方可进入研发阶段）
- 技术底座：JeecgBoot 3.9.1（前后端分离、RBAC 多租户、代码生成、在线表单/报表/流程、任务调度）
- 上一版本：[PRD_TaoLink_产品设计文档.md v0.1.0](./PRD_TaoLink_产品设计文档.md)
- 业务规划依据：[one_person_taobao_1688_plan.md](./one_person_taobao_1688_plan.md)

---

## 1. 变更记录

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 0.1.0 | 2026-04-01 | 初版 PRD：一人公司场景闭环，包含选品/上新/订单/履约/库存/客服/报表 |
| 0.2.0 | 2026-04-12 | SaaS 化迭代：多租户商铺管理、数据隔离、搜索缓存、店铺监控看板、商品上下架关联订单 |
| 0.3.0 | 2026-04-12 | **架构调整**：库存改为全局共享（跨店铺共用）、库存分析/预警（低库存+积压双预警）、总分析看板（跨店商品+订单全局视图）、代发采购结算（发货记录费用结算）、AI选品上架规划对齐 |

---

## 2. SaaS 化概述

### 2.1 目标

以 SaaS 模式运营 TaoLink 平台，多用户共同使用同一平台，每个用户管理自己的一个或多个淘宝店铺：

- 用户登录后，**仅能看到和操作自己名下的商铺/商品/订单数据**
- 每个商铺独立管理商品（上下架）、商品关联的订单信息
- **库存全局共享**：所有店铺共用同一个库存池，由 SaaS 平台管理员/仓储人员统一管理。卖家无需关心库存数字，只关注自己的订单和发货
- 代发订单自动触发采购任务，发货后记录发货数据 + 结算代发费用
- **库存分析**：支持设置低库存预警值和库存积压预警值，异常时主动告警
- **全局分析**：提供跨店铺的总分析看板，汇总所有店铺的商品情况和订单情况
- 集成外部接口（Onebound）搜索 1688 商品和淘宝商品，搜索结果缓存，避免重复搜索浪费调用次数
- 提供店铺监控面板，显示指定淘宝店铺最近几天的商品上下架动态和销量走势
- **AI 选品与上架**：预留 AI 选品评分、内容包生成、上架草稿能力（详见第 10 节规划）

### 2.2 目标用户

| 用户类型 | 描述 |
|----------|------|
| 个人卖家 | 一人公司模式，管理单个淘宝店铺，通过1688进货、上架销售 |
| 小型团队 | 多成员协作，管理1-3家店铺，需要角色分工（运营/客服/仓储） |
| SaaS 管理员 | 管理平台级租户、套餐、接口配额、系统监控 |

### 2.3 SaaS 核心模块

| 模块 | 描述 |
|------|------|
| **平台总览分析** | 跨店铺全局视图：所有店铺的商品汇总、订单汇总、库存健康度、预警总览 |
| **商铺管理** | 用户绑定自己的淘宝/1688 店铺，每个用户可拥有多家店铺，店铺级别隔离 |
| **商品管理** | 每个商铺独立的商品上下架管理，SKU规格映射到1688货源，商品关联订单 |
| **订单管理** | 按店铺隔离的订单列表，订单与库存联动，发货记录，代发费用结算 |
| **库存管理（全局共享）** | 跨店铺的统一库存池，库存分析（低库存预警 + 积压预警），全局出入库管理 |
| **采购与结算** | 代发采购任务（dropship），发货后自动记录发货数据，结算代发供应商费用 |
| **搜索中心** | 1688/淘宝商品搜索，带缓存去重，减少外部API重复调用 |
| **店铺监控** | 指定店铺的仪表盘，展示近N天商品上架/下架趋势、销量统计、异常告警 |
| **AI 选品/上架（规划）** | AI 选品评分、内容生成、上架草稿（对齐 v0.1.0 PRD 选品闭环） |

---

## 3. SaaS 数据隔离架构

### 3.1 方案选择

**共享数据库，逻辑隔离**（租户共享模式），通过 `tenant_id` 实现数据隔离。

- 优点：运维成本低，支持租户级别平滑迁移到独立库
- 隔离级别：**行级隔离** —— 所有业务表增加 `tenant_id` 和 `shop_id` 字段
- 查询拦截：通过 MyBatis-Plus `TenantLineInnerInterceptor` 自动注入租户条件

### 3.2 租户-店铺-数据层级

```
Tenant（租户=用户/企业）
 ├── [共享资源]
 │     ├── Warehouse（仓库，租户级共享）
 │     ├── Inventory（库存台账，店铺共用，不按店铺隔离）
 │     └── InventoryMovement（库存流水，租户级）
 │
 └── Shop（淘宝店铺，1对多）
      ├── Product（商品，按店铺隔离）
      │     └── ProductSku
      ├── Listing（淘宝上架商品）
      ├── Order（订单，按店铺隔离）
      │     └── OrderLine
      ├── Purchase（采购代发，按店铺归因）
      │     └── PurchaseLine
      ├── Ticket（工单，按店铺隔离）
      └── MonitorSnapshot（监控快照，按店铺）
```

### 3.3 多租户核心字段

所有业务表新增以下字段（JeecgBoot 已预留 `sys_org_code` 可用于租户隔离）：

| 字段 | 类型 | 说明 |
|------|------|------|
| `tenant_id` | string | 租户ID（JeecgBoot 内置） |
| `shop_id` | string | 店铺ID（本平台新增，关联 `taolink_shop`） |

### 3.4 新增：商铺表结构

**taolink_shop（用户淘宝店铺）**

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| id | string | 是 | 主键 |
| tenant_id | string | 是 | 租户ID |
| taobao_seller_nick | string | 是 | 淘宝卖家昵称（店铺标识） |
| api_session_key | string | 是 | 淘宝开放平台 session key |
| api_expire_at | datetime | 是 | API 授权过期时间 |
| status | enum | 是 | `active`/`disabled`/`unbound` |
| bind_platforms | json | 否 | 绑定的平台 `["taobao","1688"]` |
| owner_id | string | 是 | 关联的 JeecgBoot 用户ID |
| remark | string | 否 | 备注信息 |
| monitoring_enabled | bool | 否 | 是否启用监控，默认 `true` |
| monitoring_days | int | 否 | 监控天数回溯，默认 `7` |

### 3.5 权限模型（SaaS 扩展）

| 层级 | 规则 |
|------|------|
| 平台管理员（`role=admin`） | 可查看所有租户数据，管理租户列表 |
| 租户管理员（`role=tenant_admin`） | 可查看和管理本租户所有店铺数据 |
| 租户员工（`role=*`，含本部门/本店铺） | 仅能查看分配的店铺数据 |
| 超级隔离规则 | `tenant_id` 不可越权，`shop_id` 不可跨店铺（除非角色允许） |

---

## 4. 业务模块需求

### 4.1 商铺管理

#### 4.1.1 功能列表

| 功能点 | 说明 | 优先级 |
|--------|------|--------|
| 绑定淘宝店铺 | 通过淘宝开放平台 OAuth 流程，用户授权后将店铺绑定到系统 | 高 |
| 解绑店铺 | 解除店铺绑定，保留历史数据 | 中 |
| 店铺列表 | 显示用户名下所有店铺，含状态、API授权状态、商品数量、订单数量 | 高 |
| 店铺详情 | 显示店铺基本信息、API 配额、监控指标摘要 | 高 |
| API 授权续期提醒 | API session 过期前 N 天提醒用户重新授权 | 中 |
| 店铺标签 | 自定义标签分组，如"主营女装"、"高利润店" | 低 |

#### 4.1.2 关键交互

- 店铺绑定入口：点击"绑定新店" → 跳转淘宝授权页面 → 授权回调写入 session
- 列表页显示每店铺的"商品数/待发货订单/近7天销量"快速统计卡片

---

### 4.2 商品管理

#### 4.2.1 功能列表

| 功能点 | 说明 | 优先级 |
|--------|------|--------|
| 商品列表（按店铺隔离） | 列表显示商品名称、状态（上架/下架/草稿）、关联SKU数、库存总数、近7天销量 | 高 |
| 新增商品（手动/SKU导入） | 手动录入或从1688搜索导入商品信息 | 高 |
| 商品上下架 | 一键上架/下架，下架后不可被订单关联 | 高 |
| 商品详情 | 显示商品SPU信息、SKU列表、关联的1688货源、关联订单摘要、库存分布 | 高 |
| 商品-库存联动 | 上架即创建对应库存记录（on_hand=0），下架不删除库存 | 高 |
| 商品-订单关联 | 订单行关联商品SKU，出单后自动预留对应库存 | 高 |
| 批量操作 | 批量上架/下架/调价 | 中 |
| 商品搜索/筛选 | 按名称、状态、价格、库存、销量、分类筛选 | 高 |
| 商品评分/选品建议 | 结合AI对商品生成评分和建议 | 中 |

#### 4.2.2 关键字段变更

在已有 `TaolinkProduct` 中新增字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `shopId` | string | 所属店铺ID（关联 `taolink_shop`） |
| `listingStatus` | enum | `listed`/`delisted`/`draft`（上下架状态，独立于 `status`） |
| `listedAt` | datetime | 上架时间 |
| `delistedAt` | datetime | 下架时间 |

---

### 4.3 订单管理

#### 4.3.1 功能列表

| 功能点 | 说明 | 优先级 |
|--------|------|--------|
| 订单列表（按店铺隔离） | 按状态Tabs分栏：待付款/待发货/已发货/已完成/已关闭/售后中 | 高 |
| 订单详情 | 显示买家信息、商品明细、物流信息、履约状态、关联供应商 | 高 |
| 订单-库存联动 | 已支付订单自动预留库存（调 reserve 接口），发货后扣减库存 | 高 |
| 履约分配 | 自动/手动分配 stock 或 dropship 模式 | 高 |
| 发货回填 | 物流公司+运单号回填，可批量操作 | 高 |
| 订单同步 | 手动触发/定时任务从淘宝开放平台同步订单 | 高 |
| 订单搜索 | 按订单号、商品名、买家昵称筛选 | 中 |

---

### 4.4 库存管理（全局共享模式）

#### 4.4.1 设计原则

**库存跨店铺共享**：个人卖家不需要关心"哪个仓库还有多少"，只需要知道"还能不能发货"。库存由 SaaS 管理员/仓储人员统一管理，卖家仅查询库存状态（是否缺货），不可修改库存数字。

| 角色 | 库存权限 |
|------|----------|
| SaaS 管理员/仓储人员 | 新增仓库、手动入库/出库、调整库存、设置预警阈值 |
| 卖家（租户） | 仅查看：SKU 维度显示 在手/预留/可用，标记是否"缺货" |
| 系统自动 | 出单预留库存、发货后扣减库存、采购入库自动增加 on_hand |

#### 4.4.2 功能列表

| 功能点 | 说明 | 优先级 |
|--------|------|--------|
| 全局库存台账 | SKU 维度展示所有商品在仓数量：在手/预留/可用 | 高 |
| 手动入库 | 手动增加库存（采购入库/退货入库/调整） | 高 |
| 发货扣减 | 订单发货后自动扣减 on_hand（+ reserved 同时减少） | 高 |
| 出入库流水 | 每次库存变更都有 Movement 记录，可追溯 | 高 |
| 库存分析总览 | 全局库存健康度：品类分布、库存周转率、呆滞品占比 | 高 |
| 低库存预警 | 当可用数量（on_hand - reserved）≤ 预警值时触发告警。预警值可按 SKU 分别设置（`warning_min`），也可设全局默认值（默认 5） | 高 |
| 积压预警 | 当在库数量（on_hand）超过积压上限时触发告警。积压上限 = 近30天日均销量 × `overstock_days` + 安全缓冲。`overstock_days` 可配置（默认 30 天） | 高 |
| 预警规则管理 | 设置/修改低库存预警值和积压预警阈值，支持全局默认 + SKU 级别覆盖 | 中 |
| 库存盘点 | 支持盘点模式，批量调整库存 | 低 |

#### 4.4.3 新增实体字段

**taolink_inventory 新增字段：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `warning_min` | int | 低库存预警阈值（可按 SKU 设置，>0 则覆盖全局默认） |
| `overstock_days` | int | 积压天数阈值（可按 SKU 设置，>0 则覆盖全局默认），默认 30 |

**taolink_inventory_alert（库存预警记录表）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 主键 |
| product_sku_id | string | 关联的 SKU ID |
| alert_type | enum | `low_stock`（库存不足）/ `overstock`（库存积压） |
| current_on_hand | int | 告警时在库数量 |
| current_available | int | 告警时可用数量（on_hand - reserved） |
| threshold | int | 触发的阈值 |
| status | enum | `open`/`acknowledged`/`resolved` |
| created_at | datetime | 告警创建时间 |
| resolved_at | datetime | 告警解决时间 |

#### 4.4.4 库存分析总览接口

```
GET /taolink/inventory/analysis/overview
```

返回聚合数据：
- 总 SKU 数、有库存的 SKU 数、缺货 SKU 数
- 低库存预警 SKU 列表（current_available ≤ warning_min）
- 积压预警 SKU 列表（on_hand > overstock_threshold）
- 按仓库/品类的库存分布（饼图数据）
- 近30天库存走势

```
GET /taolink/inventory/analysis/metrics
```

返回：
- 库存周转率（近30天出库数 / 平均在库数）
- 呆滞品比例（90天无出单但仍有库存的 SKU）
- 库存价值合计（各 SKU on_hand × cost_price）

---

### 4.5 搜索中心（1688/淘宝商品搜索，带缓存）

#### 4.5.1 需求背景

Onebound API 调用有次数限制，同一个搜索条件重复调用会浪费配额。需要实现智能缓存。

#### 4.5.2 功能列表

| 功能点 | 说明 | 优先级 |
|--------|------|--------|
| 统一搜索接口 | 支持按关键词搜索1688/淘宝商品，返回商品列表 | 高 |
| 搜索缓存 | 以 `platform + searchQuery + pageNo` 为缓存key，Redis 缓存默认24h | 高 |
| 缓存命中跳过 | 缓存命中则直接返回，不调外部API；未命中则调用并写入缓存 | 高 |
| 缓存过期策略 | TTL可配置（默认24h），支持手动刷新缓存 | 高 |
| 调用次数面板 | 显示今日API调用次数、缓存命中率、各平台配额剩余 | 中 |
| 搜索结果详情 | 点击商品查看1688详情或淘宝详情，支持一键创建采购 | 中 |

#### 4.5.3 新增实体结构

**taolink_search_cache（搜索缓存表）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 主键 |
| cache_key | string | 缓存key，如 `1688_search:手机壳:p1` |
| platform | string | `1688` / `taobao` |
| search_params | json | 搜索参数快照 |
| result_json | json | 搜索结果（商品列表） |
| hit_count | int | 命中次数 |
| expires_at | datetime | 过期时间 |
| created_at | datetime | 创建时间 |

#### 4.5.4 缓存层级

```
SearchController.getResults(query, platform, page)
  │
  ├── 1. 检查 Redis 内存缓存（TTL 24h）
  │     ├── 命中 → 直接返回
  │     └── 未命中 → 继续
  │
  ├── 2. 检查 taolink_search_cache 表（DB级缓存）
  │     ├── 命中 → 返回并预热 Redis
  │     └── 未命中 → 继续
  │
  └── 3. 调用 Onebound API
        ├── 成功 → 写入 Redis + 写入 DB → 返回
        └── 失败 → 抛错误，不消耗缓存位
```

---

### 4.6 店铺监控

#### 4.6.1 需求背景

用户需要知道某个淘宝店铺近期的商品动态（上下架）和销量走势，便于把握运营状况。

#### 4.6.2 功能列表

| 功能点 | 说明 | 优先级 |
|--------|------|--------|
| 商品上下架趋势图 | 近N天每天的上架数/下架数折线图 | 高 |
| 每日销量趋势图 | 近N天每日订单数/GMV折线图 | 高 |
| 今日概览卡片 | 当日订单数、GMV、新增上架商品数、退款数 | 高 |
| Top商品排行 | 近N天销量Top10/金额Top10商品列表 | 中 |
| 异常告警 | 库存为0仍在上售品、近3日0单、销量骤降等 | 中 |
| 店铺切换 | 多店铺用户可切换查看不同店铺监控数据 | 高 |
| 数据刷新间隔 | 监控数据由定时任务（Quartz）汇总，可配置刷新频率 | 中 |

#### 4.6.3 新增实体结构

**taolink_monitor_daily_snapshot（监控每日快照）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 主键 |
| shop_id | string | 店铺ID |
| snapshot_date | date | 快照日期 |
| product_count | int | 商品总数 |
| listed_count | int | 在售商品数 |
| delist_count | int | 下架商品数 |
| new_listed_count | int | 当日新增上架数 |
| new_delisted_count | int | 当日下架数 |
| order_count | int | 当日订单数 |
| order_amount | int | 当日GMV（分） |
| refund_count | int | 当日退款数 |
| inventory_item_count | int | 库存有货SKU数 |
| snapshot_json | json | 附加数据（Top商品ID列表等） |

#### 4.6.4 定时任务

通过 JeecgBoot 内置的 Quartz 调度器，每天凌晨 01:00 执行 `ShopMonitorSnapshotJob`：

1. 遍历所有 `active` 状态且 `monitoring_enabled=true` 的店铺
2. 统计该店铺前一日的：商品上架/下架数量、订单数/GMV、库存变化
3. 写入 `taolink_monitor_daily_snapshot` 表
4. 检查触发阈值，若有异常则写入告警记录

---

### 4.7 代发采购与费用结算

#### 4.7.1 业务背景

1688 代发模式下，订单出单后需要向 1688 供应商采购，供应商发货后再回填物流单号给淘宝买家。每个代发订单产生的采购成本需要结算记录，以便后续对账和利润核算。

#### 4.7.2 功能列表

| 功能点 | 说明 | 优先级 |
|--------|------|--------|
| 代发采购任务自动生成 | 已支付订单的代发行 → 自动生成采购单 | 高 |
| 采购供应商确认 | 仓储人员确认采购单（数量、收货地址来自订单） | 高 |
| 发货回填 | 供应商发货后回填物流公司 + 运单号，系统自动回传到淘宝 | 高 |
| 结算记录 | 每笔代发订单发货后自动创建结算记录：采购成本 + 运费 | 高 |
| 结算对账单 | 按月/供应商汇总采购结算，用于和供应商对账 | 中 |
| 利润核算 | 订单售价 - 采购成本 - 运费 - 平台费 = 估算毛利 | 中 |

#### 4.7.3 新增实体字段

**taolink_purchase_line 新增字段：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `source_order_id` | string | 1688 采购订单号（供应商端） |
| `source_tracking_company` | string | 供应商发货的物流公司 |
| `source_tracking_no` | string | 供应商发货的运单号 |
| `shipped_at` | datetime | 供应商发货时间 |
| `freight_cost` | int | 运费（分） |
| `total_cost` | int | 总成本 = unit_cost × qty + freight_cost（分） |

**taolink_settlement_record（代发结算流水表）**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 主键 |
| purchase_line_id | string | 关联的采购单行 ID |
| order_id | string | 关联的订单 ID（溯源） |
| shop_id | string | 关联的店铺 ID（溯源） |
| settle_type | enum | `purchase_cost`（采购成本）/ `freight`（运费） |
| amount | int | 金额（分） |
| currency | string | 币种，默认 CNY |
| settle_status | enum | `pending`/`settled`/`void`（待结算/已结算/作废） |
| supplier_name | string | 供应商名称（从 1688 货源信息获取） |
| settled_at | datetime | 实际结算时间 |
| remark | string | 备注 |
| created_at | datetime | 创建时间 |

结算流程：
1. 代发订单发货 → 回填运单号 → 自动生成 `taolink_settlement_record` 记录
2. 仓储人员确认实际支付金额 → 修改 settlement 金额
3. 月度对账：按 supplier_name 聚合结算总额

---

### 4.8 平台总览分析看板

#### 4.8.1 需求背景

平台管理员/总运营需要一个全局视角，查看所有店铺的商品情况和订单情况，快速了解整体运营健康状况。

#### 4.8.2 功能列表

| 功能点 | 说明 | 优先级 |
|--------|------|--------|
| 全局指标卡片 | 平台总店铺数、总 SKU 数、总在售商品数、今日总订单数、今日总 GMV | 高 |
| 店铺排行 | 按订单量/GMV/商品数排名的店铺 Top 列表 | 高 |
| 全局商品汇总 | 按品类、价格段、上下架状态汇总的商品分布 | 高 |
| 全局订单汇总 | 按状态（待发货/已发货/已完成）汇总的订单分布 | 高 |
| 全局库存健康度 | 缺货 SKU 数、积压 SKU 数、库存周转率，跨店铺汇总 | 高 |
| 全局预警汇总 | 所有低库存预警 + 积压预警 + 店铺监控异常的汇总列表 | 高 |
| 全局预警设置 | 设置全局默认预警阈值（低库存默认值、积压天数默认值），各店铺/各SKU可覆盖 | 中 |
| 趋势图表 | 近30天总订单趋势、总GMV趋势、新店铺趋势 | 中 |

#### 4.8.3 接口

| 方法 | 路径 | 说明 | 权限key |
|------|------|------|---------|
| GET | `/taolink/dashboard/global/overview` | 全局总览指标 | `taolink:dashboard:view` |
| GET | `/taolink/dashboard/global/shop-ranking` | 店铺排行 | `taolink:dashboard:view` |
| GET | `/taolink/dashboard/global/inventory-health` | 全局库存健康度 | `taolink:dashboard:view` |
| GET | `/taolink/dashboard/global/alerts` | 全局预警汇总 | `taolink:dashboard:view` |
| GET | `/taolink/dashboard/global/trend?days=30` | 全局趋势数据 | `taolink:dashboard:view` |

---

### 4.9 采购管理增强

| 功能点 | 说明 | 优先级 |
|--------|------|--------|
| 采购任务自动关联店铺 | 代发采购任务关联到对应商品的店铺 | 高 |
| 采购单状态流转 | draft → submitted → shipped → arrived → closed | 中 |

#### 4.9.1 SKU绑定增强

| 功能点 | 说明 | 优先级 |
|--------|------|--------|
| 多货源绑定 | 一个内部SKU可绑定多个1688货源，设主供货源 | 中 |
| 自动成本更新 | 货源价格变动时自动更新SKU成本 | 低 |

---

## 5. 全局数据模型变更清单

### 5.1 现有实体变更

> 库存表和仓库表**不加 `shop_id`**，因为库存是全局共享的，按租户级别隔离即可。

| 已有实体 | 新增字段 | 说明 |
|----------|----------|------|
| TaolinkWarehouse | `tenant_id`（已有），无需 shop_id | 仓库为租户级共享资源 |
| TaolinkInventory | `tenant_id`（已有），无需 shop_id；新增 `warning_min`, `overstock_days` | 库存为全局共享 |
| TaolinkInventoryMovement | `tenant_id`（已有），无需 shop_id | 库存流水全局共享 |
| TaolinkProduct | `shopId`（关联店铺） | 商品按店铺隔离 |
| TaolinkOrder | `shopId`（关联店铺） | 订单按店铺隔离 |
| TaolinkOrderLine | `shopId`（继承父订单） | 订单项 |
| TaolinkPurchase | `shopId`（关联店铺） | 采购按店铺归因 |
| TaolinkPurchaseLine | `shopId`（继承），新增 `source_order_id`, `source_tracking_company`, `source_tracking_no`, `shipped_at`, `freight_cost`, `total_cost` | 采购明细 + 发货信息 + 成本 |
| TaolinkSourceOffer | `shopId`（关联店铺，可选，1688货源可跨店共享） | |
| TaolinkSkuBinding | `shopId`（关联店铺） | |
| TaolinkTicket | `shopId`（关联店铺，继承父订单） | |

### 5.2 新增实体汇总

| 新实体 | 说明 |
|--------|------|
| `TaolinkShop` | 用户绑定的淘宝店铺 |
| `TaolinkSearchCache` | 搜索缓存记录表 |
| `TaolinkMonitorDailySnapshot` | 店铺监控每日快照 |
| `TaolinkInventoryAlert` | 库存预警记录（低库存/积压） |
| `TaolinkSettlementRecord` | 代发结算流水表 |

---

## 8. AI 选品与上架规划

> 本模块为远期规划，基于 [PRD_TaoLink_产品设计文档 v0.1.0](./PRD_TaoLink_产品设计文档.md) 的选品闭环需求，分期落地。

### 8.1 业务流

```
1688 搜索/筛选 → 选品评分（规则打分 + AI 摘要） → 内容包生成（标题/卖点/详情/FAQ）
    → 人工审核（通过/退回） → 生成上架草稿 → 发布到淘宝
```

### 8.2 AI 选品评分（SelectionScoring）

**输入：**
- 1688 商品信息：title / price / min_num / desc / desc_img / sku
- 可选淘宝竞品对标：title / price / props / skus
- 业务参数：目标毛利率、客单价带、类目偏好、售后政策、发货时效

**输出：**
- summary：一句话总结
- selling_points[]：5 条卖点
- risk_flags[]：侵权/敏感/易破损/尺码争议/物流限制
- cost_model：成本拆分（可先估算）
- score：0-100 + 理由
- recommended_fulfillment_mode：dropship / stock / hybrid
- stock_advice：是否建议备货、建议备货量区间

**新增实体：**

| 表名 | 字段 | 说明 |
|------|------|------|
| `taolink_selection_score` | `source_offer_id`, `score`(0-100), `summary`, `selling_points_json`, `risk_flags_json`, `cost_model_json`, `recommended_mode`, `stock_advice_json`, `ai_run_id`, `created_at` | AI 选品评分记录 |
| `taolink_content_pack` | `source_offer_id`, `product_id`(可选), `titles_json`(多版本), `bullet_points_json`, `detail_outline_json`, `sku_naming_json`, `compliance_report_json`, `status`(`draft`/`reviewing`/`approved`/`rejected`), `review_comment`, `version` | 上架内容包 |

### 8.3 上架草稿与发布

- 内容包审核通过 → 生成淘宝上架草稿
- 草稿包含：标题、卖点、规格、详情结构、图片任务
- 发布方式：
  - 方案 A（当前）：人工在千牛发布，系统提供内容包
  - 方案 B（远期）：对接淘宝官方发布接口

### 8.4 开发阶段

| 阶段 | 内容 | 优先级 |
|------|------|--------|
| Phase 1（MVP） | 选品评分（规则引擎为主，AI 为辅）+ 内容包编辑 | 中 |
| Phase 2 | AI 摘要生成（接入 airag 模块）+ 合规检查（敏感词库） | 中 |
| Phase 3 | 上架内容包 + 审核流 | 低 |
| Phase 4 | 淘宝发布接口对接 | 低 |

---

## 9. 接口变更

### 9.1 新增接口

| 方法 | 路径 | 说明 | 权限key |
|------|------|------|---------|
| GET | `/taolink/shop/list` | 用户店铺列表 | `taolink:shop:list` |
| POST | `/taolink/shop/bind` | 绑定店铺（发起OAuth） | `taolink:shop:bind` |
| POST | `/taolink/shop/unbind` | 解绑店铺 | `taolink:shop:unbind` |
| GET | `/taolink/shop/{id}/detail` | 店铺详情 | `taolink:shop:detail` |
| POST | `/taolink/shop/{id}/reauthorize` | 重新授权（续期） | `taolink:shop:reauth` |
| POST | `/taolink/search` | 搜索1688/淘宝商品（带缓存） | `taolink:search:query` |
| GET | `/taolink/search/cache/count` | 查看缓存统计 | `taolink:search:stats` |
| DELETE | `/taolink/search/cache/clear` | 清搜索缓存 | `taolink:search:cache:clear` |
| GET | `/taolink/monitor/{shopId}/overview` | 店铺监控概览（图表数据） | `taolink:monitor:view` |
| GET | `/taolink/monitor/{shopId}/trend` | 店铺监控趋势数据 | `taolink:monitor:view` |
| GET | `/taolink/monitor/{shopId}/rankings` | 店铺商品排行榜 | `taolink:monitor:view` |
| POST | `/taolink/monitor/{shopId}/refresh` | 手动刷新监控数据 | `taolink:monitor:refresh` |

#### 库存分析与预警

| 方法 | 路径 | 说明 | 权限key |
|------|------|------|---------|
| GET | `/taolink/inventory/analysis/overview` | 库存分析总览 | `taolink:inventory:view` |
| GET | `/taolink/inventory/analysis/metrics` | 库存健康指标（周转率/呆滞品/价值） | `taolink:inventory:view` |
| GET | `/taolink/inventory/alerts` | 预警列表（低库存/积压） | `taolink:inventory:view` |
| POST | `/taolink/inventory/alert/{id}/resolve` | 标记预警已解决 | `taolink:inventory:edit` |
| PUT | `/taolink/inventory/{id}/threshold` | 设置SKU预警阈值 | `taolink:inventory:edit` |

#### 代发采购与结算

| 方法 | 路径 | 说明 | 权限key |
|------|------|------|---------|
| POST | `/taolink/purchase/{purchaseLineId}/fillTracking` | 代发订单发货回填（物流+运单号） | `taolink:purchase:edit` |
| GET | `/taolink/settlement/list` | 结算记录列表 | `taolink:settlement:view` |
| GET | `/taolink/settlement/monthly` | 月度对账单（按供应商汇总） | `taolink:settlement:view` |
| POST | `/taolink/settlement/{id}/confirm` | 确认结算金额 | `taolink:settlement:edit` |

#### 平台全局看板

| 方法 | 路径 | 说明 | 权限key |
|------|------|------|---------|
| GET | `/taolink/dashboard/global/overview` | 全局总览指标 | `taolink:dashboard:view` |
| GET | `/taolink/dashboard/global/shop-ranking` | 店铺排行 | `taolink:dashboard:view` |
| GET | `/taolink/dashboard/global/inventory-health` | 全局库存健康度 | `taolink:dashboard:view` |
| GET | `/taolink/dashboard/global/alerts` | 全局预警汇总 | `taolink:dashboard:view` |
| GET | `/taolink/dashboard/global/trend` | 全局趋势（近N天） | `taolink:dashboard:view` |

### 9.2 现有接口变更

所有 `/taolink/product/*`、`/taolink/order/*`、`/taolink/purchase/*` 接口的 list 查询自动加入 `shop_id` 过滤条件（通过租户/店铺隔离拦截器）。

**库存接口不加 `shop_id` 过滤** —— 库存是全局共享的，按 `tenant_id` 隔离即可。所有 `/taolink/inventory/*` 接口返回租户级全部仓库/SKU 的库存数据。

---

## 7. 前端页面架构（SaaS 扩展）

### 7.1 新增页面

| 页面路径 | 说明 |
|----------|------|
| `/taolink/shop` | 商铺管理：列表、绑定、解绑、授权状态管理 |
| `/taolink/product` | 商品管理：列表（上下架状态）、新增/编辑、详情 |
| `/taolink/order` | 订单管理：列表（状态Tabs）、详情、履约分配、发货回填 |
| `/taolink/inventory` | 全局库存台账：SKU维度汇总、出入库流水 |
| `/taolink/inventory-analysis` | **库存分析看板**：预警列表、库存健康指标、周转率图表 |
| `/taolink/dashboard` | **全局分析看板**：跨店商品/订单汇总、全局库存健康度、预警总览 |
| `/taolink/search` | 搜索中心：统一搜索、缓存统计、商品详情 |
| `/taolink/monitor/{shopId}` | 店铺监控：概览卡片、趋势图、排行榜、告警 |
| `/taolink/settlement` | 结算管理：结算记录列表、月度对账单 |

### 7.2 现有页面变更

| 页面 | 变更内容 |
|------|----------|
| 所有商品/订单/采购列表 | 增加"店铺"筛选下拉框（多店铺用户），默认显示当前选中店铺的数据 |
| 全局导航栏 | 增加店铺切换器，切换后所有页面自动刷新为该店铺数据 |
| 商品列表 | 增加"上架/下架"状态筛选和操作按钮 |
| 订单列表 | 增强"预留库存"和"扣减库存"操作展示，增加"代发结算"链接 |

---

## 8. 非功能性需求

| 指标 | 要求 |
|------|------|
| 租户隔离 | tenant_id 不可越权，任何绕过租户隔离的查询视为安全漏洞 |
| 库存隔离 | 库存按租户级共享，但不可跨租户查看。卖家（tenant_user）仅可查看，不可修改库存数字 |
| 搜索缓存命中率 | 目标 > 40%（常见搜索条件重复率高） |
| Onebound API 配额保护 | 单日单租户调用上限可配置，超限告警 |
| 监控数据延迟 | 快照数据最大延迟 2 小时（每日任务凌晨执行） |
| 预警响应时间 | 库存预警触发后 < 5 分钟出现在预警列表中 |
| 店铺切换响应时间 | 切换店铺后列表加载 P95 < 1s |
