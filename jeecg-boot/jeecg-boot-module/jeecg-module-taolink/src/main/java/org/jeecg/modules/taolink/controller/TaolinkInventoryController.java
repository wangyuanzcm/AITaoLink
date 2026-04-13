package org.jeecg.modules.taolink.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.taolink.entity.TaolinkInventory;
import org.jeecg.modules.taolink.service.ITaolinkInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Tag(name = "TaoLink-库存")
@RestController
@RequestMapping("/taolink/inventory")
public class TaolinkInventoryController extends JeecgController<TaolinkInventory, ITaolinkInventoryService> {
    @Autowired
    private ITaolinkInventoryService taolinkInventoryService;

    @Operation(summary = "分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<TaolinkInventory>> list(TaolinkInventory inventory,
                                               @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                               HttpServletRequest req) {
        QueryWrapper<TaolinkInventory> queryWrapper = QueryGenerator.initQueryWrapper(inventory, req.getParameterMap());
        Page<TaolinkInventory> page = new Page<>(pageNo, pageSize);
        IPage<TaolinkInventory> pageList = taolinkInventoryService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "添加")
    @PostMapping(value = "/add")
    @RequiresPermissions("taolink:inventory:add")
    public Result<String> add(@RequestBody TaolinkInventory inventory) {
        taolinkInventoryService.save(inventory);
        return Result.OK("添加成功！");
    }

    @Operation(summary = "编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("taolink:inventory:edit")
    public Result<String> edit(@RequestBody TaolinkInventory inventory) {
        taolinkInventoryService.updateById(inventory);
        return Result.OK("更新成功！");
    }

    @Operation(summary = "通过ID删除")
    @DeleteMapping(value = "/delete")
    @RequiresPermissions("taolink:inventory:delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        taolinkInventoryService.removeById(id);
        return Result.OK("删除成功!");
    }

    @Operation(summary = "通过ID查询")
    @GetMapping(value = "/queryById")
    public Result<TaolinkInventory> queryById(@RequestParam(name = "id") String id) {
        TaolinkInventory entity = taolinkInventoryService.getById(id);
        if (entity == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(entity);
    }

    @Operation(summary = "预占库存")
    @PostMapping(value = "/reserve")
    @RequiresPermissions("taolink:inventory:reserve")
    public Result<String> reserve(@RequestBody InventoryActionRequest req) {
        Result<String> check = req.check();
        if (!check.isSuccess()) {
            return check;
        }
        return taolinkInventoryService.reserve(req.getWarehouseId(), req.getProductSkuId(), req.getQty(), req.getRefType(), req.getRefId());
    }

    @Operation(summary = "释放预占")
    @PostMapping(value = "/release")
    @RequiresPermissions("taolink:inventory:release")
    public Result<String> release(@RequestBody InventoryActionRequest req) {
        Result<String> check = req.check();
        if (!check.isSuccess()) {
            return check;
        }
        return taolinkInventoryService.release(req.getWarehouseId(), req.getProductSkuId(), req.getQty(), req.getRefType(), req.getRefId());
    }

    @Operation(summary = "调整实库存")
    @PostMapping(value = "/adjustOnHand")
    @RequiresPermissions("taolink:inventory:adjust")
    public Result<String> adjustOnHand(@RequestBody InventoryActionRequest req) {
        Result<String> check = req.check();
        if (!check.isSuccess()) {
            return check;
        }
        return taolinkInventoryService.adjustOnHand(req.getWarehouseId(), req.getProductSkuId(), req.getQty(), req.getRefType(), req.getRefId());
    }

    public static class InventoryActionRequest {
        private String warehouseId;
        private String productSkuId;
        private Integer qty;
        private String refType;
        private String refId;

        // Getters and Setters
        public String getWarehouseId() {
            return warehouseId;
        }

        public void setWarehouseId(String warehouseId) {
            this.warehouseId = warehouseId;
        }

        public String getProductSkuId() {
            return productSkuId;
        }

        public void setProductSkuId(String productSkuId) {
            this.productSkuId = productSkuId;
        }

        public Integer getQty() {
            return qty;
        }

        public void setQty(Integer qty) {
            this.qty = qty;
        }

        public String getRefType() {
            return refType;
        }

        public void setRefType(String refType) {
            this.refType = refType;
        }

        public String getRefId() {
            return refId;
        }

        public void setRefId(String refId) {
            this.refId = refId;
        }

        public Result<String> check() {
            if (oConvertUtils.isEmpty(warehouseId)) {
                return Result.error("warehouseId不能为空");
            }
            if (oConvertUtils.isEmpty(productSkuId)) {
                return Result.error("productSkuId不能为空");
            }
            if (qty == null || qty == 0) {
                return Result.error("qty不能为空且不能为0");
            }
            return Result.OK();
        }
    }

    @Operation(summary = "库存分析总览")
    @GetMapping(value = "/analysis/overview")
    public Result<java.util.Map<String, Object>> inventoryAnalysisOverview() {
        return taolinkInventoryService.getInventoryAnalysisOverview();
    }

    @Operation(summary = "库存分析指标")
    @GetMapping(value = "/analysis/metrics")
    public Result<java.util.Map<String, Object>> inventoryAnalysisMetrics() {
        return taolinkInventoryService.getInventoryAnalysisMetrics();
    }

    public static class ThresholdRequest {
        private Integer warningMin;
        private Integer overstockDays;

        // Getters and Setters
        public Integer getWarningMin() {
            return warningMin;
        }

        public void setWarningMin(Integer warningMin) {
            this.warningMin = warningMin;
        }

        public Integer getOverstockDays() {
            return overstockDays;
        }

        public void setOverstockDays(Integer overstockDays) {
            this.overstockDays = overstockDays;
        }
    }

    @Operation(summary = "设置SKU预警阈值")
    @PutMapping(value = "/{id}/threshold")
    @RequiresPermissions("taolink:inventory:threshold")
    public Result<String> updateThreshold(@PathVariable String id, @RequestBody ThresholdRequest req) {
        return taolinkInventoryService.updateThreshold(id, req.getWarningMin(), req.getOverstockDays());
    }
}

