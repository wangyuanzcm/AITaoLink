package org.jeecg.modules.taolink.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.taolink.entity.TaolinkInventory;

import java.util.List;
import java.util.Map;

public interface ITaolinkInventoryService extends JeecgService<TaolinkInventory> {
    Result<String> reserve(String warehouseId, String productSkuId, int qty, String refType, String refId);

    Result<String> release(String warehouseId, String productSkuId, int qty, String refType, String refId);

    Result<String> adjustOnHand(String warehouseId, String productSkuId, int qty, String refType, String refId);

    Result<String> deduct(String warehouseId, String productSkuId, int qty, String refType, String refId);

    List<TaolinkInventory> listByProductSkuId(String productSkuId);

    Result<Map<String, Object>> getInventoryAnalysisOverview();

    Result<Map<String, Object>> getInventoryAnalysisMetrics();

    Result<String> updateThreshold(String id, Integer warningMin, Integer overstockDays);

    /**
     * 统计库存SKU数
     */
    long countInventoryItems();

    /**
     * 统计低库存SKU数
     */
    long countLowStockItems();

    /**
     * 统计积压库存SKU数
     */
    long countOverstockItems();

    /**
     * 计算库存健康度
     */
    double calculateInventoryHealthScore();

    /**
     * 计算库存周转率
     */
    double calculateInventoryTurnoverRate();

    /**
     * 计算库存价值
     */
    long calculateInventoryValue();

    /**
     * 获取各类库存状态统计
     */
    Map<String, Long> getInventoryStatusCounts();

    /**
     * 统计低库存预警
     */
    long countLowStockAlerts();

    /**
     * 统计积压库存预警
     */
    long countOverstockAlerts();

    /**
     * 获取预警详情列表
     */
    List<Map<String, Object>> getAlertDetails();
}

