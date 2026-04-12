<template>
  <div class="taolink-alerts-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <a-page-header
        title="全局预警中心"
        sub-title="库存、订单等异常预警汇总"
      />
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <a-card>
        <a-row :gutter="[16, 16]">
          <a-col :span="6">
            <a-form-item label="预警类型">
              <a-select v-model:value="alertTypeFilter" style="width: 100%" allow-clear>
                <a-select-option value="lowStock">低库存预警</a-select-option>
                <a-select-option value="overstock">积压库存预警</a-select-option>
                <a-select-option value="order">订单异常预警</a-select-option>
                <a-select-option value="refund">退款异常预警</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="预警状态">
              <a-select v-model:value="alertStatusFilter" style="width: 100%" allow-clear>
                <a-select-option value="pending">待处理</a-select-option>
                <a-select-option value="processing">处理中</a-select-option>
                <a-select-option value="resolved">已解决</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="搜索">
              <a-input
                v-model:value="searchKeyword"
                placeholder="输入商品ID、店铺名称等"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item>
              <a-button type="primary" style="width: 100%" @click="refreshAlerts">
                <template #icon>
                  <a-icon component="ReloadOutlined" />
                </template>
                刷新
              </a-button>
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 预警统计 -->
    <div class="alerts-stats">
      <a-row :gutter="[16, 16]">
        <a-col :span="6">
          <a-card class="stat-card" hoverable>
            <div class="stat-content">
              <div class="stat-value">{{ totalAlerts }}</div>
              <div class="stat-label">总预警数</div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" hoverable>
            <div class="stat-content">
              <div class="stat-value">{{ pendingAlerts }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" hoverable>
            <div class="stat-content">
              <div class="stat-value">{{ processingAlerts }}</div>
              <div class="stat-label">处理中</div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="stat-card" hoverable>
            <div class="stat-content">
              <div class="stat-value">{{ resolvedAlerts }}</div>
              <div class="stat-label">已解决</div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 数据加载状态 -->
    <a-spin :spinning="loading">
      <!-- 预警列表 -->
      <div class="alerts-list">
        <a-card>
          <a-table
            :data-source="filteredAlerts"
            :columns="alertColumns"
            :pagination="{
              showSizeChanger: true,
              pageSizeOptions: ['10', '20', '50'],
              total: filteredAlerts.length,
              showTotal: (total) => `共 ${total} 条预警`
            }"
            row-key="id"
            :scroll="{ x: 1000 }"
          >
            <!-- 预警类型 -->
            <template #bodyCell="{ record }">
              <template v-if="record.type === 'lowStock'">
                <a-tag color="orange">低库存预警</a-tag>
              </template>
              <template v-else-if="record.type === 'overstock'">
                <a-tag color="red">积压库存预警</a-tag>
              </template>
              <template v-else-if="record.type === 'order'">
                <a-tag color="blue">订单异常预警</a-tag>
              </template>
              <template v-else-if="record.type === 'refund'">
                <a-tag color="purple">退款异常预警</a-tag>
              </template>
              <template v-else>
                <a-tag>{{ record.type }}</a-tag>
              </template>
            </template>
            <!-- 预警状态 -->
            <template #bodyCell="{ record }">
              <template v-if="record.status === 'pending'">
                <a-tag color="warning">待处理</a-tag>
              </template>
              <template v-else-if="record.status === 'processing'">
                <a-tag color="blue">处理中</a-tag>
              </template>
              <template v-else-if="record.status === 'resolved'">
                <a-tag color="green">已解决</a-tag>
              </template>
              <template v-else>
                <a-tag>{{ record.status }}</a-tag>
              </template>
            </template>
            <!-- 操作 -->
            <template #bodyCell="{ record }">
              <div>
                <a-button type="link" size="small" @click="viewAlert(record)">查看</a-button>
                <a-button type="link" size="small" @click="processAlert(record)" v-if="record.status === 'pending'">处理</a-button>
                <a-button type="link" size="small" @click="resolveAlert(record)" v-if="record.status === 'processing'">解决</a-button>
              </div>
            </template>
          </a-table>
        </a-card>
      </div>

      <!-- 预警类型分布 -->
      <div class="alerts-distribution">
        <a-card title="预警类型分布">
          <div class="chart-container">
            <v-chart
              :option="alertTypeChartOption"
              style="width: 100%; height: 300px"
            />
          </div>
        </a-card>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { VChart } from '@visactor/vue3-vchart';
import { PieChart, BarChart, Title, Tooltip, Legend, Axis } from '@visactor/vchart';
import { ReloadOutlined } from '@ant-design/icons-vue';

// 注册 VChart 组件
VChart.use([PieChart, BarChart, Title, Tooltip, Legend, Axis]);

// 数据状态
const loading = ref(false);
const alerts = ref([]);
const alertTypeFilter = ref('');
const alertStatusFilter = ref('');
const searchKeyword = ref('');

