<template>
  <div class="taolink-inventory-health">
    <!-- 页面标题 -->
    <div class="page-header">
      <a-page-header
        title="全局库存健康度"
        sub-title="跨店库存健康状态分析"
      />
    </div>

    <!-- 数据加载状态 -->
    <a-spin :spinning="loading">
      <!-- 健康度概览 -->
      <div class="health-overview">
        <a-card class="health-card">
          <div class="health-content">
            <div class="health-score-section">
              <div class="score-circle">
                <div class="score-value">{{ healthData.healthScore ? healthData.healthScore.toFixed(0) : 0 }}</div>
                <div class="score-unit">%</div>
                <div class="score-label">库存健康度</div>
              </div>
            </div>
            <div class="health-metrics">
              <div class="metric-item">
                <div class="metric-label">库存周转率</div>
                <div class="metric-value">{{ healthData.turnoverRate ? healthData.turnoverRate.toFixed(2) : 0 }}</div>
              </div>
              <div class="metric-item">
                <div class="metric-label">库存价值</div>
                <div class="metric-value">¥{{ (healthData.inventoryValue || 0) / 100 }}</div>
              </div>
              <div class="metric-item">
                <div class="metric-label">总SKU数</div>
                <div class="metric-value">{{ totalSkuCount }}</div>
              </div>
            </div>
          </div>
        </a-card>
      </div>

      <!-- 库存状态分布 -->
      <div class="inventory-status">
        <a-row :gutter="[16, 16]">
          <!-- 饼图 -->
          <a-col :span="12">
            <a-card title="库存状态分布">
              <div class="chart-container">
                <div ref="pieChartRef" style="width: 100%; height: 300px"></div>
              </div>
            </a-card>
          </a-col>
          <!-- 柱状图 -->
          <a-col :span="12">
            <a-card title="库存状态数量">
              <div class="chart-container">
                <div ref="barChartRef" style="width: 100%; height: 300px"></div>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </div>

      <!-- 库存状态详情 -->
      <div class="inventory-details">
        <a-card title="库存状态详情">
          <a-table
            :data-source="inventoryStatusList"
            :columns="statusColumns"
            :pagination="false"
            row-key="status"
          />
        </a-card>
      </div>

      <!-- 健康度趋势 -->
      <div class="health-trend">
        <a-card title="健康度趋势">
          <div class="chart-container">
            <div ref="trendChartRef" style="width: 100%; height: 400px"></div>
          </div>
        </a-card>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import type { EChartsOption } from 'echarts';
import type { Ref } from 'vue';
import { ref, computed, onMounted, h, watchEffect } from 'vue';
import { Button, Tag } from 'ant-design-vue';
import { useECharts } from '/@/hooks/web/useECharts';

// 数据状态
const loading = ref(false);
const healthData = ref({
  healthScore: 85.5,
  turnoverRate: 3.2,
  inventoryValue: 2500000,
  statusCounts: {
    normal: 2200,
    lowStock: 120,
    overstock: 85,
    outOfStock: 95
  }
});

// 健康度趋势数据
const healthTrendData = ref([
  { date: '2026-03-13', score: 82.5 },
  { date: '2026-03-14', score: 83.0 },
  { date: '2026-03-15', score: 82.8 },
  { date: '2026-03-16', score: 83.5 },
  { date: '2026-03-17', score: 84.0 },
  { date: '2026-03-18', score: 84.2 },
  { date: '2026-03-19', score: 84.5 },
  { date: '2026-03-20', score: 84.8 },
  { date: '2026-03-21', score: 85.0 },
  { date: '2026-03-22', score: 85.2 },
  { date: '2026-03-23', score: 85.3 },
  { date: '2026-03-24', score: 85.5 },
  { date: '2026-03-25', score: 85.4 },
  { date: '2026-03-26', score: 85.5 },
  { date: '2026-03-27', score: 85.6 },
  { date: '2026-03-28', score: 85.7 },
  { date: '2026-03-29', score: 85.8 },
  { date: '2026-03-30', score: 85.9 },
  { date: '2026-03-31', score: 86.0 },
  { date: '2026-04-01', score: 86.1 },
  { date: '2026-04-02', score: 86.2 },
  { date: '2026-04-03', score: 86.3 },
  { date: '2026-04-04', score: 86.2 },
  { date: '2026-04-05', score: 86.1 },
  { date: '2026-04-06', score: 86.0 },
  { date: '2026-04-07', score: 85.9 },
  { date: '2026-04-08', score: 85.8 },
  { date: '2026-04-09', score: 85.7 },
  { date: '2026-04-10', score: 85.6 },
  { date: '2026-04-11', score: 85.5 }
]);

// 总SKU数
const totalSkuCount = computed(() => {
  const statusCounts = healthData.value.statusCounts || {};
  return Object.values(statusCounts).reduce((total, count) => total + count, 0);
});

