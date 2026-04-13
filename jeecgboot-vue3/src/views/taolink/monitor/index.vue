<template>
  <div class="taolink-monitor-overview">
    <a-card title="店铺监控概览" :bordered="false">
      <!-- 概览卡片 -->
      <div class="overview-cards">
        <a-card size="small" class="overview-card">
          <template #title>
            <span class="card-title">商品总数</span>
          </template>
          <div class="card-value">{{ overviewData.productCount || 0 }}</div>
        </a-card>
        <a-card size="small" class="overview-card">
          <template #title>
            <span class="card-title">在售商品</span>
          </template>
          <div class="card-value">{{ overviewData.listedCount || 0 }}</div>
        </a-card>
        <a-card size="small" class="overview-card">
          <template #title>
            <span class="card-title">当日订单</span>
          </template>
          <div class="card-value">{{ overviewData.orderCount || 0 }}</div>
        </a-card>
        <a-card size="small" class="overview-card">
          <template #title>
            <span class="card-title">当日GMV</span>
          </template>
          <div class="card-value">¥{{ (overviewData.orderAmount || 0) / 100 }}</div>
        </a-card>
        <a-card size="small" class="overview-card">
          <template #title>
            <span class="card-title">当日退款</span>
          </template>
          <div class="card-value">{{ overviewData.refundCount || 0 }}</div>
        </a-card>
        <a-card size="small" class="overview-card">
          <template #title>
            <span class="card-title">有货SKU</span>
          </template>
          <div class="card-value">{{ overviewData.inventoryItemCount || 0 }}</div>
        </a-card>
      </div>

      <!-- 趋势图表 -->
      <div class="trend-chart">
        <a-card title="店铺趋势（近7天）" :bordered="false">
          <a-spin :spinning="loading" tip="加载中...">
            <div ref="chartRef" class="chart-container"></div>
          </a-spin>
        </a-card>
      </div>

      <!-- 商品排行榜 -->
      <div class="rankings">
        <a-card title="商品排行榜" :bordered="false">
          <a-spin :spinning="loading" tip="加载中...">
            <a-table
              :columns="rankingColumns"
              :data-source="rankings"
              row-key="productId"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'rank'">
                  <span class="rank-number">{{ record.rank }}</span>
                </template>
              </template>
            </a-table>
          </a-spin>
        </a-card>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import * as echarts from 'echarts';
import { useTaolinkStore } from '/@/store/modules/taolink';

defineOptions({ name: 'TaolinkMonitorIndex' });

const taolink = useTaolinkStore();

// 响应式数据
interface OverviewData {
  productCount: number;
  listedCount: number;
  orderCount: number;
  orderAmount: number;
  refundCount: number;
  inventoryItemCount: number;
}

interface TrendItem {
  snapshotDate: string;
  productCount: number;
  listedCount: number;
  orderCount: number;
  orderAmount: number;
}

interface RankingItem {
  rank: number;
  productId: string;
  productName: string;
  sales: number;
  amount: number;
  inventory: number;
}

const overviewData = ref<Partial<OverviewData>>({});
const trendData = ref<TrendItem[]>([]);
const rankings = ref<RankingItem[]>([]);
const loading = ref(false);
const chartRef = ref<HTMLDivElement | null>(null);
let chart: echarts.ECharts | null = null;

// 表格列定义
const rankingColumns = [
  {
    title: '排名',
    dataIndex: 'rank',
    key: 'rank',
    width: 80,
  },
  {
    title: '商品名称',
    dataIndex: 'productName',
    key: 'productName',
  },
  {
    title: '销量',
    dataIndex: 'sales',
    key: 'sales',
    width: 100,
  },
  {
    title: '销售额',
    dataIndex: 'amount',
    key: 'amount',
    width: 120,
    customRender: ({ record }) => `¥${record.amount || 0}`,
  },
  {
    title: '库存',
    dataIndex: 'inventory',
    key: 'inventory',
    width: 100,
  },
];

// 获取店铺概览数据
const getShopOverview = async () => {
  const shopId = taolink.currentShopId;
  if (!shopId) {
    message.warning('请先选择店铺');
    return;
  }

  loading.value = true;
  try {
    // 模拟接口调用
    // 实际项目中应该调用真实的 API
    // const response = await taolinkApi.getShopOverview(shopId);
    // overviewData.value = response.data;
    
    // 模拟数据
    overviewData.value = {
      productCount: 156,
      listedCount: 142,
      orderCount: 23,
      orderAmount: 125800,
      refundCount: 2,
      inventoryItemCount: 138,
    };
  } catch (error) {
    message.error('获取店铺概览失败');
    console.error('获取店铺概览失败:', error);
  } finally {
    loading.value = false;
  }
};

