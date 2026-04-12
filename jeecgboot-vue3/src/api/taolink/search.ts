import { defHttp } from '@/utils/http/axios';

/**
 * 搜索中心API
 */
export const searchApi = {
  /**
   * 统一搜索接口
   * @param params 搜索参数
   * @returns 搜索结果
   */
  unifiedSearch: (params: any) => defHttp.get({ url: '/taolink/search/unified', params }),

  /**
   * 搜索结果转 source_offer 落库
   * @param data 搜索结果
   * @returns 导入结果
   */
  importToSourceOffer: (data: any) => defHttp.post({ url: '/taolink/search/import-to-source-offer', data }),

  /**
   * 获取缓存统计
   * @returns 统计数据
   */
  getCacheStats: () => defHttp.get({ url: '/taolink/search/stats' }),

  /**
   * 清除过期缓存
   * @returns 清除结果
   */
  clearExpiredCache: () => defHttp.delete({ url: '/taolink/search/clear-expired' }),
};