// 库存状态列表
const inventoryStatusList = computed(() => {
  const statusMap = {
    normal: { label: '正常库存', color: 'green' },
    lowStock: { label: '低库存', color: 'orange' },
    overstock: { label: '积压库存', color: 'red' },
    outOfStock: { label: '缺货', color: 'gray' }
  };
  
  return Object.entries(healthData.value.statusCounts || {}).map(([status, count]) => {
    const statusInfo = statusMap[status] || { label: status, color: 'default' };
    const percentage = totalSkuCount.value > 0 ? (count / totalSkuCount.value * 100).toFixed(2) : '0.00';
    return {
      status,
      label: statusInfo.label,
      count,
      percentage,
      color: statusInfo.color
    };
  });
});

// 表格列配置
const statusColumns = [
  {
    title: '库存状态',
    dataIndex: 'label',
    key: 'label',
    customRender: ({ record }) => h(Tag, { color: record.color }, () => record.label),
  },
  {
    title: '数量',
    dataIndex: 'count',
    key: 'count'
  },
  {
    title: '占比',
    dataIndex: 'percentage',
    key: 'percentage',
    customRender: ({ text }) => `${text}%`,
  },
  {
    title: '操作',
    key: 'action',
    customRender: () => h(Button, { type: 'link', size: 'small' }, () => '查看详情'),
  }
];

const pieChartRef = ref<HTMLDivElement | null>(null);
const barChartRef = ref<HTMLDivElement | null>(null);
const trendChartRef = ref<HTMLDivElement | null>(null);

const { setOptions: setPieOptions } = useECharts(pieChartRef as Ref<HTMLDivElement>);
const { setOptions: setBarOptions } = useECharts(barChartRef as Ref<HTMLDivElement>);
const { setOptions: setTrendOptions } = useECharts(trendChartRef as Ref<HTMLDivElement>);

// 饼图配置
const pieChartOption = computed<EChartsOption>(() => {
  const data = inventoryStatusList.value.map(item => ({
    name: item.label,
    value: item.count
  }));
  
  return {
    title: {
      text: '库存状态分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '库存状态',
        type: 'pie',
        radius: '60%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };
});

// 柱状图配置
const barChartOption = computed<EChartsOption>(() => {
  const data = inventoryStatusList.value;
  const statusNames = data.map(item => item.label);
  const counts = data.map(item => item.count);
  
  return {
    title: {
      text: '库存状态数量',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: statusNames
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '数量',
        type: 'bar',
        data: counts,
        itemStyle: {
          color: function(params) {
            const colors = ['#52c41a', '#faad14', '#f5222d', '#d9d9d9'];
            return colors[params.dataIndex];
          }
        },
        label: {
          show: true,
          position: 'top'
        }
      }
    ]
  };
});

// 健康度趋势图表配置
const trendChartOption = computed<EChartsOption>(() => {
  const dates = healthTrendData.value.map(item => item.date);
  const scores = healthTrendData.value.map(item => item.score);
  
  return {
    title: {
      text: '近30天库存健康度趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
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
    yAxis: {
      type: 'value',
      min: 80,
      max: 90,
      name: '健康度 (%)'
    },
    series: [
      {
        name: '健康度',
        type: 'line',
        data: scores,
        smooth: true,
        lineStyle: {
          width: 2,
          color: '#1890ff'
        },
        areaStyle: {
          color: 'rgba(24, 144, 255, 0.2)',
        }
      }
    ]
  };
});

watchEffect(() => {
  if (!pieChartRef.value) return;
  setPieOptions(pieChartOption.value);
});

watchEffect(() => {
  if (!barChartRef.value) return;
  setBarOptions(barChartOption.value);
});

watchEffect(() => {
  if (!trendChartRef.value) return;
  setTrendOptions(trendChartOption.value);
});

// 初始化数据
const initData = async () => {
  loading.value = true;
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500));
    // 实际项目中这里会调用API获取真实数据
  } catch (error) {
    console.error('获取库存健康度数据失败:', error);
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
.taolink-inventory-health {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.health-overview {
  margin-bottom: 20px;
}

.health-card {
  height: 200px;
}

.health-content {
  display: flex;
  align-items: center;
  height: 180px;
}

.health-score-section {
  flex: 1;
  display: flex;
  justify-content: center;
}

.score-circle {
  width: 150px;
  height: 150px;
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
  font-size: 36px;
  font-weight: bold;
}

.score-unit {
  font-size: 16px;
  margin-top: 4px;
}

.score-label {
  font-size: 14px;
  margin-top: 8px;
  opacity: 0.9;
}

.health-metrics {
  flex: 2;
  display: flex;
  justify-content: space-around;
  padding-left: 40px;
}

.metric-item {
  text-align: center;
}

.metric-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.inventory-status {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.inventory-details {
  margin-bottom: 20px;
}

.health-trend {
  margin-bottom: 20px;
}

.health-trend .chart-container {
  height: 400px;
}
</style>
