import { defHttp } from '@/utils/http/axios';

/**
 * 订单管理API
 */
export const orderApi = {
  /**
   * 获取订单列表
   * @param params 查询参数
   * @returns 订单列表
   */
  getOrderList: (params: any) => defHttp.get({ url: '/taolink/order/list', params }),

  /**
   * 通过ID查询订单
   * @param id 订单ID
   * @returns 订单详情
   */
  getOrderById: (id: string) => defHttp.get({ url: '/taolink/order/queryById', params: { id } }),

  /**
   * 添加订单
   * @param data 订单信息
   * @returns 添加结果
   */
  addOrder: (data: any) => defHttp.post({ url: '/taolink/order/add', data }),

  /**
   * 编辑订单
   * @param data 订单信息
   * @returns 编辑结果
   */
  editOrder: (data: any) => defHttp.post({ url: '/taolink/order/edit', data }),

  /**
   * 删除订单
   * @param id 订单ID
   * @returns 删除结果
   */
  deleteOrder: (id: string) => defHttp.delete({ url: '/taolink/order/delete', params: { id } }),

  /**
   * 订单履约分配
   * @param data 履约分配请求
   * @returns 分配结果
   */
  assignFulfillment: (data: { orderId: string; fulfillmentType: string; warehouseId?: string; supplierId?: string }) => 
    defHttp.post({ url: '/taolink/order/assignFulfillment', data }),
};
