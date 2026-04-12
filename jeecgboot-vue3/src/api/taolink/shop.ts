import { defHttp } from '@/utils/http/axios';

/**
 * 店铺管理API
 */
export const taolinkShopApi = {
  /**
   * 获取店铺列表
   * @param params 查询参数
   * @returns 店铺列表
   */
  list: (params: any) => defHttp.get({ url: '/taolink/shop/list', params }),

  /**
   * 通过ID查询店铺
   * @param id 店铺ID
   * @returns 店铺详情
   */
  queryById: (params: { id: string }) => defHttp.get({ url: '/taolink/shop/queryById', params }),

  /**
   * 绑定店铺
   * @param data 店铺信息
   * @returns 绑定结果
   */
  bind: (data: any) => defHttp.post({ url: '/taolink/shop/bind', data }),

  /**
   * 解绑店铺
   * @param id 店铺ID
   * @returns 解绑结果
   */
  unbind: (params: { id: string }) => defHttp.post({ url: '/taolink/shop/unbind', params }),

  /**
   * 编辑店铺
   * @param data 店铺信息
   * @returns 编辑结果
   */
  edit: (data: any) => defHttp.post({ url: '/taolink/shop/edit', data }),

  /**
   * 删除店铺
   * @param id 店铺ID
   * @returns 删除结果
   */
  delete: (params: { id: string }) => defHttp.delete({ url: '/taolink/shop/delete', params }),

  /**
   * 获取授权URL
   * @returns 授权URL
   */
  getAuthorizeUrl: (params: any) => defHttp.get({ url: '/taolink/shop/authorizeUrl', params }),

  /**
   * 重新授权
   * @param id 店铺ID
   * @returns 授权URL
   */
  reauthorize: (params: { id: string }) => defHttp.post({ url: `/taolink/shop/${params.id}/reauthorize` }),
};

