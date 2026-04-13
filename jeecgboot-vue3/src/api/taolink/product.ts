import { defHttp } from '@/utils/http/axios';

/**
 * 商品管理API
 */
export const taolinkProductApi = {
  /**
   * 获取商品列表
   * @param params 查询参数
   * @returns 商品列表
   */
  list: (params: any) => defHttp.get({ url: '/taolink/product/list', params }),

  /**
   * 通过ID查询商品
   * @param id 商品ID
   * @returns 商品详情
   */
  queryById: (params: { id: string }) => defHttp.get({ url: '/taolink/product/queryById', params }),

  /**
   * 新增商品
   * @param data 商品信息
   * @returns 新增结果
   */
  add: (data: any) => defHttp.post({ url: '/taolink/product/add', data }),

  /**
   * 编辑商品
   * @param data 商品信息
   * @returns 编辑结果
   */
  edit: (data: any) => defHttp.post({ url: '/taolink/product/edit', data }),

  /**
   * 删除商品
   * @param id 商品ID
   * @returns 删除结果
   */
  delete: (params: { id: string }) => defHttp.delete({ url: '/taolink/product/delete', params }),

  /**
   * 上架商品
   * @param id 商品ID
   * @returns 上架结果
   */
  listed: (params: { id: string }) => defHttp.post({ url: '/taolink/product/listed', params }),

  /**
   * 下架商品
   * @param id 商品ID
   * @returns 下架结果
   */
  delisted: (params: { id: string }) => defHttp.post({ url: '/taolink/product/delisted', params }),
};
