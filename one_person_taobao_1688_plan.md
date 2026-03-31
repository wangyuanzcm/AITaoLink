# 1688 选品（混合：一键代发 + 备货）+ 淘宝单平台：一人公司系统方案

本文档沉淀当前讨论的落地方案：使用 onebound（1688/淘宝 item_get）做商品信息采集与选品素材获取，使用淘宝官方开放平台承载交易侧（订单/物流/售后/消息），并用 AI 负责“建议/草稿/分流”，由规则与流程控制最终动作。

## 1. 目标与边界

### 1.1 目标

- 每天自动产出可上架候选商品：含定价建议、卖点/详情草稿、风险提示、履约建议（代发/备货/混合）。
- 上架后自动同步订单并按策略履约：库存优先或代发兜底，自动跟踪物流与异常。
- 客服半自动：自动分流与话术草稿，重点工单人工接管。
- 形成闭环：成交/退货/差评/履约时效等数据反哺选品与备货决策。

### 1.2 边界与职责划分

- onebound（建议用于“商品信息侧”）
  - 1688 商品详情：https://open.onebound.cn/help/api/1688.item_get.html?sso_checked=1
  - 淘宝商品详情（竞品对标）：https://open.onebound.cn/help/api/taobao.item_get.html
- 淘宝官方开放平台（建议用于“交易侧”）
  - 订单、发货回传、物流轨迹、售后、消息、评价等。
- AI 不直接执行高风险动作
  - AI 输出建议/草稿/动作计划；退款、改价、发货等关键动作由规则引擎与人工抽检/审批策略控制。

## 2. 总体架构（建议三层）

### 2.1 数据采集层（Connectors）

- onebound：拉取 1688 货源详情、淘宝竞品详情。
- 淘宝开放平台：订单、物流、售后、消息等增量同步与回调订阅。
- 通用能力：签名、限流、重试、缓存、原始回包存档、幂等处理。

### 2.2 业务中台层（Domain Services）

- 统一内部模型：Product/SPU、SKU、SourceOffer（来源商品）、Listing（淘宝上架商品）、Order、Inventory、Purchase、Ticket。
- 规则引擎：禁限类目/敏感词、毛利红线、运费估算、售后补偿上限、发货时效约束、履约切换阈值。

### 2.3 AI 工作流层（Agents/Workflows）

- 选品：摘要、评分、风险点、备货建议、标题词根与差异化卖点。
- 上新：标题/卖点/详情结构生成，规格命名标准化，素材需求清单。
- 客服：意图识别、话术草稿、下一步动作建议、风险提示（不做直接自动退款）。

## 3. 混合履约：代发 + 备货如何统一

对每个内部 SKU 维护一份 `FulfillmentPolicy`（履约策略），将“淘宝端销售 SKU”与“1688 货源 SKU/供货方式”解耦。

### 3.1 履约模式

- dropship（一键代发）
  - 订单 → 映射到 1688 货源 SKU → 生成采购任务 → 供应商发货/你回填物流。
- stock（备货）
  - 入库后出单 → 直接扣减库存 → 打单发货 → 回传物流。
- hybrid（混合）
  - 库存充足走 stock；库存不足自动切换 dropship，避免断货。

### 3.2 策略字段建议

- mode：dropship/stock/hybrid
- preferred：默认优先模式（例如库存>0优先 stock）
- switch_rules：
  - `stock_min_threshold`：低于该阈值切代发
  - `max_ship_hours`：最晚发货时效
  - `risk_level_limit`：高售后风险 SKU 降低备货优先级

## 4. 统一数据模型（表结构建议）

以下为最小可用的数据结构，便于后续扩展。

### 4.1 主数据

- source_offers（货源/竞品源商品）
  - platform：1688/taobao
  - num_iid、detail_url、seller_nick、title、price、min_num、location
  - raw_json、fetched_at
- products（内部 SPU）
  - name、category_id、status（draft/active/blocked）
- product_skus（内部 SKU）
  - product_id、spec_json、status
- sku_bindings（内部 SKU 与来源 SKU 绑定）
  - product_sku_id、source_offer_id、source_sku_id、source_properties、cost_price、is_primary

