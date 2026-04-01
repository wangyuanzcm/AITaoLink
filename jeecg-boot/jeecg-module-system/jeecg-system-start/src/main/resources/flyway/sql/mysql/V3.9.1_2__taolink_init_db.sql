CREATE TABLE IF NOT EXISTS taolink_source_offer (
  id varchar(32) NOT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  platform varchar(20) NOT NULL,
  num_iid varchar(64) NOT NULL,
  title varchar(255) DEFAULT NULL,
  detail_url varchar(512) DEFAULT NULL,
  seller_nick varchar(128) DEFAULT NULL,
  price_min int DEFAULT NULL,
  price_max int DEFAULT NULL,
  min_num int DEFAULT NULL,
  location varchar(128) DEFAULT NULL,
  risk_level varchar(20) DEFAULT NULL,
  raw_json longtext,
  fetched_at datetime DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_source_offer_platform_num_iid (platform, num_iid),
  KEY idx_source_offer_fetched_at (fetched_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS taolink_product (
  id varchar(32) NOT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  name varchar(255) NOT NULL,
  category_id varchar(32) DEFAULT NULL,
  status varchar(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_product_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS taolink_product_sku (
  id varchar(32) NOT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  product_id varchar(32) NOT NULL,
  spec_json longtext,
  status varchar(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_product_sku_product_id (product_id),
  KEY idx_product_sku_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS taolink_sku_binding (
  id varchar(32) NOT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  product_sku_id varchar(32) NOT NULL,
  source_offer_id varchar(32) NOT NULL,
  source_sku_id varchar(64) DEFAULT NULL,
  source_properties varchar(255) DEFAULT NULL,
  cost_price int DEFAULT NULL,
  is_primary tinyint(1) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sku_binding (product_sku_id, source_offer_id, source_sku_id),
  KEY idx_sku_binding_offer (source_offer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS taolink_warehouse (
  id varchar(32) NOT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  name varchar(128) NOT NULL,
  type varchar(20) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_warehouse_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS taolink_inventory (
  id varchar(32) NOT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  warehouse_id varchar(32) NOT NULL,
  product_sku_id varchar(32) NOT NULL,
  on_hand int DEFAULT NULL,
  reserved int DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_inventory_wh_sku (warehouse_id, product_sku_id),
  KEY idx_inventory_sku (product_sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS taolink_inventory_movement (
  id varchar(32) NOT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  warehouse_id varchar(32) NOT NULL,
  product_sku_id varchar(32) NOT NULL,
  type varchar(20) NOT NULL,
  qty int NOT NULL,
  ref_type varchar(20) DEFAULT NULL,
  ref_id varchar(32) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_inv_move_wh_sku (warehouse_id, product_sku_id),
  KEY idx_inv_move_ref (ref_type, ref_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS taolink_order (
  id varchar(32) NOT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  platform_order_id varchar(64) NOT NULL,
  status varchar(30) DEFAULT NULL,
  pay_time datetime DEFAULT NULL,
  receiver_snapshot_json longtext,
  raw_json longtext,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_platform_order_id (platform_order_id),
  KEY idx_order_status (status),
  KEY idx_order_pay_time (pay_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS taolink_order_line (
  id varchar(32) NOT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  order_id varchar(32) NOT NULL,
  product_sku_id varchar(32) DEFAULT NULL,
  qty int NOT NULL,
  sale_price int DEFAULT NULL,
  fulfillment_mode varchar(20) DEFAULT NULL,
  fulfillment_status varchar(30) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_order_line_order_id (order_id),
  KEY idx_order_line_sku (product_sku_id),
  KEY idx_order_line_fulfillment (fulfillment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS taolink_purchase (
  id varchar(32) NOT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  mode varchar(20) NOT NULL,
  supplier_platform varchar(20) DEFAULT NULL,
  status varchar(30) DEFAULT NULL,
  expected_ship_at datetime DEFAULT NULL,
  expected_arrive_at datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_purchase_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS taolink_purchase_line (
  id varchar(32) NOT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  purchase_id varchar(32) NOT NULL,
  source_offer_id varchar(32) NOT NULL,
  source_sku_id varchar(64) DEFAULT NULL,
  qty int NOT NULL,
  unit_cost int DEFAULT NULL,
  spec_snapshot_json longtext,
  PRIMARY KEY (id),
  KEY idx_purchase_line_purchase_id (purchase_id),
  KEY idx_purchase_line_offer (source_offer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS taolink_ticket (
  id varchar(32) NOT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT NULL,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  order_id varchar(32) DEFAULT NULL,
  type varchar(30) NOT NULL,
  priority varchar(10) DEFAULT NULL,
  status varchar(20) DEFAULT NULL,
  conversation_snapshot_json longtext,
  ai_suggestion_json longtext,
  PRIMARY KEY (id),
  KEY idx_ticket_status (status),
  KEY idx_ticket_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

