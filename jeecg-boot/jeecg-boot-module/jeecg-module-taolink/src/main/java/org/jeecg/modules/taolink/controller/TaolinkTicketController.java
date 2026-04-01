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
import org.jeecg.modules.taolink.entity.TaolinkTicket;
import org.jeecg.modules.taolink.service.ITaolinkTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Tag(name = "TaoLink-工单")
@RestController
@RequestMapping("/taolink/ticket")
public class TaolinkTicketController extends JeecgController<TaolinkTicket, ITaolinkTicketService> {
    @Autowired
    private ITaolinkTicketService taolinkTicketService;

    @Operation(summary = "分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<TaolinkTicket>> list(TaolinkTicket ticket,
                                            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                            HttpServletRequest req) {
        QueryWrapper<TaolinkTicket> queryWrapper = QueryGenerator.initQueryWrapper(ticket, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<TaolinkTicket> page = new Page<>(pageNo, pageSize);
        IPage<TaolinkTicket> pageList = taolinkTicketService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "添加")
    @PostMapping(value = "/add")
    @RequiresPermissions("taolink:ticket:add")
    public Result<String> add(@RequestBody TaolinkTicket ticket) {
        taolinkTicketService.save(ticket);
        return Result.OK("添加成功！");
    }

    @Operation(summary = "编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    @RequiresPermissions("taolink:ticket:edit")
    public Result<String> edit(@RequestBody TaolinkTicket ticket) {
        taolinkTicketService.updateById(ticket);
        return Result.OK("更新成功！");
    }

    @Operation(summary = "通过ID删除")
    @DeleteMapping(value = "/delete")
    @RequiresPermissions("taolink:ticket:delete")
    public Result<String> delete(@RequestParam(name = "id") String id) {
        taolinkTicketService.removeById(id);
        return Result.OK("删除成功!");
    }

    @Operation(summary = "通过ID查询")
    @GetMapping(value = "/queryById")
    public Result<TaolinkTicket> queryById(@RequestParam(name = "id") String id) {
        TaolinkTicket entity = taolinkTicketService.getById(id);
        if (entity == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(entity);
    }
}