// 表格列配置
const alertColumns = [
  {
    title: '预警类型',
    dataIndex: 'type',
    key: 'type',
    width: 120
  },
  {
    title: '预警对象',
    dataIndex: 'object',
    key: 'object',
    width: 200
  },
  {
    title: '预警描述',
    dataIndex: 'description',
    key: 'description',
    width: 300
  },
  {
    title: '预警时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 180
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right'
  }
];

// 过滤后的预警列表
const filteredAlerts = computed(() => {
  let result = [...alerts.value];
  
  // 类型筛选
  if (alertTypeFilter.value) {
    result = result.filter(item => item.type === alertTypeFilter.value);
  }
  
  // 状态筛选
  if (alertStatusFilter.value) {
    result = result.filter(item => item.status === alertStatusFilter.value);
  }
  
  // 搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(item => 
      item.object.toLowerCase().includes(keyword) ||
      item.description.toLowerCase().includes(keyword)
    );
  }
  
  return result;
});

// 预警统计
const totalAlerts = computed(() => alerts.value.length);
const pendingAlerts = computed(() => alerts.value.filter(item => item.status === 'pending').length);
const processingAlerts = computed(() => alerts.value.filter(item => item.status === 'processing').length);
const resolvedAlerts = computed(() => alerts.value.filter(item => item.status === 'resolved').length);

// 预警类型分布图表配置
const alertTypeChartOption = computed(() => {
  const typeCounts = {
    lowStock: 0,
    overstock: 0,
    order: 0,
    refund: 0
  };
  
  alerts.value.forEach(alert => {
    if (typeCounts.hasOwnProperty(alert.type)) {
      typeCounts[alert.type]++;
    }
  });
  
  const data = [
    { name: '低库存预警', value: typeCounts.lowStock },
    { name: '积压库存预警', value: typeCounts.overstock },
    { name: '订单异常预警', value: typeCounts.order },
    { name: '退款异常预警', value: typeCounts.refund }
  ];
  
  return {
    title: {
      text: '预警类型分布',
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
        name: '预警类型',
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

// 查看预警详情
const viewAlert = (record) => {
  console.log('查看预警详情:', record);
  // 实际项目中这里会打开详情模态框
};

// 处理预警
const processAlert = (record) => {
  console.log('处理预警:', record);
  // 实际项目中这里会更新预警状态为处理中
};

// 解决预警
const resolveAlert = (record) => {
  console.log('解决预警:', record);
  // 实际项目中这里会更新预警状态为已解决
};

// 刷新预警列表
const refreshAlerts = () => {
  initData();
};

// 模拟数据
const mockAlerts = [
  { id: '1', type: 'lowStock', object: '商品SKU: SKU001', description: '库存不足，当前库存: 2，预警阈值: 5', status: 'pending', createdAt: '2026-04-12 08:30:00' },
  { id: '2', type: 'lowStock', object: '商品SKU: SKU002', description: '库存不足，当前库存: 1，预警阈值: 5', status: 'pending', createdAt: '2026-04-12 09:15:00' },
  { id: '3', type: 'overstock', object: '商品SKU: SKU003', description: '库存积压，当前库存: 150，超过30天未动', status: 'processing', createdAt: '2026-04-12 10:00:00' },
  { id: '4', type: 'order', object: '订单: ORDER001', description: '订单待处理超过24小时', status: 'pending', createdAt: '2026-04-12 11:30:00' },
  { id: '5', type: 'refund', object: '订单: ORDER002', description: '退款处理超过24小时', status: 'resolved', createdAt: '2026-04-12 12:45:00' },
  { id: '6', type: 'lowStock', object: '商品SKU: SKU004', description: '库存不足，当前库存: 0，预警阈值: 5', status: 'pending', createdAt: '2026-04-12 14:00:00' },
  { id: '7', type: 'overstock', object: '商品SKU: SKU005', description: '库存积压，当前库存: 200，超过30天未动', status: 'pending', createdAt: '2026-04-12 15:30:00' },
  { id: '8', type: 'order', object: '订单: ORDER003', description: '订单待处理超过24小时', status: 'processing', createdAt: '2026-04-12 16:45:00' },
  { id: '9', type: 'refund', object: '订单: ORDER004', description: '退款处理超过24小时', status: 'pending', createdAt: '2026-04-12 18:00:00' },
  { id: '10', type: 'lowStock', object: '商品SKU: SKU006', description: '库存不足，当前库存: 3，预警阈值: 5', status: 'resolved', createdAt: '2026-04-12 19:15:00' },
  { id: '11', type: 'overstock', object: '商品SKU: SKU007', description: '库存积压，当前库存: 120，超过30天未动', status: 'pending', createdAt: '2026-04-12 20:30:00' },
  { id: '12', type: 'order', object: '订单: ORDER005', description: '订单待处理超过24小时', status: 'resolved', createdAt: '2026-04-12 21:45:00' }
];

// 初始化数据
const initData = async () => {
  loading.value = true;
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // 使用模拟数据
    alerts.value = mockAlerts;
  } catch (error) {
    console.error('获取预警数据失败:', error);
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
.taolink-alerts-center {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.filter-section {
  margin-bottom: 20px;
}

.alerts-stats {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.alerts-list {
  margin-bottom: 20px;
}

.alerts-distribution {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}
</style>
