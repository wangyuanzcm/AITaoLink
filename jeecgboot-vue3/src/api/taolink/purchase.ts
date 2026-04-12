import { defHttp } from '@/utils/http/axios';

/**
 * 采购管理API
 */
export const purchaseApi = {
  /**
   * 获取采购单列表
   * @param params 查询参数
   * @returns 采购单列表
   */
  getPurchaseList: (params: any) => defHttp.get({ url: '/taolink/purchase/list', params }),

  /**
   * 获取采购单行列表
   * @param params 查询参数
   * @returns 采购单行列表
   */
  getPurchaseLineList: (params: any) => defHttp.get({ url: '/taolink/purchaseLine/list', params }),

  /**
   * 发货回填
   * @param lineId 采购单行ID
   * @param data 回填数据
   * @returns 回填结果
   */
  fillTracking: (lineId: string, data: { sourceTrackingCompany: string; sourceTrackingNo: string; freightCost: number; remark: string }) => 
    defHttp.post({ url: `/taolink/purchase/${lineId}/fillTracking`, data }),
};
