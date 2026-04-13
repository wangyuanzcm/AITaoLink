-- TaoLink SaaS 数据库迁移脚本 v2.0
-- 创建时间：2026-04-12
-- 包含：创建新表 + 修改现有表

-- =====================================
-- 1. 创建新表
-- =====================================

-- 1.1 店铺表
CREATE TABLE IF NOT EXISTS `taolink_shop` (
    `id` VARCHAR(36) NOT NULL COMMENT '主键',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_by_org` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `sys_org_code` VARCHAR(64) DEFAULT NULL COMMENT '部门编码',
    `tenant_id` VARCHAR(64) DEFAULT NULL COMMENT '租户ID',
    `bpm_status` VARCHAR(32) DEFAULT NULL,

    `taobao_seller_nick` VARCHAR(100) NOT NULL COMMENT '淘宝卖家昵称（店铺标识）',
    `api_session_key` VARCHAR(1000) DEFAULT NULL COMMENT '淘宝开放平台 session key',
    `api_expire_at` DATETIME DEFAULT NULL COMMENT 'API授权过期时间',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态: active/disabled/unbound',
    `bind_platforms` VARCHAR(200) DEFAULT NULL COMMENT '绑定平台列表，如 ["taobao","1688"]',
    `owner_id` VARCHAR(36) NOT NULL COMMENT '绑定人用户ID（关联 sys_user）',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `monitoring_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用监控',
    `monitoring_days` INT DEFAULT 7 COMMENT '监控回溯天数',

    PRIMARY KEY (`id`),
    INDEX `idx_owner_id` (`owner_id`),
    INDEX `idx_status` (`status`),
    UNIQUE INDEX `uk_tenant_tao_nick` (`tenant_id`, `taobao_seller_nick`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='淘宝店铺绑定表';

-- 1.x 采购明细表（基础表，若未初始化则创建；后续 ALTER 会补齐更多字段）
CREATE TABLE IF NOT EXISTS `taolink_purchase_line` (
    `id` VARCHAR(32) NOT NULL COMMENT '主键',
    `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
    `create_by_org` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `sys_org_code` VARCHAR(64) DEFAULT NULL COMMENT '部门编码',
    `tenant_id` VARCHAR(64) DEFAULT NULL COMMENT '租户ID',
    `bpm_status` VARCHAR(32) DEFAULT NULL,

    `purchase_id` VARCHAR(32) NOT NULL COMMENT '采购单ID',
    `source_offer_id` VARCHAR(32) NOT NULL COMMENT '来源商品ID',
    `source_sku_id` VARCHAR(64) DEFAULT NULL COMMENT '来源SKU ID',
    `qty` INT NOT NULL COMMENT '数量',
    `unit_cost` INT DEFAULT NULL COMMENT '单价成本（分）',
    `spec_snapshot_json` LONGTEXT DEFAULT NULL COMMENT '规格快照JSON（字符串）',

    PRIMARY KEY (`id`),
    INDEX `idx_purchase_line_purchase_id` (`purchase_id`),
    INDEX `idx_purchase_line_offer` (`source_offer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购单行';

-- 1.2 搜索缓存表
CREATE TABLE IF NOT EXISTS `taolink_search_cache` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键',
  `cache_key` VARCHAR(255) NOT NULL COMMENT '缓存键',
  `platform` VARCHAR(20) NOT NULL COMMENT '平台：1688/taobao',
  `search_params` JSON NOT NULL COMMENT '搜索参数快照',
  `result_json` JSON NOT NULL COMMENT '搜索结果',
  `hit_count` INT DEFAULT 0 COMMENT '命中次数',
  `expires_at` DATETIME NOT NULL COMMENT '过期时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_cache_key` (`cache_key`),
  INDEX `idx_platform` (`platform`),
  INDEX `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索缓存表';

-- 1.3 店铺监控每日快照表
CREATE TABLE IF NOT EXISTS `taolink_monitor_daily_snapshot` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键',
  `shop_id` VARCHAR(32) NOT NULL COMMENT '店铺ID',
  `snapshot_date` DATE NOT NULL COMMENT '快照日期',
  `product_count` INT DEFAULT 0 COMMENT '商品总数',
  `listed_count` INT DEFAULT 0 COMMENT '在售商品数',
  `delist_count` INT DEFAULT 0 COMMENT '下架商品数',
  `new_listed_count` INT DEFAULT 0 COMMENT '当日新增上架数',
  `new_delisted_count` INT DEFAULT 0 COMMENT '当日下架数',
  `order_count` INT DEFAULT 0 COMMENT '当日订单数',
  `order_amount` INT DEFAULT 0 COMMENT '当日GMV（分）',
  `refund_count` INT DEFAULT 0 COMMENT '当日退款数',
  `inventory_item_count` INT DEFAULT 0 COMMENT '库存有货SKU数',
  `snapshot_json` JSON DEFAULT NULL COMMENT '附加数据',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `idx_shop_date` (`shop_id`, `snapshot_date`),
  INDEX `idx_snapshot_date` (`snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺监控每日快照表';

-- 1.4 库存预警记录表
CREATE TABLE IF NOT EXISTS `taolink_inventory_alert` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键',
  `product_sku_id` VARCHAR(32) NOT NULL COMMENT '关联的SKU ID',
  `alert_type` VARCHAR(20) NOT NULL COMMENT '预警类型：low_stock/overstock',
  `current_on_hand` INT NOT NULL COMMENT '告警时在库数量',
  `current_available` INT NOT NULL COMMENT '告警时可用数量',
  `threshold` INT NOT NULL COMMENT '触发的阈值',
  `status` VARCHAR(20) NOT NULL COMMENT '状态：open/acknowledged/resolved',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '告警创建时间',
  `resolved_at` DATETIME DEFAULT NULL COMMENT '告警解决时间',
  PRIMARY KEY (`id`),
  INDEX `idx_product_sku_id` (`product_sku_id`),
  INDEX `idx_alert_type` (`alert_type`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警记录（低库存/积压）';

-- 1.5 代发结算流水表
CREATE TABLE IF NOT EXISTS `taolink_settlement_record` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键',
  `purchase_line_id` VARCHAR(32) NOT NULL COMMENT '关联的采购单行ID',
  `order_id` VARCHAR(32) NOT NULL COMMENT '关联的订单ID',
  `shop_id` VARCHAR(32) NOT NULL COMMENT '关联的店铺ID',
  `settle_type` VARCHAR(20) NOT NULL COMMENT '结算类型：purchase_cost/freight',
  `amount` INT NOT NULL COMMENT '金额（分）',
  `currency` VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',
  `settle_status` VARCHAR(20) NOT NULL COMMENT '结算状态：pending/settled/void',
  `supplier_name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
  `settled_at` DATETIME DEFAULT NULL COMMENT '实际结算时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_purchase_line_id` (`purchase_line_id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_shop_id` (`shop_id`),
  INDEX `idx_settle_status` (`settle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代发结算流水表';

-- =====================================
-- 2. 修改现有表 - 添加字段
-- =====================================

-- 2.1 商品表 - 添加店铺ID和上下架状态
ALTER TABLE `taolink_product` 
  ADD COLUMN `shop_id` VARCHAR(32) DEFAULT NULL COMMENT '所属店铺ID',
  ADD COLUMN `listing_status` VARCHAR(20) DEFAULT 'draft' COMMENT '上下架状态：listed/delisted/draft',
  ADD COLUMN `listed_at` DATETIME DEFAULT NULL COMMENT '上架时间',
  ADD COLUMN `delisted_at` DATETIME DEFAULT NULL COMMENT '下架时间',
  ADD INDEX `idx_shop_id` (`shop_id`),
  ADD INDEX `idx_listing_status` (`listing_status`);

-- 2.2 货源表 - 添加店铺ID（可选）
ALTER TABLE `taolink_source_offer` 
  ADD COLUMN `shop_id` VARCHAR(32) DEFAULT NULL COMMENT '所属店铺ID（可选）',
  ADD INDEX `idx_shop_id` (`shop_id`);

-- 2.3 订单表 - 添加店铺ID
ALTER TABLE `taolink_order` 
  ADD COLUMN `shop_id` VARCHAR(32) DEFAULT NULL COMMENT '所属店铺ID',
  ADD INDEX `idx_shop_id` (`shop_id`);

-- 2.4 采购表 - 添加店铺ID
ALTER TABLE `taolink_purchase` 
  ADD COLUMN `shop_id` VARCHAR(32) DEFAULT NULL COMMENT '所属店铺ID',
  ADD INDEX `idx_shop_id` (`shop_id`);

-- 2.5 采购明细表 - 添加店铺ID和发货信息
ALTER TABLE `taolink_purchase_line` 
  ADD COLUMN `shop_id` VARCHAR(32) DEFAULT NULL COMMENT '所属店铺ID',
  ADD COLUMN `source_order_id` VARCHAR(100) DEFAULT NULL COMMENT '1688采购订单号',
  ADD COLUMN `source_tracking_company` VARCHAR(100) DEFAULT NULL COMMENT '供应商发货的物流公司',
  ADD COLUMN `source_tracking_no` VARCHAR(100) DEFAULT NULL COMMENT '供应商发货的运单号',
  ADD COLUMN `shipped_at` DATETIME DEFAULT NULL COMMENT '供应商发货时间',
  ADD COLUMN `freight_cost` INT DEFAULT 0 COMMENT '运费（分）',
  ADD COLUMN `total_cost` INT DEFAULT 0 COMMENT '总成本（分）',
  ADD INDEX `idx_shop_id` (`shop_id`),
  ADD INDEX `idx_source_order_id` (`source_order_id`),
  ADD INDEX `idx_source_tracking_no` (`source_tracking_no`);

-- 2.6 SKU绑定表 - 添加店铺ID
ALTER TABLE `taolink_sku_binding` 
  ADD COLUMN `shop_id` VARCHAR(32) DEFAULT NULL COMMENT '所属店铺ID',
  ADD INDEX `idx_shop_id` (`shop_id`);

-- 2.7 工单表 - 添加店铺ID
ALTER TABLE `taolink_ticket` 
  ADD COLUMN `shop_id` VARCHAR(32) DEFAULT NULL COMMENT '所属店铺ID',
  ADD INDEX `idx_shop_id` (`shop_id`);

-- 2.8 库存表 - 添加预警阈值字段
ALTER TABLE `taolink_inventory` 
  ADD COLUMN `warning_min` INT DEFAULT 5 COMMENT '低库存预警阈值',
  ADD COLUMN `overstock_days` INT DEFAULT 30 COMMENT '积压天数阈值';

-- =====================================
-- 3. 数据初始化
-- =====================================

-- 3.1 初始化店铺状态枚举
INSERT INTO `sys_dict` (`id`, `dict_code`, `dict_name`, `dict_type`, `status`) 
VALUES 
('1', 'shop_status', '店铺状态', 'taolink', '1'),
('2', 'listing_status', '上下架状态', 'taolink', '1'),
('3', 'alert_type', '预警类型', 'taolink', '1'),
('4', 'settle_type', '结算类型', 'taolink', '1'),
('5', 'settle_status', '结算状态', 'taolink', '1')
ON DUPLICATE KEY UPDATE `status` = '1';

-- 3.2 初始化店铺状态字典项
INSERT INTO `sys_dict_item` (`id`, `dict_id`, `item_text`, `item_value`, `sort_order`) 
VALUES 
('1', '1', '活跃', 'active', 1),
('2', '1', '禁用', 'disabled', 2),
('3', '1', '未绑定', 'unbound', 3),
('4', '2', '已上架', 'listed', 1),
('5', '2', '已下架', 'delisted', 2),
('6', '2', '草稿', 'draft', 3),
('7', '3', '库存不足', 'low_stock', 1),
('8', '3', '库存积压', 'overstock', 2),
('9', '4', '采购成本', 'purchase_cost', 1),
('10', '4', '运费', 'freight', 2),
('11', '5', '待结算', 'pending', 1),
('12', '5', '已结算', 'settled', 2),
('13', '5', '作废', 'void', 3)
ON DUPLICATE KEY UPDATE `sort_order` = VALUES(`sort_order`);

-- 3.3 初始化菜单权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `code`, `url`, `menu_type`, `perms`, `sort_no`, `status`) 
VALUES 
('1', '0', 'TaoLink', 'taolink', '/taolink', '1', '', 10, '1'),
('2', '1', '商铺管理', 'taolink_shop', '/taolink/shop', '1', 'taolink:shop:list', 1, '1'),
('3', '1', '商品管理', 'taolink_product', '/taolink/product', '1', 'taolink:product:list', 2, '1'),
('4', '1', '订单管理', 'taolink_order', '/taolink/order', '1', 'taolink:order:list', 3, '1'),
('5', '1', '库存管理', 'taolink_inventory', '/taolink/inventory', '1', 'taolink:inventory:list', 4, '1'),
('6', '1', '库存分析', 'taolink_inventory_analysis', '/taolink/inventory-analysis', '1', 'taolink:inventory:view', 5, '1'),
('7', '1', '采购管理', 'taolink_purchase', '/taolink/purchase', '1', 'taolink:purchase:list', 6, '1'),
('8', '1', '结算管理', 'taolink_settlement', '/taolink/settlement', '1', 'taolink:settlement:list', 7, '1'),
('9', '1', '搜索中心', 'taolink_search', '/taolink/search', '1', 'taolink:search:query', 8, '1'),
('10', '1', '店铺监控', 'taolink_monitor', '/taolink/monitor', '1', 'taolink:monitor:view', 9, '1'),
('11', '1', '全局看板', 'taolink_dashboard', '/taolink/dashboard', '1', 'taolink:dashboard:view', 10, '1')
ON DUPLICATE KEY UPDATE `status` = '1';

-- 3.4 为管理员角色分配权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) 
SELECT '1', id FROM `sys_permission` WHERE `code` LIKE 'taolink%'
ON DUPLICATE KEY UPDATE `role_id` = '1';

-- 执行完成
SELECT 'TaoLink SaaS 数据库迁移脚本执行完成' AS message;
