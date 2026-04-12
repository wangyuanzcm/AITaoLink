package org.jeecg.modules.taolink.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.taolink.entity.TaolinkShop;
import org.jeecg.modules.taolink.service.ITaolinkShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Tag(name = "TaoLink-店铺")
@RestController
@RequestMapping("/taolink/shop")
public class TaolinkShopController extends JeecgController<TaolinkShop, ITaolinkShopService> {
    @Autowired
    private ITaolinkShopService taolinkShopService;

    @Operation(summary = "店铺列表")
    @GetMapping(value = "/list")
    public Result<IPage<TaolinkShop>> list(TaolinkShop shop,
                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                            HttpServletRequest req) {
        QueryWrapper<TaolinkShop> queryWrapper = QueryGenerator.initQueryWrapper(shop, req.getParameterMap());
        Page<TaolinkShop> page = new Page<>(pageNo, pageSize);
        IPage<TaolinkShop> pageList = taolinkShopService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "通过ID查询")
    @GetMapping(value = "/queryById")
    public Result<TaolinkShop> queryById(@RequestParam(name = "id") String id) {
        TaolinkShop entity = taolinkShopService.getById(id);
        if (entity == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(entity);
    }

    @Operation(summary = "绑定店铺")
    @PostMapping(value = "/bind")
    public Result<String> bind(@RequestBody TaolinkShop shop) {
        taolinkShopService.save(shop);
        return Result.OK("绑定成功！");
    }

    @Operation(summary = "解绑店铺")
    @PostMapping(value = "/unbind")
    public Result<String> unbind(@RequestParam(name = "id") String id) {
        TaolinkShop shop = taolinkShopService.getById(id);
        if (shop == null) {
            return Result.error("未找到对应店铺");
        }
        // 软解绑：改状态为unbound，不删除数据
        shop.setStatus("unbound");
        // 清空授权信息
        shop.setApiSessionKey(null);
        taolinkShopService.updateById(shop);
        return Result.OK("解绑成功");
    }

    @Operation(summary = "编辑店铺")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody TaolinkShop shop) {
        taolinkShopService.updateById(shop);
        return Result.OK("更新成功！");
    }

    @Operation(summary = "删除店铺")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        taolinkShopService.removeById(id);
        return Result.OK("删除成功!");
    }
}
