package org.jeecg.modules.taolink.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.taolink.entity.TaolinkSettlementRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;
import java.util.Date;

/**
 * 代发结算流水记录服务接口
 */
public interface ITaolinkSettlementRecordService extends IService<TaolinkSettlementRecord> {

    /**
     * 生成结算记录
     * @param settlementRecord 结算记录信息
     * @return 生成结果
     */
    Result<String> createSettlementRecord(TaolinkSettlementRecord settlementRecord);

    /**
     * 标记结算记录为已结算
     * @param id 结算记录ID
     * @return 操作结果
     */
    Result<String> settleRecord(String id);

    /**
     * 取消结算记录
     * @param id 结算记录ID
     * @return 操作结果
     */
    Result<String> cancelRecord(String id);

    /**
     * 获取月度对账单
     * @param supplierId 供应商ID
     * @param month 月份，格式：yyyy-MM
     * @return 月度对账单数据
     */
    Result<Map<String, Object>> getMonthlyStatement(String supplierId, String month);
}
