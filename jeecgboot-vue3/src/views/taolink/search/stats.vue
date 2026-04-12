<template>
  <div class="cache-stats">
    <a-card title="缓存统计面板" :bordered="false">
      <!-- 统计卡片 -->
      <div class="stats-cards">
        <a-row :gutter="[16, 16]">
          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <a-statistic title="总缓存数" :value="stats.totalCache || 0" />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <a-statistic title="今日新增" :value="stats.todayCache || 0" />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <a-statistic title="总命中次数" :value="stats.totalHits || 0" />
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card :bordered="false" class="stat-card">
              <a-statistic title="过期缓存" :value="stats.expiredCache || 0" />
            </a-card>
          </a-col>
        </a-row>
      </div>

      <!-- 命中率图表 -->
      <div class="chart-section" style="margin-top: 24px">
        <a-card title="缓存命中率" :bordered="false">
          <div ref="chartRef" style="height: 300px"></div>
        </a-card>
      </div>

      <!-- 操作按钮 -->
      <div class="actions" style="margin-top: 24px">
        <a-button type="primary" @click="handleClearExpired">
          <a-icon type="delete" />
          清除过期缓存
        </a-button>
        <a-button style="margin-left: 8px" @click="loadStats">
          <a-icon type="reload" />
          刷新数据
        </a-button>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { searchApi } from '@/api/taolink/search';
import { message } from 'ant-design-vue';
import * as echarts from 'echarts';

// 统计数据
const stats = ref({
  totalCache: 0,
  todayCache: 0,
  totalHits: 0,
  expiredCache: 0,
});

// 图表引用
const chartRef = ref<HTMLElement>();
// 图表实例
let chart: echarts.ECharts | null = null;

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await searchApi.getCacheStats();
    if (res.success) {
      stats.value = res.result;
      updateChart();
    } else {
      message.error('获取统计数据失败');
    }
  } catch (error) {
    message.error('获取统计数据失败');
  }
};

// 清除过期缓存
const handleClearExpired = async () => {
  try {
    const res = await searchApi.clearExpiredCache();
    if (res.success) {
      message.success(res.message);
      loadStats();
    } else {
      message.error('清除失败：' + res.message);
    }
  } catch (error) {
    message.error('清除失败');
  }
};

// 更新图表
const updateChart = () => {
  if (!chartRef.value) return;

  if (!chart) {
    chart = echarts.init(chartRef.value);
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['命中', '未命中'],
    },
    series: [
      {
        name: '缓存命中',
        type: 'pie',
        radius: '50%',
        data: [
          { value: stats.value.totalHits || 0, name: '命中' },
          { value: (stats.value.totalCache || 0) - (stats.value.totalHits || 0), name: '未命中' },
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  };

  chart.setOption(option);
};

// 监听窗口大小变化
const handleResize = () => {
  if (chart) {
    chart.resize();
  }
};

// 初始化
onMounted(() => {
  loadStats();
  window.addEventListener('resize', handleResize);
});

// 组件卸载时清理
onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  if (chart) {
    chart.dispose();
  }
});
</script>

<style scoped>
.cache-stats {
  padding: 20px;
}

.stat-card {
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.actions {
  text-align: center;
}
</style>
