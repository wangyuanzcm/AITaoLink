<template>
  <div class="taolink-shop-ranking">
    <!-- 页面标题 -->
    <div class="page-header">
      <a-page-header
        title="店铺排行"
        sub-title="按订单量和GMV进行店铺排名"
      />
    </div>

    <!-- 筛选和排序 -->
    <div class="filter-section">
      <a-card>
        <a-row :gutter="[16, 16]">
          <a-col :span="8">
            <a-form-item label="排序方式">
              <a-select v-model:value="sortBy" style="width: 100%">
                <a-select-option value="gmv">按GMV排序</a-select-option>
                <a-select-option value="orderCount">按订单数排序</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="排序方向">
              <a-select v-model:value="sortOrder" style="width: 100%">
                <a-select-option value="desc">降序</a-select-option>
                <a-select-option value="asc">升序</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="店铺状态">
              <a-select v-model:value="statusFilter" style="width: 100%" allow-clear>
                <a-select-option value="active">正常</a-select-option>
                <a-select-option value="inactive">禁用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
      </a-card>
    </div>

    <!-- 数据加载状态 -->
    <a-spin :spinning="loading">
      <!-- 排行表格 -->
      <div class="ranking-table">
        <a-card>
          <a-table
            :data-source="filteredRanking"
            :columns="rankingColumns"
            :pagination="{
              showSizeChanger: true,
              pageSizeOptions: ['10', '20', '50'],
              total: filteredRanking.length,
              showTotal: (total) => `共 ${total} 家店铺`
            }"
            row-key="shopId"
            :scroll="{ x: 800 }"
          >
            <!-- 排名列 -->
            <template #headerCell="{ column }">
              <template v-if="column.key === 'rank'">
                <span>排名</span>
              </template>
            </template>
            <template #bodyCell="{ record }">
              <template v-if="record.rank <= 3">
                <a-tag :color="getRankColor(record.rank)">
                  {{ record.rank }}
                </a-tag>
              </template>
              <template v-else>
                {{ record.rank }}
              </template>
            </template>
          </a-table>
        </a-card>
      </div>

      <!-- 排行图表 -->
      <div class="ranking-chart">
        <a-card title="店铺GMV排行前10">
          <div class="chart-container">
            <div ref="chartRef" style="width: 100%; height: 400px"></div>
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

defineOptions({ name: 'TaolinkDashboardShops' });

// 数据状态
const loading = ref(false);

interface ShopRankingItem {
  shopId: string;
  shopName: string;
  orderCount: number;
  gmv: number;
  status: string;
}

const shopRanking = ref<ShopRankingItem[]>([]);
const sortBy = ref<'gmv' | 'orderCount'>('gmv');
const sortOrder = ref<'desc' | 'asc'>('desc');
const statusFilter = ref('');

const chartRef = ref<HTMLDivElement | null>(null);
const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>);

// 表格列配置
const rankingColumns = [
  {
    title: '排名',
    dataIndex: 'rank',
    key: 'rank',
    width: 80
  },
  {
    title: '店铺名称',
    dataIndex: 'shopName',
    key: 'shopName',
    width: 200
  },
  {
    title: '订单数',
    dataIndex: 'orderCount',
    key: 'orderCount',
    width: 120
  },
  {
    title: 'GMV',
    dataIndex: 'gmv',
    key: 'gmv',
    width: 150,
    customRender: ({ text }) => `¥${(text / 100).toFixed(2)}`,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    customRender: ({ text }) => {
      const statusMap = {
        'active': { text: '正常', color: 'green' },
        'inactive': { text: '禁用', color: 'red' }
      };
      const status = statusMap[text] || { text: text, color: 'default' };
      return h(Tag, { color: status.color }, () => status.text);
    }
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right',
    customRender: () =>
      h(
        'div',
        { style: { display: 'flex', gap: '8px' } },
        [
          h(Button, { type: 'link', size: 'small' }, () => '详情'),
          h(Button, { type: 'link', size: 'small' }, () => '查看店铺'),
        ],
      ),
  }
];

