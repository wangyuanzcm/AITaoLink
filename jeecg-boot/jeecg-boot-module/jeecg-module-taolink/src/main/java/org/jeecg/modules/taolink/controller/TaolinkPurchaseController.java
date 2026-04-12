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
import org.jeecg.modules.taolink.entity.TaolinkPurchase;
import org.jeecg.modules.taolink.service.ITaolinkPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Tag(name = "TaoLink-采购单")
@RestController
@RequestMapping("/taolink/purchase")
public class TaolinkPurchaseController extends JeecgController<TaolinkPurchase, ITaolinkPurchaseService> {
    @Autowired
    private ITaolinkPurchaseService taolinkPurchaseService;

    @Operation(summary = "分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<TaolinkPurchase>> list(TaolinkPurchase purchase,
                                              @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                              HttpServletRequest req) {
        QueryWrapper<TaolinkPurchase> queryWrapper = QueryGenerator.initQueryWrapper(purchase, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<TaolinkPurchase> page = new Page<>(pageNo, pageSize);
        IPage<TaolinkPurchase> pageList = taolinkPurchaseService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "添加")
    @PostMapping(value = "/add")
    @RequiresPermissions("taolink:purchase:add")
    public Result<String> add(@RequestBody TaolinkPurchase purchase) {
        taolinkPurchaseService.save(purchase);
        return Result.OK("添加成功！");
    }

    @Operation(summary = "编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("taolink:purchase:edit")
    public Result<String> edit(@RequestBody TaolinkPurchase purchase) {
        taolinkPurchaseService.updateById(purchase);
        return Result.OK("更新成功！");
    }

    @Operation(summary = "通过ID删除")
    @DeleteMapping(value = "/delete")
    @RequiresPermissions("taolink:purchase:delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        taolinkPurchaseService.removeById(id);
        return Result.OK("删除成功!");
    }

    @Operation(summary = "通过ID查询")
    @GetMapping(value = "/queryById")
    public Result<TaolinkPurchase> queryById(@RequestParam(name = "id") String id) {
        TaolinkPurchase entity = taolinkPurchaseService.getById(id);
        if (entity == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(entity);
    }

    @Data
    public static class TrackingRequest {
        private String sourceTrackingCompany;
        private String sourceTrackingNo;
        private Double freightCost;
        private String remark;
    }

    @Operation(summary = "代发订单发货回填")
    @PostMapping(value = "/{lineId}/fillTracking")
    @RequiresPermissions("taolink:purchase:fillTracking")
    public Result<String> fillTracking(@PathVariable String lineId, @RequestBody TrackingRequest req) {
        return taolinkPurchaseService.fillTracking(lineId, req.getSourceTrackingCompany(), 
                req.getSourceTrackingNo(), req.getFreightCost(), req.getRemark());
    }
}