### 4.2 淘宝上架与商品

- taobao_listings（你在淘宝的商品）
  - product_id、taobao_num_iid（发布后写入）、title、price_strategy_json
  - status（draft/published/offline）、publish_payload_json、published_at

### 4.3 订单与履约

- orders
  - platform_order_id、status、pay_time、receiver_snapshot_json（最小化/脱敏）、raw_json
- order_lines
  - order_id、taobao_sku_id、product_sku_id、qty、sale_price
  - fulfillment_mode（stock/dropship）、fulfillment_status
- shipments
  - order_id、carrier、tracking_no、shipped_at、status

### 4.4 库存（备货必须）

- warehouses：name、type（self/3pl）
- inventory：warehouse_id、product_sku_id、on_hand、reserved、updated_at
- inventory_movements：type（in/out/adjust/reserve/release）、qty、ref_type/ref_id、created_at

### 4.5 采购（代发/备货共用）

- purchases：mode（dropship/stock）、supplier_platform（1688）、status、expected_ship_at/arrive_at
- purchase_lines：source_offer_id、source_sku_id、qty、unit_cost、spec_snapshot_json

### 4.6 AI 与工单

- ai_runs：type、input_snapshot_json、output_json、model、created_at
- tickets：order_id（可空）、type、priority、status、conversation_snapshot_json、ai_suggestion_json

## 5. 接口接入：调用顺序与用途

### 5.1 1688 货源采集（onebound `1688.item_get`）

目的：获取上新所需素材与 SKU 结构，尤其是 `desc(HTML)`、`desc_img`、起批量、规格。

- 输入：1688 offer id（num_iid）
- 关键字段（常用）
  - item.title、item.price、item.min_num、item.desc、item.desc_img、item.pic_url、SKU/规格相关字段
- 落库：
  - source_offers(platform=1688) 保存 raw_json
  - 解析 SKU → 生成 sku_bindings 候选绑定
- 素材处理要点：
  - desc：保存原始 HTML，并做一次清洗提取（去无关追踪图等）
  - desc_img：作为详情图候选池

### 5.2 淘宝竞品采集（onebound `taobao.item_get`，推荐）

目的：竞品对标（价格带、标题词根、规格命名、图片结构）。

- 输入：淘宝商品 id（num_iid）
- 关键字段（常用）
  - item.title、item.price、item.orginal_price、item.props、item.item_imgs、item.prop_imgs、item.skus
- 落库：source_offers(platform=taobao) 保存 raw_json

### 5.3 淘宝交易侧（官方开放平台）

目的：订单/物流/售后/消息统一由官方承载，稳定合规。

- 推荐落地顺序
  - 订单列表与订单详情（定时 + 增量）
  - 发货回传（物流公司、单号）
  - 售后/退款查询（用于工单触发）
  - 消息/评价同步（用于客服闭环）

## 6. 业务流程（选品→上新→接单→履约→客服→复盘）

### 6.1 选品 SOP（每日自动 + 人工审核）

1. 候选池：关键词/类目/供应商池/榜单链接 → 得到一批 1688 num_iid
2. 拉详情：onebound `1688.item_get`
3. AI 摘要：核心卖点、适用人群、材质工艺、规格要点、注意事项
4. 规则打分：
  - 毛利红线：预计售价 -（成本+运费+平台费+退货损耗+推广预估）≥ 阈值
  - 发货时效：代发能否满足承诺
  - 售后风险：易破损/尺码争议/材质投诉等
  - 合规风险：敏感词/禁限类目/侵权品牌词
5. 输出：Top N → products(draft) + taobao_listings(draft) 内容草稿包

### 6.2 上新（半自动最稳）

- 产出上新内容包：
  - 标题：3-5 个版本（含词根方案）
  - 卖点：5 点
  - 规格：标准化命名（颜色/尺码/套餐）
  - 详情结构：场景-材质-工艺-尺寸-使用-售后
  - 风险提示：禁词/夸大/侵权点
