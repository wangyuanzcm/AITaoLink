import { defHttp } from '@/utils/http/axios';

/**
 * 库存管理API
 */
export const inventoryApi = {
  /**
   * 获取库存列表
   * @param params 查询参数
   * @returns 库存列表
   */
  getInventoryList: (params: any) => defHttp.get({ url: '/taolink/inventory/list', params }),

  /**
   * 预留库存
   * @param data 预留请求
   * @returns 预留结果
   */
  reserveInventory: (data: { warehouseId: string; productSkuId: string; qty: number; refType: string; refId: string }) => 
    defHttp.post({ url: '/taolink/inventory/reserve', data }),

  /**
   * 释放库存
   * @param data 释放请求
   * @returns 释放结果
   */
  releaseInventory: (data: { warehouseId: string; productSkuId: string; qty: number; refType: string; refId: string }) => 
    defHttp.post({ url: '/taolink/inventory/release', data }),

  /**
   * 调整库存
   * @param data 调整请求
   * @returns 调整结果
   */
  adjustInventory: (data: { warehouseId: string; productSkuId: string; qty: number; refType: string; refId: string }) => 
    defHttp.post({ url: '/taolink/inventory/adjustOnHand', data }),

  /**
   * 扣减库存
   * @param data 扣减请求
   * @returns 扣减结果
   */
  deductInventory: (data: { warehouseId: string; productSkuId: string; qty: number; refType: string; refId: string }) => 
    defHttp.post({ url: '/taolink/inventory/deduct', data }),

  /**
   * 根据SKU ID获取库存列表
   * @param productSkuId SKU ID
   * @returns 库存列表
   */
  getInventoryBySkuId: (productSkuId: string) => 
    defHttp.get({ url: '/taolink/inventory/listByProductSkuId', params: { productSkuId } }),

  /**
   * 获取库存流水列表
   * @param params 查询参数
   * @returns 流水列表
   */
  getMovementList: (params: any) => defHttp.get({ url: '/taolink/inventoryMovement/list', params }),

  /**
   * 更新预警阈值
   * @param id 库存ID
   * @param data 阈值数据
   * @returns 更新结果
   */
  updateThreshold: (id: string, data: { warningMin: number; overstockDays: number }) => 
    defHttp.put({ url: `/taolink/inventory/${id}/threshold`, data }),

  /**
   * 获取库存分析概览
   * @returns 库存分析概览数据
   */
  getInventoryAnalysisOverview: () => defHttp.get({ url: '/taolink/inventory/analysis/overview' }),

  /**
   * 获取库存分析指标
   * @returns 库存分析指标数据
   */
  getInventoryAnalysisMetrics: () => defHttp.get({ url: '/taolink/inventory/analysis/metrics' }),

  /**
   * 获取预警列表
   * @param params 查询参数
   * @returns 预警列表
   */
  getAlertList: (params: any) => defHttp.get({ url: '/taolink/inventory/alert/list', params }),

  /**
   * 解决预警
   * @param id 预警ID
   * @returns 解决结果
   */
  resolveAlert: (id: string, data: { handler: string }) => defHttp.put({ url: `/taolink/inventory/alert/${id}/resolve`, data }),
}; 
