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
}

