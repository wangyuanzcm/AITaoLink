import { defineStore } from 'pinia';

/**
 * TaoLink 店铺状态管理
 */
export const useTaolinkStore = defineStore('taolink', {
  state: () => ({
    // 当前选中的店铺ID
    currentShopId: localStorage.getItem('taolink_current_shop_id') || '',
    // 店铺列表
    shopList: [],
    // 加载状态
    loading: false,
  }),

  getters: {
    /**
     * 是否有选中的店铺
     */
    hasSelectedShop: (state) => !!state.currentShopId,

    /**
     * 当前选中的店铺
     */
    currentShop: (state) => {
      if (!state.currentShopId) return null;
      return state.shopList.find((shop: any) => shop.id === state.currentShopId);
    },
  },

  actions: {
    /**
     * 设置当前店铺ID
     * @param shopId 店铺ID
     */
    setCurrentShopId(shopId: string) {
      this.currentShopId = shopId;
      localStorage.setItem('taolink_current_shop_id', shopId);
    },

    /**
     * 清除当前店铺ID
     */
    clearCurrentShopId() {
      this.currentShopId = '';
      localStorage.removeItem('taolink_current_shop_id');
    },

    /**
     * 设置店铺列表
     * @param shops 店铺列表
     */
    setShopList(shops: any[]) {
      this.shopList = shops;
    },

    /**
     * 添加店铺
     * @param shop 店铺信息
     */
    addShop(shop: any) {
      this.shopList.push(shop);
    },

    /**
     * 更新店铺信息
     * @param shop 店铺信息
     */
    updateShop(shop: any) {
      const index = this.shopList.findIndex((item: any) => item.id === shop.id);
      if (index !== -1) {
        this.shopList[index] = shop;
      }
    },

    /**
     * 删除店铺
     * @param shopId 店铺ID
     */
    removeShop(shopId: string) {
      this.shopList = this.shopList.filter((shop: any) => shop.id !== shopId);
      if (this.currentShopId === shopId) {
        this.clearCurrentShopId();
      }
    },

    /**
     * 设置加载状态
     * @param loading 加载状态
     */
    setLoading(loading: boolean) {
      this.loading = loading;
    },
  },
});
