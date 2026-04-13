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
import org.jeecg.modules.taolink.entity.TaolinkInventoryAlert;
import org.jeecg.modules.taolink.service.ITaolinkInventoryAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Tag(name = "TaoLink-库存预警")
@RestController
@RequestMapping("/taolink/inventory/alert")
public class TaolinkInventoryAlertController extends JeecgController<TaolinkInventoryAlert, ITaolinkInventoryAlertService> {
    @Autowired
    private ITaolinkInventoryAlertService taolinkInventoryAlertService;

    @Operation(summary = "分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<TaolinkInventoryAlert>> list(TaolinkInventoryAlert alert,
                                               @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                               HttpServletRequest req) {
        QueryWrapper<TaolinkInventoryAlert> queryWrapper = QueryGenerator.initQueryWrapper(alert, req.getParameterMap());
        Page<TaolinkInventoryAlert> page = new Page<>(pageNo, pageSize);
        IPage<TaolinkInventoryAlert> pageList = taolinkInventoryAlertService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "添加")
    @PostMapping(value = "/add")
    @RequiresPermissions("taolink:inventory:alert:add")
    public Result<String> add(@RequestBody TaolinkInventoryAlert alert) {
        taolinkInventoryAlertService.save(alert);
        return Result.OK("添加成功！");
    }

    @Operation(summary = "编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("taolink:inventory:alert:edit")
    public Result<String> edit(@RequestBody TaolinkInventoryAlert alert) {
        taolinkInventoryAlertService.updateById(alert);
        return Result.OK("更新成功！");
    }

    @Operation(summary = "通过ID删除")
    @DeleteMapping(value = "/delete")
    @RequiresPermissions("taolink:inventory:alert:delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        taolinkInventoryAlertService.removeById(id);
        return Result.OK("删除成功!");
    }

    @Operation(summary = "通过ID查询")
    @GetMapping(value = "/queryById")
    public Result<TaolinkInventoryAlert> queryById(@RequestParam(name = "id") String id) {
        TaolinkInventoryAlert entity = taolinkInventoryAlertService.getById(id);
        if (entity == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(entity);
    }

    public static class CreateAlertRequest {
        private String productSkuId;
        private String alertType;
        private Integer threshold;
        private Integer currentValue;
        private String message;

        // Getters and Setters
        public String getProductSkuId() {
            return productSkuId;
        }

        public void setProductSkuId(String productSkuId) {
            this.productSkuId = productSkuId;
        }

        public String getAlertType() {
            return alertType;
        }

        public void setAlertType(String alertType) {
            this.alertType = alertType;
        }

        public Integer getThreshold() {
            return threshold;
        }

        public void setThreshold(Integer threshold) {
            this.threshold = threshold;
        }

        public Integer getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(Integer currentValue) {
            this.currentValue = currentValue;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Result<String> check() {
            if (oConvertUtils.isEmpty(productSkuId)) {
                return Result.error("productSkuId不能为空");
            }
            if (oConvertUtils.isEmpty(alertType)) {
                return Result.error("alertType不能为空");
            }
            if (threshold == null) {
                return Result.error("threshold不能为空");
            }
            if (currentValue == null) {
                return Result.error("currentValue不能为空");
            }
            return Result.OK();
        }
    }

    @Operation(summary = "创建库存预警")
    @PostMapping(value = "/create")
    @RequiresPermissions("taolink:inventory:alert:create")
    public Result<String> createAlert(@RequestBody CreateAlertRequest req) {
        Result<String> check = req.check();
        if (!check.isSuccess()) {
            return check;
        }

        TaolinkInventoryAlert alert = new TaolinkInventoryAlert()
                .setProductSkuId(req.getProductSkuId())
                .setAlertType(req.getAlertType())
                .setThreshold(req.getThreshold())
                .setCurrentValue(req.getCurrentValue())
                .setMessage(req.getMessage());

        return taolinkInventoryAlertService.createAlert(alert);
    }

    public static class ResolveAlertRequest {
        private String handler;

        // Getters and Setters
        public String getHandler() {
            return handler;
        }

        public void setHandler(String handler) {
            this.handler = handler;
        }

        public Result<String> check() {
            if (oConvertUtils.isEmpty(handler)) {
                return Result.error("handler不能为空");
            }
            return Result.OK();
        }
    }

    @Operation(summary = "解决库存预警")
    @PutMapping(value = "/{id}/resolve")
    @RequiresPermissions("taolink:inventory:alert:resolve")
    public Result<String> resolveAlert(@PathVariable String id, @RequestBody ResolveAlertRequest req) {
        Result<String> check = req.check();
        if (!check.isSuccess()) {
            return check;
        }
        return taolinkInventoryAlertService.resolveAlert(id, req.getHandler());
    }

    @Operation(summary = "查询预警列表")
    @GetMapping(value = "/listAlerts")
    public Result<List<TaolinkInventoryAlert>> listAlerts(
            @RequestParam(name = "alertType", required = false) String alertType,
            @RequestParam(name = "status", required = false) String status) {
        List<TaolinkInventoryAlert> alerts = taolinkInventoryAlertService.listAlerts(alertType, status);
        return Result.OK(alerts);
    }

    @Operation(summary = "获取预警统计")
    @GetMapping(value = "/stats")
    public Result<java.util.Map<String, Object>> getAlertStats() {
        return taolinkInventoryAlertService.getAlertStats();
    }
}
