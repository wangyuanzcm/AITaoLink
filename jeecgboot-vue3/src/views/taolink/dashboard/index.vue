<template>
  <div class="taolink-dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <a-page-header
        title="全局分析看板"
        sub-title="跨店铺数据总览与分析"
      />
    </div>

    <!-- 数据加载状态 -->
    <a-spin :spinning="loading">
      <!-- 关键指标卡片 -->
      <div class="metrics-cards">
        <a-row :gutter="[16, 16]">
          <a-col :span="6">
            <a-card class="metric-card" hoverable>
              <div class="metric-content">
                <div class="metric-value">{{ overviewData.shopCount || 0 }}</div>
                <div class="metric-label">店铺数量</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="metric-card" hoverable>
              <div class="metric-content">
                <div class="metric-value">{{ overviewData.productCount || 0 }}</div>
                <div class="metric-label">商品总数</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="metric-card" hoverable>
              <div class="metric-content">
                <div class="metric-value">{{ overviewData.todayOrderCount || 0 }}</div>
                <div class="metric-label">今日订单</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="metric-card" hoverable>
              <div class="metric-content">
                <div class="metric-value">¥{{ (overviewData.todayGmv || 0) / 100 }}</div>
                <div class="metric-label">今日GMV</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="metric-card" hoverable>
              <div class="metric-content">
                <div class="metric-value">{{ overviewData.inventorySkuCount || 0 }}</div>
                <div class="metric-label">库存SKU数</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="metric-card" hoverable>
              <div class="metric-content">
                <div class="metric-value">{{ overviewData.lowStockSkuCount || 0 }}</div>
                <div class="metric-label">低库存SKU</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="metric-card" hoverable>
              <div class="metric-content">
                <div class="metric-value">{{ overviewData.overstockSkuCount || 0 }}</div>
                <div class="metric-label">积压库存SKU</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="metric-card" hoverable>
              <div class="metric-content">
                <div class="metric-value">{{ healthData.healthScore ? healthData.healthScore.toFixed(1) : 0 }}%</div>
                <div class="metric-label">库存健康度</div>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </div>

      <!-- 趋势图表 -->
      <div class="charts-section">
        <a-card title="全局趋势" class="chart-card">
          <div class="chart-container">
            <v-chart
              :option="trendChartOption"
              style="width: 100%; height: 400px"
            />
          </div>
        </a-card>
      </div>

      <!-- 库存健康度和预警汇总 -->
      <div class="health-alerts-section">
        <a-row :gutter="[16, 16]">
          <!-- 库存健康度 -->
          <a-col :span="12">
            <a-card title="库存健康度" class="health-card">
              <div class="health-content">
                <div class="health-score">
                  <div class="score-circle">
                    <div class="score-value">{{ healthData.healthScore ? healthData.healthScore.toFixed(0) : 0 }}</div>
                    <div class="score-unit">%</div>
                  </div>
                </div>
                <div class="health-details">
                  <div class="health-item">
                    <span class="health-label">库存周转率</span>
                    <span class="health-value">{{ healthData.turnoverRate ? healthData.turnoverRate.toFixed(2) : 0 }}</span>
                  </div>
                  <div class="health-item">
                    <span class="health-label">库存价值</span>
                    <span class="health-value">¥{{ (healthData.inventoryValue || 0) / 100 }}</span>
                  </div>
                  <div class="health-item" v-for="(count, status) in healthData.statusCounts" :key="status">
                    <span class="health-label">{{ getStatusLabel(status) }}</span>
                    <span class="health-value">{{ count }}</span>
                  </div>
                </div>
              </div>
            </a-card>
          </a-col>

          <!-- 预警汇总 -->
          <a-col :span="12">
            <a-card title="预警汇总" class="alerts-card">
              <div class="alerts-content">
                <div class="alert-item" v-for="(count, type) in alertsData" :key="type" v-if="type !== 'alertDetails'">
                  <div class="alert-type">{{ getAlertTypeLabel(type) }}</div>
                  <div class="alert-count">{{ count }}</div>
                </div>
                <div class="alert-more">
                  <a-button type="link" @click="goToAlerts">查看详情</a-button>
                </div>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </div>

      <!-- 店铺排行 -->
      <div class="ranking-section">
        <a-card title="店铺排行" class="ranking-card">
          <a-table
            :data-source="shopRanking"
            :columns="rankingColumns"
            :pagination="false"
            row-key="shopId"
          />
        </a-card>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { VChart } from '@visactor/vue3-vchart';
import { BarChart, LineChart, Point, Title, Tooltip, Legend, Axis } from '@visactor/vchart';

// 注册 VChart 组件
VChart.use([BarChart, LineChart, Point, Title, Tooltip, Legend, Axis]);

const router = useRouter();

// 数据状态
const loading = ref(false);
const overviewData = ref({});
const healthData = ref({});
const alertsData = ref({});
const shopRanking = ref([]);
const trendData = ref([]);

// 排行表格列
const rankingColumns = [
  {
    title: '排名',
    dataIndex: 'rank',
    key: 'rank',
    width: 80,
    customRender: ({ index }) => index + 1
  },
  {
    title: '店铺名称',
    dataIndex: 'shopName',
    key: 'shopName'
  },
  {
    title: '订单数',
    dataIndex: 'orderCount',
    key: 'orderCount'
  },
  {
    title: 'GMV',
    dataIndex: 'gmv',
    key: 'gmv',
    customRender: (text) => `¥${(text / 100).toFixed(2)}`
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    customRender: (text) => {
      const statusMap = {
        'active': '正常',
        'inactive': '禁用'
      };
      return statusMap[text] || text;
    }
  }
];

