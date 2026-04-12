package org.jeecg.modules.taolink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.taolink.entity.TaolinkPurchase;
import org.jeecg.modules.taolink.entity.TaolinkPurchaseLine;
import org.jeecg.modules.taolink.entity.TaolinkSettlementRecord;
import org.jeecg.modules.taolink.mapper.TaolinkPurchaseMapper;
import org.jeecg.modules.taolink.service.ITaolinkPurchaseLineService;
import org.jeecg.modules.taolink.service.ITaolinkPurchaseService;
import org.jeecg.modules.taolink.service.ITaolinkSettlementRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.math.BigDecimal;

@Service
public class TaolinkPurchaseServiceImpl extends ServiceImpl<TaolinkPurchaseMapper, TaolinkPurchase> implements ITaolinkPurchaseService {

    @Autowired
    private ITaolinkPurchaseLineService taolinkPurchaseLineService;

    @Autowired
    private ITaolinkSettlementRecordService taolinkSettlementRecordService;

    @Override
    public Result<String> fillTracking(String lineId, String sourceTrackingCompany, String sourceTrackingNo, Double freightCost, String remark) {
        try {
            // 查询采购单行记录
            TaolinkPurchaseLine purchaseLine = taolinkPurchaseLineService.getById(lineId);
            if (purchaseLine == null) {
                return Result.error("未找到对应采购单行记录");
            }

            // 更新采购单行信息
            purchaseLine.setSourceTrackingCompany(sourceTrackingCompany);
            purchaseLine.setSourceTrackingNo(sourceTrackingNo);
            purchaseLine.setShippedAt(new Date());
            if (freightCost != null) {
                // 将 Double 类型的 freightCost 转换为 Integer（分）
                int freightCostInt = (int) (freightCost * 100);
                purchaseLine.setFreightCost(freightCostInt);
                // 计算总成本（商品成本 + 运费）
                int totalCost = purchaseLine.getTotalCost() != null ? purchaseLine.getTotalCost() : 0;
                totalCost += freightCostInt;
                purchaseLine.setTotalCost(totalCost);
            }
            taolinkPurchaseLineService.updateById(purchaseLine);

            // 生成结算记录
            TaolinkSettlementRecord settlementRecord = new TaolinkSettlementRecord()
                    .setShopId(purchaseLine.getShopId())
                    .setSettleType("purchase")
                    .setAmount(purchaseLine.getTotalCost() != null ? BigDecimal.valueOf(purchaseLine.getTotalCost()) : BigDecimal.ZERO)
                    .setPurchaseId(purchaseLine.getPurchaseId())
                    .setPurchaseLineId(lineId)
                    .setStatus("pending")
                    .setRemark(remark);
            taolinkSettlementRecordService.save(settlementRecord);

            return Result.OK("发货回填成功并生成结算记录！");
        } catch (Exception e) {
            return Result.error("发货回填失败: " + e.getMessage());
        }
    }
}

