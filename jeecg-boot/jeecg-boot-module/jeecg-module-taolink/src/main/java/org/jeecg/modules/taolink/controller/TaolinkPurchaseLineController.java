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
import org.jeecg.modules.taolink.entity.TaolinkPurchaseLine;
import org.jeecg.modules.taolink.service.ITaolinkPurchaseLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;

@Slf4j
@Tag(name = "TaoLink-采购明细")
@RestController
@RequestMapping("/taolink/purchaseLine")
public class TaolinkPurchaseLineController extends JeecgController<TaolinkPurchaseLine, ITaolinkPurchaseLineService> {
    @Autowired
    private ITaolinkPurchaseLineService taolinkPurchaseLineService;

    @Operation(summary = "分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<TaolinkPurchaseLine>> list(TaolinkPurchaseLine line,
                                                      @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                      @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                      HttpServletRequest req) {
        QueryWrapper<TaolinkPurchaseLine> queryWrapper = QueryGenerator.initQueryWrapper(line, req.getParameterMap());
        Page<TaolinkPurchaseLine> page = new Page<>(pageNo, pageSize);
        IPage<TaolinkPurchaseLine> pageList = taolinkPurchaseLineService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "代发订单发货回填")
    @PostMapping(value = "/fillTracking")
    @RequiresPermissions("taolink:purchaseLine:fillTracking")
    public Result<String> fillTracking(@RequestBody FillTrackingRequest req) {
        req.check();
        TaolinkPurchaseLine line = taolinkPurchaseLineService.getById(req.getPurchaseLineId());
        if (line == null) {
            return Result.error("未找到对应采购行");
        }
        line.setSourceTrackingCompany(req.getTrackingCompany());
        line.setSourceTrackingNo(req.getTrackingNo());
        line.setShippedAt(new Date());

        // 计算总成本
        int unitTotal = (line.getUnitCost() != null ? line.getUnitCost() : 0) * (line.getQty() != null ? line.getQty() : 1);
        int freight = line.getFreightCost() != null ? line.getFreightCost() : 0;
        line.setTotalCost(unitTotal + freight);

        taolinkPurchaseLineService.updateById(line);
        return Result.OK("回填成功！");
    }

    @Data
    public static class FillTrackingRequest {
        private String purchaseLineId;
        private String trackingCompany;
        private String trackingNo;

        public void check() {
            if (purchaseLineId == null || purchaseLineId.isEmpty()) {
                throw new RuntimeException("采购行ID不能为空");
            }
            if (trackingCompany == null || trackingCompany.isEmpty()) {
                throw new RuntimeException("物流公司不能为空");
            }
            if (trackingNo == null || trackingNo.isEmpty()) {
                throw new RuntimeException("运单号不能为空");
            }
        }
    }
}
