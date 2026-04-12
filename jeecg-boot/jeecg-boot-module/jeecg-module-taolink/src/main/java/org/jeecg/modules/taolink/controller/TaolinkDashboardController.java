package org.jeecg.modules.taolink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.taolink.service.ITaolinkOrderService;
import org.jeecg.modules.taolink.service.ITaolinkProductService;
import org.jeecg.modules.taolink.service.ITaolinkInventoryService;
import org.jeecg.modules.taolink.service.ITaolinkShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局分析看板控制器
 * 提供跨店铺的全局数据统计和分析接口
 */
@Slf4j
@Tag(name = "TaoLink-全局分析看板")
@RestController
@RequestMapping("/taolink/dashboard")
public class TaolinkDashboardController {

    @Autowired
    private ITaolinkShopService shopService;

    @Autowired
    private ITaolinkProductService productService;

    @Autowired
    private ITaolinkOrderService orderService;

    @Autowired
    private ITaolinkInventoryService inventoryService;

    /**
     * 全局总览指标接口
     * 获取所有店铺的汇总数据
     */
    @Operation(summary = "全局总览指标")
    @GetMapping(value = "/global/overview")
    public Result<Map<String, Object>> getGlobalOverview() {
        try {
            Map<String, Object> overview = new HashMap<>();
            
            // 统计店铺数量
            long shopCount = shopService.count();
            overview.put("shopCount", shopCount);
            
            // 统计商品总数
            long productCount = productService.count();
            overview.put("productCount", productCount);
            
            // 统计在售商品数
            long listedProductCount = productService.countListedProducts();
            overview.put("listedProductCount", listedProductCount);
            
            // 统计当日订单数
            int todayOrderCount = orderService.countTodayOrders();
            overview.put("todayOrderCount", todayOrderCount);
            
            // 统计当日GMV
            int todayGmv = orderService.calculateTodayGmv();
            overview.put("todayGmv", todayGmv);
            
            // 统计库存SKU数
            long inventorySkuCount = inventoryService.countInventoryItems();
            overview.put("inventorySkuCount", inventorySkuCount);
            
            // 统计低库存SKU数
            long lowStockSkuCount = inventoryService.countLowStockItems();
            overview.put("lowStockSkuCount", lowStockSkuCount);
            
            // 统计积压库存SKU数
            long overstockSkuCount = inventoryService.countOverstockItems();
            overview.put("overstockSkuCount", overstockSkuCount);
            
            return Result.OK(overview);
        } catch (Exception e) {
            log.error("获取全局总览指标失败", e);
            return Result.error("获取全局总览指标失败");
        }
    }

    /**
     * 店铺排行接口
     * 按订单量和GMV进行店铺排名
     */
    @Operation(summary = "店铺排行")
    @GetMapping(value = "/global/shop-ranking")
    public Result<List<Map<String, Object>>> getShopRanking() {
        try {
            List<Map<String, Object>> rankingList = shopService.getShopRanking();
            return Result.OK(rankingList);
        } catch (Exception e) {
            log.error("获取店铺排行失败", e);
            return Result.error("获取店铺排行失败");
        }
    }

    /**
     * 全局库存健康度接口
     * 提供库存健康状态的统计数据
     */
    @Operation(summary = "全局库存健康度")
    @GetMapping(value = "/global/inventory-health")
    public Result<Map<String, Object>> getInventoryHealth() {
        try {
            Map<String, Object> healthData = new HashMap<>();
            
            // 库存健康度计算
            double healthScore = inventoryService.calculateInventoryHealthScore();
            healthData.put("healthScore", healthScore);
            
            // 库存周转率
            double turnoverRate = inventoryService.calculateInventoryTurnoverRate();
            healthData.put("turnoverRate", turnoverRate);
            
            // 库存价值
            long inventoryValue = inventoryService.calculateInventoryValue();
            healthData.put("inventoryValue", inventoryValue);
            
            // 各类库存状态统计
            Map<String, Long> statusCounts = inventoryService.getInventoryStatusCounts();
            healthData.put("statusCounts", statusCounts);
            
            return Result.OK(healthData);
        } catch (Exception e) {
            log.error("获取库存健康度失败", e);
            return Result.error("获取库存健康度失败");
        }
    }

    /**
     * 全局预警汇总接口
     * 汇总所有店铺的预警信息
     */
    @Operation(summary = "全局预警汇总")
    @GetMapping(value = "/global/alerts")
    public Result<Map<String, Object>> getGlobalAlerts() {
        try {
            Map<String, Object> alertsData = new HashMap<>();
            
            // 低库存预警
            long lowStockAlerts = inventoryService.countLowStockAlerts();
            alertsData.put("lowStockAlerts", lowStockAlerts);
            
            // 积压库存预警
            long overstockAlerts = inventoryService.countOverstockAlerts();
            alertsData.put("overstockAlerts", overstockAlerts);
            
            // 订单异常预警
            long orderAlerts = orderService.countOrderAlerts();
            alertsData.put("orderAlerts", orderAlerts);
            
            // 退款异常预警
            long refundAlerts = orderService.countRefundAlerts();
            alertsData.put("refundAlerts", refundAlerts);
            
            // 预警详情列表
            List<Map<String, Object>> alertDetails = inventoryService.getAlertDetails();
            alertsData.put("alertDetails", alertDetails);
            
            return Result.OK(alertsData);
        } catch (Exception e) {
            log.error("获取全局预警汇总失败", e);
            return Result.error("获取全局预警汇总失败");
        }
    }

    /**
     * 全局趋势接口
     * 获取指定天数的趋势数据
     */
    @Operation(summary = "全局趋势")
    @GetMapping(value = "/global/trend")
    public Result<List<Map<String, Object>>> getGlobalTrend(
            @Parameter(description = "统计天数", example = "30")
            @RequestParam(name = "days", defaultValue = "30") Integer days) {
        try {
            List<Map<String, Object>> trendData = orderService.getGlobalTrend(days);
            return Result.OK(trendData);
        } catch (Exception e) {
            log.error("获取全局趋势失败", e);
            return Result.error("获取全局趋势失败");
        }
    }
}
