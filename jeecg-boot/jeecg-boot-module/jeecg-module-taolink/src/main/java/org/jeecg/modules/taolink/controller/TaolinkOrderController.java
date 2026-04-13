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
import org.jeecg.modules.taolink.entity.TaolinkOrder;
import org.jeecg.modules.taolink.service.ITaolinkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Tag(name = "TaoLink-订单")
@RestController
@RequestMapping("/taolink/order")
public class TaolinkOrderController extends JeecgController<TaolinkOrder, ITaolinkOrderService> {
    @Autowired
    private ITaolinkOrderService taolinkOrderService;

    @Operation(summary = "分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<TaolinkOrder>> list(TaolinkOrder order,
                                           @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                           HttpServletRequest req) {
        QueryWrapper<TaolinkOrder> queryWrapper = QueryGenerator.initQueryWrapper(order, req.getParameterMap());
        queryWrapper.orderByDesc("pay_time");
        Page<TaolinkOrder> page = new Page<>(pageNo, pageSize);
        IPage<TaolinkOrder> pageList = taolinkOrderService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "添加")
    @PostMapping(value = "/add")
    @RequiresPermissions("taolink:order:add")
    public Result<String> add(@RequestBody TaolinkOrder order) {
        taolinkOrderService.save(order);
        return Result.OK("添加成功！");
    }

    @Operation(summary = "编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("taolink:order:edit")
    public Result<String> edit(@RequestBody TaolinkOrder order) {
        taolinkOrderService.updateById(order);
        return Result.OK("更新成功！");
    }

    @Operation(summary = "通过ID删除")
    @DeleteMapping(value = "/delete")
    @RequiresPermissions("taolink:order:delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        taolinkOrderService.removeById(id);
        return Result.OK("删除成功!");
    }

    @Operation(summary = "通过ID查询")
    @GetMapping(value = "/queryById")
    public Result<TaolinkOrder> queryById(@RequestParam(name = "id") String id) {
        TaolinkOrder entity = taolinkOrderService.getById(id);
        if (entity == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(entity);
    }

    public static class AssignFulfillmentRequest {
        private String orderId;
        private String fulfillmentType; // stock 或 dropship
        private String warehouseId; // 当 fulfillmentType 为 stock 时必填
        private String supplierId; // 当 fulfillmentType 为 dropship 时必填

        // Getters and Setters
        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getFulfillmentType() {
            return fulfillmentType;
        }

        public void setFulfillmentType(String fulfillmentType) {
            this.fulfillmentType = fulfillmentType;
        }

        public String getWarehouseId() {
            return warehouseId;
        }

        public void setWarehouseId(String warehouseId) {
            this.warehouseId = warehouseId;
        }

        public String getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(String supplierId) {
            this.supplierId = supplierId;
        }
    }

    @Operation(summary = "订单履约分配")
    @PostMapping(value = "/assignFulfillment")
    @RequiresPermissions("taolink:order:edit")
    public Result<String> assignFulfillment(@RequestBody AssignFulfillmentRequest req) {
        if (req.getOrderId() == null || req.getOrderId().isEmpty()) {
            return Result.error("订单ID不能为空");
        }
        if (req.getFulfillmentType() == null || (!"stock".equals(req.getFulfillmentType()) && !"dropship".equals(req.getFulfillmentType()))) {
            return Result.error("履约类型必须为 stock 或 dropship");
        }
        if ("stock".equals(req.getFulfillmentType()) && (req.getWarehouseId() == null || req.getWarehouseId().isEmpty())) {
            return Result.error("库存履约时仓库ID不能为空");
        }
        if ("dropship".equals(req.getFulfillmentType()) && (req.getSupplierId() == null || req.getSupplierId().isEmpty())) {
            return Result.error("代发履约时供应商ID不能为空");
        }
        
        TaolinkOrder order = taolinkOrderService.getById(req.getOrderId());
        if (order == null) {
            return Result.error("未找到对应订单");
        }
        
        // 这里可以添加履约分配的业务逻辑
        // 例如：更新订单的履约方式，为代发订单创建采购任务等
        
        return Result.OK("履约分配成功！");
    }
}

