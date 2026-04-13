-- ========================================================
-- TaoLink SaaS v0.3.0 数据库迁移脚本
-- 创建日期: 2026-04-12
-- 说明: 创建新表 + 修改已有表结构
-- ========================================================

-- ======================== A. 创建新表 ========================

-- 1. 淘宝店铺绑定表
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

-- 2. 搜索缓存表
CREATE TABLE IF NOT EXISTS `taolink_search_cache` (
    `id` VARCHAR(36) NOT NULL COMMENT '主键',
    `create_by` VARCHAR(50) DEFAULT NULL,
    `create_by_org` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `sys_org_code` VARCHAR(64) DEFAULT NULL,
    `bpm_status` VARCHAR(32) DEFAULT NULL,

    `cache_key` VARCHAR(255) NOT NULL COMMENT '缓存key',
    `platform` VARCHAR(20) NOT NULL COMMENT '搜索平台',
    `search_params` TEXT DEFAULT NULL COMMENT '搜索参数快照',
    `result_json` MEDIUMTEXT DEFAULT NULL COMMENT '搜索结果',
    `hit_count` INT DEFAULT 0 COMMENT '命中次数',
    `expires_at` DATETIME DEFAULT NULL COMMENT '过期时间',

    PRIMARY KEY (`id`),
    INDEX `idx_cache_key` (`cache_key`),
    INDEX `idx_platform` (`platform`),
    INDEX `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索缓存表';

-- 3. 店铺监控每日快照表
CREATE TABLE IF NOT EXISTS `taolink_monitor_daily_snapshot` (
    `id` VARCHAR(36) NOT NULL COMMENT '主键',
    `create_by` VARCHAR(50) DEFAULT NULL,
    `create_by_org` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `sys_org_code` VARCHAR(64) DEFAULT NULL,
    `bpm_status` VARCHAR(32) DEFAULT NULL,

    `shop_id` VARCHAR(36) DEFAULT NULL COMMENT '店铺ID',
    `snapshot_date` DATE NOT NULL COMMENT '快照日期',
    `product_count` INT DEFAULT 0 COMMENT '商品总数',
    `listed_count` INT DEFAULT 0 COMMENT '在售商品数',
    `new_listed_count` INT DEFAULT 0 COMMENT '当日新增上架数',
    `new_delisted_count` INT DEFAULT 0 COMMENT '当日下架数',
    `order_count` INT DEFAULT 0 COMMENT '当日订单数',
    `order_amount` INT DEFAULT 0 COMMENT '当日GMV（分）',
    `refund_count` INT DEFAULT 0 COMMENT '当日退款数',
    `inventory_item_count` INT DEFAULT 0 COMMENT '库存有货SKU数',
    `snapshot_json` TEXT DEFAULT NULL COMMENT '附加数据',

    PRIMARY KEY (`id`),
    INDEX `idx_shop_date` (`shop_id`, `snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺监控每日快照表';

-- 4. 库存预警记录表
CREATE TABLE IF NOT EXISTS `taolink_inventory_alert` (
    `id` VARCHAR(36) NOT NULL COMMENT '主键',
    `create_by` VARCHAR(50) DEFAULT NULL,
    `create_by_org` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `sys_org_code` VARCHAR(64) DEFAULT NULL,
    `bpm_status` VARCHAR(32) DEFAULT NULL,

    `product_sku_id` VARCHAR(36) NOT NULL COMMENT '内部SKU ID',
    `alert_type` VARCHAR(20) NOT NULL COMMENT 'low_stock / overstock',
    `current_on_hand` INT DEFAULT 0 COMMENT '告警时在库数量',
    `current_available` INT DEFAULT 0 COMMENT '告警时可用数量',
    `threshold` INT DEFAULT 0 COMMENT '触发的阈值',
    `status` VARCHAR(20) DEFAULT 'open' COMMENT 'open/acknowledged/resolved',
    `resolved_at` DATETIME DEFAULT NULL COMMENT '告警解决时间',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',

    PRIMARY KEY (`id`),
    INDEX `idx_sku_id` (`product_sku_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警记录表';

-- 5. 代发结算流水表
CREATE TABLE IF NOT EXISTS `taolink_settlement_record` (
    `id` VARCHAR(36) NOT NULL COMMENT '主键',
    `create_by` VARCHAR(50) DEFAULT NULL,
    `create_by_org` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(50) DEFAULT NULL,
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `sys_org_code` VARCHAR(64) DEFAULT NULL,
    `bpm_status` VARCHAR(32) DEFAULT NULL,

    `purchase_line_id` VARCHAR(36) NOT NULL COMMENT '关联采购单行ID',
    `order_id` VARCHAR(36) DEFAULT NULL COMMENT '关联订单ID',
    `shop_id` VARCHAR(36) DEFAULT NULL COMMENT '关联店铺ID',
    `settle_type` VARCHAR(20) DEFAULT 'purchase_cost' COMMENT 'purchase_cost / freight',
    `amount` INT DEFAULT 0 COMMENT '金额（分）',
    `currency` VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',
    `settle_status` VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/settled/void',
    `supplier_name` VARCHAR(200) DEFAULT NULL COMMENT '供应商名称',
    `settled_at` DATETIME DEFAULT NULL COMMENT '实际结算时间',
    `actual_amount` INT DEFAULT NULL COMMENT '实际结算金额（分）',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',

    PRIMARY KEY (`id`),
    INDEX `idx_purchase_line_id` (`purchase_line_id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_shop_id` (`shop_id`),
    INDEX `idx_settle_status` (`settle_status`),
    INDEX `idx_supplier_settle` (`supplier_name`, `settle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代发结算流水表';


-- ======================== B. 修改已有表结构 ========================

-- 1. taolink_product：新增店铺关联 + 上下架字段
ALTER TABLE `taolink_product`
    ADD COLUMN `shop_id` VARCHAR(36) DEFAULT NULL COMMENT '所属店铺ID' AFTER `name`,
    ADD COLUMN `listing_status` VARCHAR(20) DEFAULT 'draft' COMMENT '上下架状态: listed/delisted/draft' AFTER `status`,
    ADD COLUMN `listed_at` DATETIME DEFAULT NULL COMMENT '上架时间' AFTER `listing_status`,
    ADD COLUMN `delisted_at` DATETIME DEFAULT NULL COMMENT '下架时间' AFTER `listed_at`,
    ADD INDEX `idx_shop_id` (`shop_id`);

-- 2. taolink_order：新增店铺关联
ALTER TABLE `taolink_order`
    ADD COLUMN `shop_id` VARCHAR(36) DEFAULT NULL COMMENT '关联店铺ID' AFTER `id`,
    ADD INDEX `idx_shop_id` (`shop_id`);

-- 3. taolink_purchase：新增店铺关联
ALTER TABLE `taolink_purchase`
    ADD COLUMN `shop_id` VARCHAR(36) DEFAULT NULL COMMENT '关联店铺ID' AFTER `id`,
    ADD INDEX `idx_shop_id` (`shop_id`);

-- 4. taolink_purchase_line：新增发货/成本字段
ALTER TABLE `taolink_purchase_line`
    ADD COLUMN `shop_id` VARCHAR(36) DEFAULT NULL COMMENT '关联店铺ID' AFTER `purchase_id`,
    ADD COLUMN `source_order_id` VARCHAR(100) DEFAULT NULL COMMENT '1688采购订单号' AFTER `spec_snapshot_json`,
    ADD COLUMN `source_tracking_company` VARCHAR(100) DEFAULT NULL COMMENT '供应商发货物流公司' AFTER `source_order_id`,
    ADD COLUMN `source_tracking_no` VARCHAR(100) DEFAULT NULL COMMENT '供应商发货运单号' AFTER `source_tracking_company`,
    ADD COLUMN `shipped_at` DATETIME DEFAULT NULL COMMENT '供应商发货时间' AFTER `source_tracking_no`,
    ADD COLUMN `freight_cost` INT DEFAULT 0 COMMENT '运费（分）' AFTER `shipped_at`,
    ADD COLUMN `total_cost` INT DEFAULT 0 COMMENT '总成本（分）' AFTER `freight_cost`,
    ADD INDEX `idx_shop_id` (`shop_id`);

-- 5. taolink_source_offer：新增店铺关联
ALTER TABLE `taolink_source_offer`
    ADD COLUMN `shop_id` VARCHAR(36) DEFAULT NULL COMMENT '关联店铺ID（可选，1688货源可跨店共享）' AFTER `id`,
    ADD INDEX `idx_shop_id` (`shop_id`);

-- 6. taolink_sku_binding：新增店铺关联
ALTER TABLE `taolink_sku_binding`
    ADD COLUMN `shop_id` VARCHAR(36) DEFAULT NULL COMMENT '关联店铺ID' AFTER `id`,
    ADD INDEX `idx_shop_id` (`shop_id`);

-- 7. taolink_ticket：新增店铺关联
ALTER TABLE `taolink_ticket`
    ADD COLUMN `shop_id` VARCHAR(36) DEFAULT NULL COMMENT '关联店铺ID' AFTER `id`,
    ADD INDEX `idx_shop_id` (`shop_id`);

-- 8. taolink_inventory：新增库存预警阈值（不加shop_id，全局共享）
ALTER TABLE `taolink_inventory`
    ADD COLUMN `warning_min` INT DEFAULT 0 COMMENT '低库存预警阈值（>0则覆盖全局默认）' AFTER `reserved`,
    ADD COLUMN `overstock_days` INT DEFAULT 30 COMMENT '积压天数阈值（>0则覆盖全局默认）' AFTER `warning_min`;
