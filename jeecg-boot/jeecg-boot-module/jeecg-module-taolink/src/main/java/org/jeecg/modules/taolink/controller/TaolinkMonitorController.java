package org.jeecg.modules.taolink.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.taolink.entity.TaolinkMonitorDailySnapshot;
import org.jeecg.modules.taolink.service.ITaolinkMonitorDailySnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "TaoLink-监控")
@RestController
@RequestMapping("/taolink/monitor")
public class TaolinkMonitorController extends JeecgController<TaolinkMonitorDailySnapshot, ITaolinkMonitorDailySnapshotService> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TaolinkMonitorController.class);
    @Autowired
    private ITaolinkMonitorDailySnapshotService monitorDailySnapshotService;

    @Operation(summary = "店铺每日快照概览")
    @GetMapping(value = "/{shopId}/overview")
    public Result<Map<String, Object>> getShopOverview(@PathVariable String shopId) {
        try {
            // Get the latest snapshot for the shop
            LambdaQueryWrapper<TaolinkMonitorDailySnapshot> query = new LambdaQueryWrapper<>();
            query.eq(TaolinkMonitorDailySnapshot::getShopId, shopId);
            query.orderByDesc(TaolinkMonitorDailySnapshot::getSnapshotDate);
            query.last("LIMIT 1");
            TaolinkMonitorDailySnapshot latest = monitorDailySnapshotService.getOne(query);

            if (latest == null) {
                return Result.error("未找到该店铺的监控数据");
            }

            Map<String, Object> overview = new HashMap<>();
            overview.put("snapshotDate", latest.getSnapshotDate());
            overview.put("productCount", latest.getProductCount());
            overview.put("listedCount", latest.getListedCount());
            overview.put("orderCount", latest.getOrderCount());
            overview.put("orderAmount", latest.getOrderAmount());
            overview.put("refundCount", latest.getRefundCount());
            overview.put("inventoryItemCount", latest.getInventoryItemCount());

            return Result.OK(overview);
        } catch (Exception e) {
            log.error("获取店铺概览失败, shopId: {}", shopId, e);
            return Result.error("获取店铺概览失败: " + e.getMessage());
        }
    }

    @Operation(summary = "店铺趋势数据")
    @GetMapping(value = "/{shopId}/trend")
    public Result<List<Map<String, Object>>> getShopTrend(@PathVariable String shopId,
                                                           @RequestParam(name = "days", defaultValue = "7") Integer days) {
        try {
            LambdaQueryWrapper<TaolinkMonitorDailySnapshot> query = new LambdaQueryWrapper<>();
            query.eq(TaolinkMonitorDailySnapshot::getShopId, shopId);
            query.orderByDesc(TaolinkMonitorDailySnapshot::getSnapshotDate);
            query.last("LIMIT " + days);

            List<TaolinkMonitorDailySnapshot> snapshots = monitorDailySnapshotService.list(query);
            Collections.reverse(snapshots);

            List<Map<String, Object>> trendData = snapshots.stream().map(snapshot -> {
                Map<String, Object> map = new HashMap<>();
                map.put("snapshotDate", snapshot.getSnapshotDate());
                map.put("productCount", snapshot.getProductCount());
                map.put("listedCount", snapshot.getListedCount());
                map.put("newListedCount", snapshot.getNewListedCount());
                map.put("newDelistedCount", snapshot.getNewDelistedCount());
                map.put("orderCount", snapshot.getOrderCount());
                map.put("orderAmount", snapshot.getOrderAmount());
                map.put("refundCount", snapshot.getRefundCount());
                return map;
            }).collect(Collectors.toList());

            return Result.OK(trendData);
        } catch (Exception e) {
            log.error("获取店铺趋势数据失败, shopId: {}, days: {}", shopId, days, e);
            return Result.error("获取趋势数据失败: " + e.getMessage());
        }
    }

    @Operation(summary = "店铺商品排行")
    @GetMapping(value = "/{shopId}/rankings")
    public Result<List<Map<String, Object>>> getShopRankings(@PathVariable String shopId) {
        // TODO: Implement ranking logic based on sales/orders
        List<Map<String, Object>> rankings = new ArrayList<>();
        return Result.OK(rankings);
    }
}
