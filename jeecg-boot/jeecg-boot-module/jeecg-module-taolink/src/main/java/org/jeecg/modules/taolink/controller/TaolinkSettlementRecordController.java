package org.jeecg.modules.taolink.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.taolink.entity.TaolinkSettlementRecord;
import org.jeecg.modules.taolink.service.ITaolinkSettlementRecordService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 代发结算流水记录控制器
 */
@Tag(name = "代发结算管理")
@RestController
@RequestMapping("/taolink/settlement-records")
@Slf4j
public class TaolinkSettlementRecordController {

    @Autowired
    private ITaolinkSettlementRecordService taolinkSettlementRecordService;

    /**
     * 分页查询结算记录
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @param supplierId 供应商ID
     * @param status 状态
     * @return 分页结果
     */
    @Operation(summary="分页查询结算记录")
    @GetMapping(value = "/records")
    public Result<?> queryPageList(
            @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
            @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            @RequestParam(name="supplierId", required=false) String supplierId,
            @RequestParam(name="status", required=false) String status
    ) {
        try {
            Page<TaolinkSettlementRecord> page = new Page<>(pageNo, pageSize);
            // 这里可以根据条件构建查询
            IPage<TaolinkSettlementRecord> pageList = taolinkSettlementRecordService.page(page);
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error("查询结算记录失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建结算记录
     * @param settlementRecord 结算记录信息
     * @return 创建结果
     */
    @Operation(summary="创建结算记录")
    @PostMapping(value = "/create")
    public Result<String> create(@RequestBody TaolinkSettlementRecord settlementRecord) {
        return taolinkSettlementRecordService.createSettlementRecord(settlementRecord);
    }

    /**
     * 标记结算记录为已结算
     * @param id 结算记录ID
     * @return 操作结果
     */
    @Operation(summary="标记结算记录为已结算")
    @PutMapping(value = "/{id}/settle")
    public Result<String> settle(@PathVariable String id) {
        return taolinkSettlementRecordService.settleRecord(id);
    }

    /**
     * 取消结算记录
     * @param id 结算记录ID
     * @return 操作结果
     */
    @Operation(summary="取消结算记录")
    @PutMapping(value = "/{id}/cancel")
    public Result<String> cancel(@PathVariable String id) {
        return taolinkSettlementRecordService.cancelRecord(id);
    }

    /**
     * 获取月度对账单
     * @param supplierId 供应商ID
     * @param month 月份，格式：yyyy-MM
     * @return 月度对账单数据
     */
    @Operation(summary="获取月度对账单")
    @GetMapping(value = "/monthly")
    public Result<Map<String, Object>> getMonthlyStatement(
            @RequestParam(name="supplierId") String supplierId,
            @RequestParam(name="month") String month
    ) {
        return taolinkSettlementRecordService.getMonthlyStatement(supplierId, month);
    }

    /**
     * 删除结算记录
     * @param id 结算记录ID
     * @return 删除结果
     */
    @Operation(summary="删除结算记录")
    @DeleteMapping(value = "/{id}")
    public Result<String> delete(@PathVariable String id) {
        try {
            taolinkSettlementRecordService.removeById(id);
            return Result.OK("删除成功！");
        } catch (Exception e) {
            log.error("删除结算记录失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取结算记录详情
     * @param id 结算记录ID
     * @return 结算记录详情
     */
    @Operation(summary="获取结算记录详情")
    @GetMapping(value = "/{id}")
    public Result<TaolinkSettlementRecord> getById(@PathVariable String id) {
        try {
            TaolinkSettlementRecord record = taolinkSettlementRecordService.getById(id);
            return Result.OK(record);
        } catch (Exception e) {
            log.error("获取结算记录详情失败", e);
            return Result.error("获取详情失败: " + e.getMessage());
        }
    }
}