// 趋势图表配置
const trendChartOption = computed(() => {
  const dates = trendData.value.map(item => item.date);
  const orderCounts = trendData.value.map(item => item.orderCount);
  const gmvValues = trendData.value.map(item => item.gmv / 100); // 转换为元

  return {
    title: {
      text: '近30天趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['订单数', 'GMV'],
      top: 30
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
        name: 'GMV (元)',
        position: 'right'
      }
    ],
    series: [
      {
        name: '订单数',
        type: 'line',
        data: orderCounts,
        smooth: true,
        lineStyle: {
          width: 2
        }
      },
      {
        name: 'GMV',
        type: 'line',
        yAxisIndex: 1,
        data: gmvValues,
        smooth: true,
        lineStyle: {
          width: 2
        }
      }
    ]
  };
});

// 状态标签映射
const getStatusLabel = (status) => {
  const statusMap = {
    'normal': '正常库存',
    'lowStock': '低库存',
    'overstock': '积压库存',
    'outOfStock': '缺货'
  };
  return statusMap[status] || status;
};

// 预警类型标签映射
const getAlertTypeLabel = (type) => {
  const typeMap = {
    'lowStockAlerts': '低库存预警',
    'overstockAlerts': '积压库存预警',
    'orderAlerts': '订单异常预警',
    'refundAlerts': '退款异常预警'
  };
  return typeMap[type] || type;
};

// 跳转到预警详情页
const goToAlerts = () => {
  router.push('/taolink/dashboard/alerts');
};

// 模拟数据
const mockOverviewData = {
  shopCount: 5,
  productCount: 1200,
  listedProductCount: 850,
  todayOrderCount: 128,
  todayGmv: 580000,
  inventorySkuCount: 2500,
  lowStockSkuCount: 120,
  overstockSkuCount: 85
};

const mockHealthData = {
  healthScore: 85.5,
  turnoverRate: 3.2,
  inventoryValue: 2500000,
  statusCounts: {
    normal: 2200,
    lowStock: 120,
    overstock: 85,
    outOfStock: 95
  }
};

const mockAlertsData = {
  lowStockAlerts: 120,
  overstockAlerts: 85,
  orderAlerts: 12,
  refundAlerts: 8
};

const mockShopRanking = [
  { shopId: '1', shopName: '淘宝旗舰店', orderCount: 1200, gmv: 6000000, status: 'active' },
  { shopId: '2', shopName: '京东专卖店', orderCount: 950, gmv: 4800000, status: 'active' },
  { shopId: '3', shopName: '拼多多店', orderCount: 1500, gmv: 3500000, status: 'active' },
  { shopId: '4', shopName: '抖音小店', orderCount: 800, gmv: 2800000, status: 'active' },
  { shopId: '5', shopName: '小红书店', orderCount: 450, gmv: 1500000, status: 'inactive' }
];

const mockTrendData = [];
const today = new Date();
for (let i = 29; i >= 0; i--) {
  const date = new Date(today);
  date.setDate(date.getDate() - i);
  const dateStr = date.toISOString().split('T')[0];
  mockTrendData.push({
    date: dateStr,
    orderCount: Math.floor(Math.random() * 100) + 50,
    gmv: Math.floor(Math.random() * 300000) + 200000
  });
}

// 初始化数据
const initData = async () => {
  loading.value = true;
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // 使用模拟数据
    overviewData.value = mockOverviewData;
    healthData.value = mockHealthData;
    alertsData.value = mockAlertsData;
    shopRanking.value = mockShopRanking;
    trendData.value = mockTrendData;
  } catch (error) {
    console.error('获取数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 页面挂载时初始化数据
onMounted(() => {
  initData();
});
</script>

<style scoped>
.taolink-dashboard {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.metrics-cards {
  margin-bottom: 20px;
}

.metric-card {
  height: 120px;
}

.metric-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 8px;
}

.metric-label {
  font-size: 14px;
  color: #666;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  height: 450px;
}

.chart-container {
  height: 400px;
}

.health-alerts-section {
  margin-bottom: 20px;
}

.health-card, .alerts-card {
  height: 300px;
}

.health-content {
  display: flex;
  align-items: center;
  height: 250px;
}

.health-score {
  flex: 1;
  display: flex;
  justify-content: center;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background-color: #1890ff;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.score-value {
  font-size: 32px;
  font-weight: bold;
}

.score-unit {
  font-size: 14px;
  margin-top: 4px;
}

.health-details {
  flex: 1;
  padding-left: 20px;
}

.health-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.health-label {
  font-size: 14px;
  color: #666;
}

.health-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.alerts-content {
  height: 250px;
  display: flex;
  flex-direction: column;
}

.alert-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.alert-type {
  font-size: 14px;
  color: #666;
}

.alert-count {
  font-size: 16px;
  font-weight: 500;
  color: #ff4d4f;
}

.alert-more {
  margin-top: auto;
  text-align: right;
  padding-top: 12px;
}

.ranking-section {
  margin-bottom: 20px;
}

.ranking-card {
  min-height: 300px;
}
</style>