// 过滤和排序后的数据
const filteredRanking = computed(() => {
  let result = [...shopRanking.value];
  
  // 状态筛选
  if (statusFilter.value) {
    result = result.filter(item => item.status === statusFilter.value);
  }
  
  // 排序
  result.sort((a, b) => {
    const aValue = a[sortBy.value];
    const bValue = b[sortBy.value];
    if (sortOrder.value === 'desc') {
      return bValue - aValue;
    } else {
      return aValue - bValue;
    }
  });
  
  // 添加排名
  return result.map((item, index) => ({
    ...item,
    rank: index + 1
  }));
});

// 图表配置
const chartOption = computed<EChartsOption>(() => {
  const top10Shops = filteredRanking.value.slice(0, 10);
  const shopNames = top10Shops.map(item => item.shopName);
  const gmvValues = top10Shops.map(item => item.gmv / 100); // 转换为元

  return {
    title: {
      text: '店铺GMV排行前10',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const data = params[0];
        const val = Number(data?.value ?? 0);
        return `${data.name}<br/>GMV: ¥${val.toFixed(2)}`;
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: shopNames,
      axisLabel: {
        interval: 0,
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: 'GMV (元)'
    },
    series: [
      {
        name: 'GMV',
        type: 'bar',
        data: gmvValues,
        itemStyle: { color: '#1890ff' },
        label: {
          show: true,
          position: 'top',
          formatter: function(params) {
            const val = Number(params?.value ?? 0);
            return `¥${val.toFixed(0)}`;
          }
        }
      }
    ]
  };
});

// 获取排名颜色
const getRankColor = (rank: number) => {
  switch (rank) {
    case 1:
      return 'gold';
    case 2:
      return 'silver';
    case 3:
      return 'bronze';
    default:
      return 'default';
  }
};

// 模拟数据
const mockShopRanking: ShopRankingItem[] = [
  { shopId: '1', shopName: '淘宝旗舰店', orderCount: 1200, gmv: 6000000, status: 'active' },
  { shopId: '2', shopName: '京东专卖店', orderCount: 950, gmv: 4800000, status: 'active' },
  { shopId: '3', shopName: '拼多多店', orderCount: 1500, gmv: 3500000, status: 'active' },
  { shopId: '4', shopName: '抖音小店', orderCount: 800, gmv: 2800000, status: 'active' },
  { shopId: '5', shopName: '小红书店', orderCount: 450, gmv: 1500000, status: 'inactive' },
  { shopId: '6', shopName: '天猫超市', orderCount: 2000, gmv: 8000000, status: 'active' },
  { shopId: '7', shopName: '苏宁易购', orderCount: 750, gmv: 2200000, status: 'active' },
  { shopId: '8', shopName: '国美电器', orderCount: 600, gmv: 1800000, status: 'inactive' },
  { shopId: '9', shopName: '唯品会', orderCount: 1100, gmv: 3200000, status: 'active' },
  { shopId: '10', shopName: '考拉海购', orderCount: 550, gmv: 1600000, status: 'active' },
  { shopId: '11', shopName: '亚马逊中国', orderCount: 400, gmv: 1200000, status: 'active' },
  { shopId: '12', shopName: '当当网', orderCount: 350, gmv: 900000, status: 'inactive' }
];

// 初始化数据
const initData = async () => {
  loading.value = true;
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // 使用模拟数据
    shopRanking.value = mockShopRanking;
  } catch (error) {
    console.error('获取店铺排行数据失败:', error);
  } finally {
    loading.value = false;
  }
};

watchEffect(() => {
  if (!chartRef.value) return;
  setOptions(chartOption.value);
});

// 页面挂载时初始化数据
onMounted(() => {
  initData();
});
</script>

<style scoped>
.taolink-shop-ranking {
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

.ranking-table {
  margin-bottom: 20px;
}

.ranking-chart {
  margin-bottom: 20px;
}

.chart-container {
  height: 400px;
}
</style>
