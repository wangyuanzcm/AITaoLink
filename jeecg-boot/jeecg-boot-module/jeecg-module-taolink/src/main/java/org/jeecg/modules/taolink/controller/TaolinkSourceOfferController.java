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
import org.jeecg.modules.taolink.entity.TaolinkSourceOffer;
import org.jeecg.modules.taolink.service.ITaolinkSourceOfferIngestService;
import org.jeecg.modules.taolink.service.ITaolinkSourceOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Tag(name = "TaoLink-来源商品")
@RestController
@RequestMapping("/taolink/sourceOffer")
public class TaolinkSourceOfferController extends JeecgController<TaolinkSourceOffer, ITaolinkSourceOfferService> {
    @Autowired
    private ITaolinkSourceOfferService taolinkSourceOfferService;

    @Autowired
    private ITaolinkSourceOfferIngestService taolinkSourceOfferIngestService;

    @Operation(summary = "分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<TaolinkSourceOffer>> list(TaolinkSourceOffer sourceOffer,
                                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                 HttpServletRequest req) {
        QueryWrapper<TaolinkSourceOffer> queryWrapper = QueryGenerator.initQueryWrapper(sourceOffer, req.getParameterMap());
        queryWrapper.orderByDesc("fetched_at");
        Page<TaolinkSourceOffer> page = new Page<>(pageNo, pageSize);
        IPage<TaolinkSourceOffer> pageList = taolinkSourceOfferService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "添加")
    @PostMapping(value = "/add")
    @RequiresPermissions("taolink:sourceOffer:add")
    public Result<String> add(@RequestBody TaolinkSourceOffer sourceOffer) {
        taolinkSourceOfferService.save(sourceOffer);
        return Result.OK("添加成功！");
    }

    @Operation(summary = "编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("taolink:sourceOffer:edit")
    public Result<String> edit(@RequestBody TaolinkSourceOffer sourceOffer) {
        taolinkSourceOfferService.updateById(sourceOffer);
        return Result.OK("更新成功！");
    }

    @Operation(summary = "通过ID删除")
    @DeleteMapping(value = "/delete")
    @RequiresPermissions("taolink:sourceOffer:delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        taolinkSourceOfferService.removeById(id);
        return Result.OK("删除成功!");
    }

    @Operation(summary = "通过ID查询")
    @GetMapping(value = "/queryById")
    public Result<TaolinkSourceOffer> queryById(@RequestParam(name = "id") String id) {
        TaolinkSourceOffer entity = taolinkSourceOfferService.getById(id);
        if (entity == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(entity);
    }

    @Operation(summary = "采集并更新来源商品")
    @PostMapping(value = "/fetch")
    @RequiresPermissions("taolink:sourceOffer:fetch")
    public Result<TaolinkSourceOffer> fetch(@RequestParam(name = "platform") String platform,
                                           @RequestParam(name = "numIid") String numIid) {
        return taolinkSourceOfferIngestService.fetchAndUpsert(platform, numIid);
    }
}

