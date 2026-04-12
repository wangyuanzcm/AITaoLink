import { defHttp } from '@/utils/http/axios';

/**
 * 店铺监控API
 */
export const taolinkMonitorApi = {
  /**
   * 获取店铺监控概览
   * @param params 查询参数
   * @returns 监控概览数据
   */
  getOverview: (params: { shopId: string }) => defHttp.get({ url: `/taolink/monitor/${params.shopId}/overview` }),

  /**
   * 获取店铺监控趋势数据
   * @param params 查询参数
   * @returns 趋势数据
   */
  getTrend: (params: { shopId: string; days: number }) => defHttp.get({ url: `/taolink/monitor/${params.shopId}/trend`, params }),

  /**
   * 获取店铺商品排行榜
   * @param params 查询参数
   * @returns 排行榜数据
   */
  getRankings: (params: { shopId: string }) => defHttp.get({ url: `/taolink/monitor/${params.shopId}/rankings` }),

  /**
   * 手动刷新监控数据
   * @param params 查询参数
   * @returns 刷新结果
   */
  refresh: (params: { shopId: string }) => defHttp.post({ url: `/taolink/monitor/${params.shopId}/refresh` }),
};
