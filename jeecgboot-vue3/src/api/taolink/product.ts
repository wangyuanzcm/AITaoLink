import { defHttp } from '@/utils/http/axios';

/**
 * 商品管理API
 */
export const productApi = {
  /**
   * 获取商品列表
   * @param params 查询参数
   * @returns 商品列表
   */
  getProductList: (params: any) => defHttp.get({ url: '/taolink/product/list', params }),

  /**
   * 通过ID查询商品
   * @param id 商品ID
   * @returns 商品详情
   */
  getProductById: (id: string) => defHttp.get({ url: '/taolink/product/queryById', params: { id } }),

  /**
   * 添加商品
   * @param data 商品信息
   * @returns 添加结果
   */
  addProduct: (data: any) => defHttp.post({ url: '/taolink/product/add', data }),

  /**
   * 编辑商品
   * @param data 商品信息
   * @returns 编辑结果
   */
  editProduct: (data: any) => defHttp.post({ url: '/taolink/product/edit', data }),

  /**
   * 删除商品
   * @param id 商品ID
   * @returns 删除结果
   */
  deleteProduct: (id: string) => defHttp.delete({ url: '/taolink/product/delete', params: { id } }),

  /**
   * 商品上架
   * @param data 上架请求
   * @returns 上架结果
   */
  listedProduct: (data: { id: string }) => defHttp.post({ url: '/taolink/product/listed', data }),

  /**
   * 商品下架
   * @param data 下架请求
   * @returns 下架结果
   */
  delistedProduct: (data: { id: string }) => defHttp.post({ url: '/taolink/product/delisted', data }),
};
