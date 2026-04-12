<template>
  <div class="inventory-analysis">
    <a-card title="库存分析看板" :bordered="false">
      <!-- 库存概览指标 -->
      <div class="overview-card">
        <a-row :gutter="[16, 16]">
          <a-col :span="6">
            <a-statistic-card
              title="总SKU数"
              :value="overviewData.totalSkus || 0"
              :value-style="{ color: '#1890ff' }"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic-card
              title="低库存SKU数"
              :value="overviewData.lowStockSkus || 0"
              :value-style="{ color: '#f5222d' }"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic-card
              title="积压库存SKU数"
              :value="overviewData.overstockSkus || 0"
              :value-style="{ color: '#faad14' }"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic-card
              title="总库存价值"
              :value="overviewData.totalValue || 0"
              suffix="元"
              :value-style="{ color: '#52c41a' }"
            />
          </a-col>
        </a-row>
      </div>

      <!-- 库存健康度指标 -->
      <div class="health-card">
        <a-row :gutter="[16, 16]">
          <a-col :span="8">
            <a-statistic-card
              title="库存周转率"
              :value="metricsData.turnoverRate || 0"
              suffix="次/年"
              :value-style="{ color: '#1890ff' }"
            />
          </a-col>
          <a-col :span="8">
            <a-statistic-card
              title="呆滞品比例"
              :value="(metricsData.stagnantRatio || 0) * 100"
              suffix="%"
              :value-style="{ color: '#faad14' }"
            />
          </a-col>
          <a-col :span="8">
            <a-statistic-card
              title="库存健康度"
              :value="metricsData.healthScore || 0"
              suffix="分"
              :value-style="{ color: '#52c41a' }"
            />
          </a-col>
        </a-row>
      </div>

      <!-- 预警列表 -->
      <div class="alert-card">
        <a-card title="库存预警" :bordered="false">
          <a-table
            :columns="alertColumns"
            :data-source="alertList"
            :loading="loading"
            :pagination="alertPagination"
            @change="handleAlertTableChange"
          >
            <!-- 操作列 -->
            <template #column:action="{ record }">
              <a-space size="small">
                <a-button type="primary" @click="handleResolveAlert(record)">
                  确认解决
                </a-button>
              </a-space>
            </template>

            <!-- 预警类型列 -->
            <template #column:alertType="{ record }">
              <a-tag :color="record.alertType === 'low_stock' ? 'red' : 'orange'">
                {{ record.alertType === 'low_stock' ? '低库存' : '积压库存' }}
              </a-tag>
            </template>

            <!-- 状态列 -->
            <template #column:status="{ record }">
              <a-tag :color="record.status === 'open' ? 'red' : 'green'">
                {{ record.status === 'open' ? '未解决' : '已解决' }}
              </a-tag>
            </template>
          </a-table>
        </a-card>
      </div>

      <!-- 库存趋势图表 -->
      <div class="chart-card">
        <a-card title="库存趋势" :bordered="false">
          <div style="height: 400px">
            <a-chart
              :data="chartData"
              :x-field="'date'"
              :y-field="'value'"
              :series-field="'type'"
              :color="['#1890ff', '#f5222d', '#52c41a']"
            >
              <a-line position="date*value" />
              <a-axis label="date" title="日期" />
              <a-axis label="value" title="数量" />
              <a-legend position="top" />
            </a-chart>
          </div>
        </a-card>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { inventoryApi } from '@/api/taolink/inventory';
import { message } from 'ant-design-vue';

// 库存概览数据
const overviewData = reactive({
  totalSkus: 0,
  lowStockSkus: 0,
  overstockSkus: 0,
  totalValue: 0,
});

// 库存健康度数据
const metricsData = reactive({
  turnoverRate: 0,
  stagnantRatio: 0,
  healthScore: 0,
});

// 预警列表数据
const alertList = ref<any[]>([]);
const loading = ref(false);
const alertPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 图表数据
const chartData = ref([
  { date: '2026-04-01', type: '在仓库存', value: 1000 },
  { date: '2026-04-02', type: '在仓库存', value: 1050 },
  { date: '2026-04-03', type: '在仓库存', value: 1100 },
  { date: '2026-04-04', type: '在仓库存', value: 1080 },
  { date: '2026-04-05', type: '在仓库存', value: 1150 },
  { date: '2026-04-01', type: '已预留', value: 200 },
  { date: '2026-04-02', type: '已预留', value: 220 },
  { date: '2026-04-03', type: '已预留', value: 250 },
  { date: '2026-04-04', type: '已预留', value: 230 },
  { date: '2026-04-05', type: '已预留', value: 260 },
  { date: '2026-04-01', type: '可用库存', value: 800 },
  { date: '2026-04-02', type: '可用库存', value: 830 },
  { date: '2026-04-03', type: '可用库存', value: 850 },
  { date: '2026-04-04', type: '可用库存', value: 850 },
  { date: '2026-04-05', type: '可用库存', value: 890 },
]);

// 预警列表列定义
const alertColumns = [
  {
    title: '预警ID',
    dataIndex: 'id',
    key: 'id',
  },
  {
    title: 'SKU ID',
    dataIndex: 'productSkuId',
    key: 'productSkuId',
  },
  {
    title: '预警类型',
    dataIndex: 'alertType',
    key: 'alertType',
    slots: { customRender: 'alertType' },
  },
  {
    title: '当前值',
    dataIndex: 'currentValue',
    key: 'currentValue',
  },
  {
    title: '预警阈值',
    dataIndex: 'threshold',
    key: 'threshold',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    slots: { customRender: 'status' },
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
  },
];

// 加载库存概览数据
const loadOverviewData = async () => {
  try {
    const res = await inventoryApi.getInventoryAnalysisOverview();
    if (res.success) {
      Object.assign(overviewData, res.result);
    }
  } catch (error) {
    console.error('加载库存概览数据失败', error);
  }
};

// 加载库存健康度数据
const loadMetricsData = async () => {
  try {
    const res = await inventoryApi.getInventoryAnalysisMetrics();
    if (res.success) {
      Object.assign(metricsData, res.result);
    }
  } catch (error) {
    console.error('加载库存健康度数据失败', error);
  }
};

// 加载预警列表
const loadAlertList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNo: alertPagination.value.current,
      pageSize: alertPagination.value.pageSize,
    };
    const res = await inventoryApi.getAlertList(params);
    if (res.success) {
      alertList.value = res.result.records || [];
      alertPagination.value.total = res.result.total || 0;
    }
  } catch (error) {
    message.error('加载预警列表失败');
  } finally {
    loading.value = false;
  }
};

// 预警列表分页
const handleAlertTableChange = (pagination: any) => {
  alertPagination.value = pagination;
  loadAlertList();
};

// 确认解决预警
const handleResolveAlert = async (record: any) => {
  try {
    // 这里可以从用户信息中获取处理人，暂时使用固定值
    const handler = 'admin';
    const res = await inventoryApi.resolveAlert(record.id, { handler });
    if (res.success) {
      message.success('预警已解决');
      loadAlertList();
    } else {
      message.error('解决预警失败');
    }
  } catch (error) {
    message.error('解决预警失败');
  }
};

// 初始化
onMounted(() => {
  loadOverviewData();
  loadMetricsData();
  loadAlertList();
});
</script>

<style scoped>
.inventory-analysis {
  padding: 20px;
}

.overview-card,
.health-card,
.alert-card,
.chart-card {
  margin-bottom: 20px;
}
</style>
