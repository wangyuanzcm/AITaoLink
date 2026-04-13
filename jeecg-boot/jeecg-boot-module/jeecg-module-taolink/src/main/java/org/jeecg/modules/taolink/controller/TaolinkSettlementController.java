package org.jeecg.modules.taolink.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.taolink.entity.TaolinkSettlementRecord;
import org.jeecg.modules.taolink.service.ITaolinkSettlementRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "TaoLink-结算")
@RestController
@RequestMapping("/taolink/settlement")
public class TaolinkSettlementController extends JeecgController<TaolinkSettlementRecord, ITaolinkSettlementRecordService> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TaolinkSettlementController.class);
    @Autowired
    private ITaolinkSettlementRecordService taolinkSettlementRecordService;

    @Operation(summary = "分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<TaolinkSettlementRecord>> list(TaolinkSettlementRecord record,
                                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                        HttpServletRequest req) {
        QueryWrapper<TaolinkSettlementRecord> queryWrapper = QueryGenerator.initQueryWrapper(record, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<TaolinkSettlementRecord> page = new Page<>(pageNo, pageSize);
        IPage<TaolinkSettlementRecord> pageList = taolinkSettlementRecordService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "添加")
    @PostMapping(value = "/add")
    @RequiresPermissions("taolink:settlement:add")
    public Result<String> add(@RequestBody TaolinkSettlementRecord record) {
        taolinkSettlementRecordService.save(record);
        return Result.OK("添加成功！");
    }

    @Operation(summary = "编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("taolink:settlement:edit")
    public Result<String> edit(@RequestBody TaolinkSettlementRecord record) {
        taolinkSettlementRecordService.updateById(record);
        return Result.OK("更新成功！");
    }

    @Operation(summary = "通过ID删除")
    @DeleteMapping(value = "/delete")
    @RequiresPermissions("taolink:settlement:delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        taolinkSettlementRecordService.removeById(id);
        return Result.OK("删除成功!");
    }

    @Operation(summary = "通过ID查询")
    @GetMapping(value = "/queryById")
    public Result<TaolinkSettlementRecord> queryById(@RequestParam(name = "id") String id) {
        TaolinkSettlementRecord entity = taolinkSettlementRecordService.getById(id);
        if (entity == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(entity);
    }

    @Operation(summary = "月度结算汇总")
    @GetMapping(value = "/monthly")
    public Result<List<Map<String, Object>>> getMonthlySummary(
            @RequestParam(name = "supplier", required = false) String supplier,
            @RequestParam(name = "month", required = false) String month) {
        try {
            QueryWrapper<TaolinkSettlementRecord> query = new QueryWrapper<>();

            if (supplier != null && !supplier.isEmpty()) {
                query.like("supplier_name", supplier);
            }

            List<TaolinkSettlementRecord> records = taolinkSettlementRecordService.list(query);

            // Group by supplier and aggregate
            Map<String, List<TaolinkSettlementRecord>> grouped = records.stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getSupplierName() != null ? r.getSupplierName() : "未知供应商"
                    ));

            List<Map<String, Object>> summary = new ArrayList<>();
            for (Map.Entry<String, List<TaolinkSettlementRecord>> entry : grouped.entrySet()) {
                Map<String, Object> map = new HashMap<>();
                map.put("supplierName", entry.getKey());
                List<TaolinkSettlementRecord> supplierRecords = entry.getValue();
                map.put("totalCount", supplierRecords.size());

                long pendingCount = supplierRecords.stream()
                        .filter(r -> "pending".equals(r.getStatus()))
                        .count();
                long settledCount = supplierRecords.stream()
                        .filter(r -> "settled".equals(r.getStatus()))
                        .count();
                map.put("pendingCount", pendingCount);
                map.put("settledCount", settledCount);

                int totalAmount = supplierRecords.stream()
                        .mapToInt(r -> r.getAmount() != null ? r.getAmount().intValue() : 0)
                        .sum();
                map.put("totalAmount", totalAmount);

                int settledAmount = supplierRecords.stream()
                        .filter(r -> r.getAmount() != null)
                        .mapToInt(r -> r.getAmount().intValue())
                        .sum();
                map.put("settledAmount", settledAmount);

                summary.add(map);
            }

            return Result.OK(summary);
        } catch (Exception e) {
            log.error("获取月度结算汇总失败", e);
            return Result.error("获取月度结算汇总失败: " + e.getMessage());
        }
    }
}