- 发布方式：
  - 方案 A：你人工在千牛发布（系统提供内容包）
  - 方案 B：对接官方上架接口（需要更严格的合规与参数校验）

### 6.3 接单与履约分配（混合模式关键）

1. 同步订单后，为每条订单行匹配内部 SKU
2. 按 FulfillmentPolicy 决策：
  - `available_stock >= qty` → stock
  - 否则 → dropship
  - hybrid：按阈值与时效约束自动切换
3. 执行：
  - stock：reserve 库存 → 打单发货 → 回传物流 → 出库 movement
  - dropship：生成 purchase(dropship) → 汇总到供应商/规格 → 人工一键处理采购 → 回填物流

### 6.4 异常工单（解放客服时间）

触发示例：

- 付款后超过 T 小时未发货 → 催发工单
- 物流停滞超过 N 天 → 物流异常工单
- 买家发起退款/售后 → 售后工单
- 差评/低分评价出现 → 评价挽回工单

每个工单由 AI 产出：

- 话术草稿（多版本）
- 下一步动作建议（补发/退款/补偿/催物流）
- 风险提示（避免违规承诺、避免触发平台规则）

## 7. AI 工作流节点（输入/输出定义）

### 7.1 SelectionScoring

- 输入
  - 1688：title/price/min_num/desc/desc_img/sku
  - 可选淘宝竞品：title/price/props/skus
  - 业务参数：目标毛利率、客单价带、类目偏好、售后政策、发货时效
- 输出
  - summary：一句话总结
  - selling_points[]：5 条卖点
  - risk_flags[]：侵权/敏感/易破损/尺码争议/物流限制
  - cost_model：成本拆分（可先估算）
  - score：0-100 + 理由
  - recommended_fulfillment_mode：dropship/stock/hybrid
  - stock_advice：是否建议备货、建议备货量区间（简化预测）

### 7.2 ListingGenerate

- 输入：内部 Product + 1688 源信息 + 标题规范/禁词库
- 输出
  - titles[]：多标题版本
  - bullet_points[]：核心卖点
  - detail_outline：模块化详情结构
  - sku_naming_map：内部规格 → 淘宝展示名映射
  - image_tasks[]：需要补齐的图片任务（尺寸图/细节图/对比图）

### 7.3 CustomerServiceDraft

- 输入：订单状态、物流信息（如有）、商品 FAQ、买家消息
- 输出
  - intent：催发/退款/少件/质量/改地址/差评等
  - reply_drafts[]：多语气话术草稿
  - next_actions[]：下一步动作（催供应商/补发/登记备注等）
  - do_not_say[]：禁承诺点

## 8. 备货策略（适合一人公司：简单但有效）

### 8.1 ABC 分层

- A：高动销、低售后 → 优先备货
- B：中动销 → 小量备货 + 代发兜底
- C：低动销/高风险 → 只代发或不做

### 8.2 补货点（ROP）简化

- ROP = 日均销量 * 采购到货周期 + 安全库存
- 安全库存默认：
  - A：7 天
  - B：3 天
  - C：0 天

## 9. 每日看板（建议只盯这 8 个）

- 选品漏斗：候选数 → 通过审核数 → 上架数 → 出单数
- 毛利：订单毛利（估算/实算）
- 发货时效：24/48 小时发货达标率
- 退款率/退货率（按商品与供应商）
- 缺货率（stock 模式关键）
- 物流异常率（停滞/拒收/丢件）
- 客服压力：工单量、需要人工接管比例
- 供应商表现：交付时效、质量投诉、补发/赔付次数

## 10. 落地路线（按“先跑通闭环→再压缩人力”）

### 阶段 1：先跑通赚钱闭环

- 1688 采集（`1688.item_get`）→ 选品评分 → 上新内容包生成（可先人工上架）
- 淘宝订单同步（官方）→ 履约分配（stock/dropship）→ 发货回传
- 客服：话术草稿 + 工单分流（不做全自动售后动作）

### 阶段 2：把人工压到最低

- 采购任务自动汇总（按供应商/规格）
- 库存自动预警与补货建议
- 评价/售后闭环：原因聚类 → 反哺选品与详情优化

