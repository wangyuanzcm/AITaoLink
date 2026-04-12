import { defHttp } from '@/utils/http/axios';

/**
 * 店铺管理API
 */
export const shopApi = {
  /**
   * 获取店铺列表
   * @param params 查询参数
   * @returns 店铺列表
   */
  getShopList: (params: any) => defHttp.get({ url: '/taolink/shop/list', params }),

  /**
   * 通过ID查询店铺
   * @param id 店铺ID
   * @returns 店铺详情
   */
  getShopById: (id: string) => defHttp.get({ url: '/taolink/shop/queryById', params: { id } }),

  /**
   * 绑定店铺
   * @param data 店铺信息
   * @returns 绑定结果
   */
  bindShop: (data: any) => defHttp.post({ url: '/taolink/shop/bind', data }),

  /**
   * 解绑店铺
   * @param id 店铺ID
   * @returns 解绑结果
   */
  unbindShop: (id: string) => defHttp.post({ url: '/taolink/shop/unbind', params: { id } }),

  /**
   * 编辑店铺
   * @param data 店铺信息
   * @returns 编辑结果
   */
  editShop: (data: any) => defHttp.post({ url: '/taolink/shop/edit', data }),

  /**
   * 删除店铺
   * @param id 店铺ID
   * @returns 删除结果
   */
  deleteShop: (id: string) => defHttp.delete({ url: '/taolink/shop/delete', params: { id } }),
};