// 获取店铺趋势数据
const getShopTrend = async () => {
  const shopId = taolink.currentShopId;
  if (!shopId) return;

  try {
    // 模拟接口调用
    // const response = await taolinkApi.getShopTrend(shopId, 7);
    // trendData.value = response.data;
    
    // 模拟数据
    trendData.value = [
      { snapshotDate: '2026-04-06', productCount: 150, listedCount: 135, orderCount: 18, orderAmount: 98000 },
      { snapshotDate: '2026-04-07', productCount: 152, listedCount: 137, orderCount: 21, orderAmount: 105000 },
      { snapshotDate: '2026-04-08', productCount: 153, listedCount: 138, orderCount: 19, orderAmount: 102000 },
      { snapshotDate: '2026-04-09', productCount: 154, listedCount: 139, orderCount: 25, orderAmount: 130000 },
      { snapshotDate: '2026-04-10', productCount: 155, listedCount: 140, orderCount: 22, orderAmount: 115000 },
      { snapshotDate: '2026-04-11', productCount: 156, listedCount: 141, orderCount: 20, orderAmount: 108000 },
      { snapshotDate: '2026-04-12', productCount: 156, listedCount: 142, orderCount: 23, orderAmount: 125800 },
    ];
    
    renderTrendChart();
  } catch (error) {
    console.error('获取店铺趋势数据失败:', error);
  }
};

// 获取商品排行榜
const getShopRankings = async () => {
  const shopId = taolink.currentShopId;
  if (!shopId) return;

  try {
    // 模拟接口调用
    // const response = await taolinkApi.getShopRankings(shopId);
    // rankings.value = response.data;
    
    // 模拟数据
    rankings.value = [
      { rank: 1, productId: '1', productName: '智能手表', sales: 120, amount: 12000, inventory: 50 },
      { rank: 2, productId: '2', productName: '无线耳机', sales: 95, amount: 9500, inventory: 35 },
      { rank: 3, productId: '3', productName: '蓝牙音箱', sales: 88, amount: 8800, inventory: 42 },
      { rank: 4, productId: '4', productName: '运动手环', sales: 76, amount: 7600, inventory: 28 },
      { rank: 5, productId: '5', productName: '充电宝', sales: 65, amount: 6500, inventory: 60 },
    ];
  } catch (error) {
    console.error('获取商品排行榜失败:', error);
  }
};

// 渲染趋势图表
const renderTrendChart = () => {
  if (!chartRef.value) return;
  
  if (chart) {
    chart.dispose();
  }
  
  chart = echarts.init(chartRef.value);
  
  const dates = trendData.value.map(item => item.snapshotDate);
  const orderCounts = trendData.value.map(item => item.orderCount);
  const orderAmounts = trendData.value.map(item => item.orderAmount / 100);
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      data: ['订单数', 'GMV(元)']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '订单数',
        position: 'left'
      },
      {
        type: 'value',
        name: 'GMV(元)',
        position: 'right'
      }
    ],
    series: [
      {
        name: '订单数',
        type: 'line',
        stack: 'Total',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        data: orderCounts
      },
      {
        name: 'GMV(元)',
        type: 'line',
        yAxisIndex: 1,
        stack: 'Total',
        areaStyle: {},
        emphasis: {
          focus: 'series'
        },
        data: orderAmounts
      }
    ]
  };
  
  chart.setOption(option);
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chart?.resize();
  });
};

// 生命周期
onMounted(() => {
  getShopOverview();
  getShopTrend();
  getShopRankings();
});
</script>

<style scoped>
.taolink-monitor-overview {
  padding: 16px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.overview-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-title {
  font-size: 14px;
  color: #666;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
  margin-top: 8px;
}

.trend-chart {
  margin-bottom: 24px;
}

.chart-container {
  height: 400px;
  width: 100%;
}

.rankings {
  margin-top: 24px;
}

.rank-number {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 50%;
  background-color: #1890ff;
  color: white;
  font-size: 12px;
  font-weight: bold;
}
</style>
