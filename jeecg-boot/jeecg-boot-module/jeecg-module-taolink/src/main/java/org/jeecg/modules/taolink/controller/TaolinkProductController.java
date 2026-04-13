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
import org.jeecg.modules.taolink.entity.TaolinkProduct;
import org.jeecg.modules.taolink.service.ITaolinkProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;

@Slf4j
@Tag(name = "TaoLink-商品")
@RestController
@RequestMapping("/taolink/product")
public class TaolinkProductController extends JeecgController<TaolinkProduct, ITaolinkProductService> {
    @Autowired
    private ITaolinkProductService taolinkProductService;

    @Operation(summary = "分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<TaolinkProduct>> list(TaolinkProduct product,
                                               @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                               HttpServletRequest req) {
        QueryWrapper<TaolinkProduct> queryWrapper = QueryGenerator.initQueryWrapper(product, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<TaolinkProduct> page = new Page<>(pageNo, pageSize);
        IPage<TaolinkProduct> pageList = taolinkProductService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "添加商品")
    @PostMapping(value = "/add")
    @RequiresPermissions("taolink:product:add")
    public Result<String> add(@RequestBody TaolinkProduct product) {
        taolinkProductService.save(product);
        return Result.OK("添加成功！");
    }

    @Operation(summary = "编辑商品")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("taolink:product:edit")
    public Result<String> edit(@RequestBody TaolinkProduct product) {
        taolinkProductService.updateById(product);
        return Result.OK("更新成功！");
    }

    @Operation(summary = "通过ID删除")
    @DeleteMapping(value = "/delete")
    @RequiresPermissions("taolink:product:delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        taolinkProductService.removeById(id);
        return Result.OK("删除成功!");
    }

    @Operation(summary = "通过ID查询")
    @GetMapping(value = "/queryById")
    public Result<TaolinkProduct> queryById(@RequestParam(name = "id") String id) {
        TaolinkProduct entity = taolinkProductService.getById(id);
        if (entity == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(entity);
    }

    @Operation(summary = "商品上架")
    @PostMapping(value = "/listed")
    @RequiresPermissions("taolink:product:listed")
    public Result<String> listed(@RequestBody ListDelistRequest req) {
        req.check();
        TaolinkProduct product = taolinkProductService.getById(req.getId());
        if (product == null) {
            return Result.error("未找到对应商品");
        }
        product.setListingStatus("listed");
        product.setListedAt(new Date());
        taolinkProductService.updateById(product);
        return Result.OK("上架成功！");
    }

    @Operation(summary = "商品下架")
    @PostMapping(value = "/delisted")
    @RequiresPermissions("taolink:product:delisted")
    public Result<String> delisted(@RequestBody ListDelistRequest req) {
        req.check();
        TaolinkProduct product = taolinkProductService.getById(req.getId());
        if (product == null) {
            return Result.error("未找到对应商品");
        }
        product.setListingStatus("delisted");
        product.setDelistedAt(new Date());
        taolinkProductService.updateById(product);
        return Result.OK("下架成功！");
    }

    public static class ListDelistRequest {
        private String id;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void check() {
            if (id == null || id.isEmpty()) {
                throw new RuntimeException("商品ID不能为空");
            }
        }
    }
}
