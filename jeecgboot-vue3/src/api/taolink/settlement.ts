import { defHttp } from '@/utils/http/axios';

/**
 * 结算管理API
 */
export const settlementApi = {
  /**
   * 获取结算记录列表
   * @param params 查询参数
   * @returns 结算记录列表
   */
  getSettlementList: (params: any) => defHttp.get({ url: '/taolink/settlement/list', params }),

  /**
   * 创建结算记录
   * @param data 结算记录信息
   * @returns 创建结果
   */
  createSettlement: (data: any) => defHttp.post({ url: '/taolink/settlement/create', data }),

  /**
   * 标记结算记录为已结算
   * @param id 结算记录ID
   * @returns 操作结果
   */
  settleRecord: (id: string) => defHttp.put({ url: `/taolink/settlement/${id}/settle` }),

  /**
   * 取消结算记录
   * @param id 结算记录ID
   * @returns 操作结果
   */
  cancelRecord: (id: string) => defHttp.put({ url: `/taolink/settlement/${id}/cancel` }),

  /**
   * 获取月度对账单
   * @param supplierId 供应商ID
   * @param month 月份，格式：yyyy-MM
   * @returns 月度对账单数据
   */
  getMonthlyStatement: (supplierId: string, month: string) => 
    defHttp.get({ url: '/taolink/settlement/monthly', params: { supplierId, month } }),
};
