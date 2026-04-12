import type { AppRouteModule } from '/@/router/types';
import { LAYOUT } from '/@/router/constant';
import { t } from '/@/hooks/web/useI18n';

const taolink: AppRouteModule = {
  path: '/taolink',
  name: 'TaoLink',
  component: LAYOUT,
  redirect: '/taolink/shop',
  meta: {
    orderNo: 10,
    title: 'TaoLink',
    icon: 'icon-shopping-cart',
    hideChildrenInMenu: false,
  },
  children: [
    {
      path: 'shop',
      name: 'TaoLinkShop',
      component: () => import('/@/views/taolink/shop/index.vue'),
      meta: {
        title: '商铺管理',
        icon: 'icon-shop',
      },
    },
    {
      path: 'product',
      name: 'TaoLinkProduct',
      component: () => import('/@/views/taolink/product/index.vue'),
      meta: {
        title: '商品管理',
        icon: 'icon-goods',
      },
    },
    {
      path: 'order',
      name: 'TaoLinkOrder',
      component: () => import('/@/views/taolink/order/index.vue'),
      meta: {
        title: '订单管理',
        icon: 'icon-order',
      },
    },
    {
      path: 'inventory',
      name: 'TaoLinkInventory',
      component: () => import('/@/views/taolink/inventory/index.vue'),
      meta: {
        title: '库存管理',
        icon: 'icon-inventory',
      },
    },
    {
      path: 'inventory-analysis',
      name: 'TaoLinkInventoryAnalysis',
      component: () => import('/@/views/taolink/inventory-analysis/index.vue'),
      meta: {
        title: '库存分析',
        icon: 'icon-chart',
      },
    },
    {
      path: 'purchase',
      name: 'TaoLinkPurchase',
      component: () => import('/@/views/taolink/purchase/index.vue'),
      meta: {
        title: '采购管理',
        icon: 'icon-purchase',
      },
    },
    {
      path: 'settlement',
      name: 'TaoLinkSettlement',
      component: () => import('/@/views/taolink/settlement/index.vue'),
      meta: {
        title: '结算管理',
        icon: 'icon-settlement',
      },
    },
    {
      path: 'search',
      name: 'TaoLinkSearch',
      component: () => import('/@/views/taolink/search/index.vue'),
      meta: {
        title: '搜索中心',
        icon: 'icon-search',
      },
    },
    {
      path: 'monitor',
      name: 'TaoLinkMonitor',
      component: () => import('/@/views/taolink/monitor/index.vue'),
      meta: {
        title: '店铺监控',
        icon: 'icon-monitor',
      },
    },
    {
      path: 'dashboard',
      name: 'TaoLinkDashboard',
      component: () => import('/@/views/taolink/dashboard/index.vue'),
      meta: {
        title: '全局看板',
        icon: 'icon-dashboard',
      },
    },
  ],
};

export default taolink;
