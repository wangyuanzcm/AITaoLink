package org.jeecg.modules.taolink.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.taolink.entity.TaolinkPurchase;

public interface ITaolinkPurchaseService extends JeecgService<TaolinkPurchase> {
    /**
     * 代发订单发货回填
     * @param lineId 采购单行ID
     * @param sourceTrackingCompany 物流公司
     * @param sourceTrackingNo 运单号
     * @param freightCost 运费
     * @param remark 备注
     * @return 操作结果
     */
    Result<String> fillTracking(String lineId, String sourceTrackingCompany, String sourceTrackingNo, Double freightCost, String remark);
}

