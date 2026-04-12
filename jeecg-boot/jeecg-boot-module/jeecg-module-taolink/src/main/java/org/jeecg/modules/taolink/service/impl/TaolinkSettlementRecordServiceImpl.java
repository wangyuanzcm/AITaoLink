package org.jeecg.modules.taolink.service.impl;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.taolink.entity.TaolinkSettlementRecord;
import org.jeecg.modules.taolink.mapper.TaolinkSettlementRecordMapper;
import org.jeecg.modules.taolink.service.ITaolinkSettlementRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.math.BigDecimal;

/**
 * 代发结算流水记录服务实现类
 */
@Service
public class TaolinkSettlementRecordServiceImpl extends ServiceImpl<TaolinkSettlementRecordMapper, TaolinkSettlementRecord> implements ITaolinkSettlementRecordService {

    @Override
    public Result<String> createSettlementRecord(TaolinkSettlementRecord settlementRecord) {
        try {
            // 设置默认状态
            if (settlementRecord.getStatus() == null) {
                settlementRecord.setStatus("pending");
            }
            // 保存结算记录
            this.save(settlementRecord);
            return Result.OK("结算记录创建成功！");
        } catch (Exception e) {
            return Result.error("创建结算记录失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> settleRecord(String id) {
        try {
            TaolinkSettlementRecord record = this.getById(id);
            if (record == null) {
                return Result.error("未找到对应结算记录");
            }
            if (!"pending".equals(record.getStatus())) {
                return Result.error("该记录状态不是待结算，无法操作");
            }
            // 更新状态为已结算
            record.setStatus("settled");
            record.setSettleTime(new Date());
            this.updateById(record);
            return Result.OK("结算记录已标记为已结算！");
        } catch (Exception e) {
            return Result.error("标记结算失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> cancelRecord(String id) {
        try {
            TaolinkSettlementRecord record = this.getById(id);
            if (record == null) {
                return Result.error("未找到对应结算记录");
            }
            if (!"pending".equals(record.getStatus())) {
                return Result.error("该记录状态不是待结算，无法操作");
            }
            // 更新状态为已取消
            record.setStatus("cancelled");
            this.updateById(record);
            return Result.OK("结算记录已取消！");
        } catch (Exception e) {
            return Result.error("取消结算失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> getMonthlyStatement(String supplierId, String month) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<TaolinkSettlementRecord> qw = new LambdaQueryWrapper<>();
            qw.eq(TaolinkSettlementRecord::getSupplierId, supplierId);
            qw.eq(TaolinkSettlementRecord::getStatus, "settled");
            // 这里可以根据月份进行时间范围查询
            // 示例：qw.between(TaolinkSettlementRecord::getSettleTime, startDate, endDate);

            // 查询该供应商的结算记录
            List<TaolinkSettlementRecord> records = this.list(qw);

            // 计算汇总数据
            BigDecimal totalAmount = BigDecimal.ZERO;
            int recordCount = records.size();
            for (TaolinkSettlementRecord record : records) {
                totalAmount = totalAmount.add(record.getAmount());
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("supplierId", supplierId);
            result.put("month", month);
            result.put("totalAmount", totalAmount);
            result.put("recordCount", recordCount);
            result.put("records", records);

            return Result.OK(result);
        } catch (Exception e) {
            return Result.error("获取月度对账单失败: " + e.getMessage());
        }
    }
}
