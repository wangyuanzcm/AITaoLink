package org.jeecg.modules.taolink.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.taolink.entity.TaolinkInventoryAlert;

import java.util.List;
import java.util.Map;

public interface ITaolinkInventoryAlertService extends JeecgService<TaolinkInventoryAlert> {
    /**
     * 创建库存预警记录
     * @param alert 预警记录
     * @return 操作结果
     */
    Result<String> createAlert(TaolinkInventoryAlert alert);

    /**
     * 解决预警
     * @param id 预警ID
     * @param handler 处理人
     * @return 操作结果
     */
    Result<String> resolveAlert(String id, String handler);

    /**
     * 查询预警列表
     * @param alertType 预警类型
     * @param status 状态
     * @return 预警列表
     */
    List<TaolinkInventoryAlert> listAlerts(String alertType, String status);

    /**
     * 获取预警统计
     * @return 预警统计数据
     */
    Result<Map<String, Object>> getAlertStats();
}
