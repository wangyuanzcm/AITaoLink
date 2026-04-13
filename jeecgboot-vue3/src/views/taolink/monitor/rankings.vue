<template>
  <div class="taolink-monitor-rankings">
    <a-card title="商品排行榜" :bordered="false">
      <!-- 排行榜类型选择 -->
      <div class="ranking-tabs">
        <a-radio-group v-model:value="rankingType" @change="handleRankingTypeChange">
          <a-radio-button value="sales">销量排行</a-radio-button>
          <a-radio-button value="amount">销售额排行</a-radio-button>
          <a-radio-button value="inventory">库存排行</a-radio-button>
        </a-radio-group>
      </div>

      <!-- 排行榜表格 -->
      <a-spin :spinning="loading" tip="加载中...">
        <a-table
          :columns="rankingColumns"
          :data-source="rankings"
          row-key="productId"
          size="middle"
          :pagination="false"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'rank'">
              <span class="rank-number" :class="{
                'rank-top-1': record.rank === 1,
                'rank-top-2': record.rank === 2,
                'rank-top-3': record.rank === 3
              }">
                {{ record.rank }}
              </span>
            </template>
            <template v-if="column.key === 'productName'">
              <a-tooltip :title="record.productName">
                <span class="product-name">{{ record.productName }}</span>
              </a-tooltip>
            </template>
            <template v-if="column.key === 'amount'">
              ¥{{ record.amount || 0 }}
            </template>
            <template v-if="column.key === 'inventory'">
              <span :class="{
                'inventory-low': record.inventory < 10,
                'inventory-medium': record.inventory >= 10 && record.inventory < 50,
                'inventory-high': record.inventory >= 50
              }">
                {{ record.inventory }}
              </span>
            </template>
          </template>
        </a-table>
      </a-spin>

      <!-- 导出按钮 -->
      <div class="export-section">
        <a-button type="primary" @click="exportRankings">
          <template #icon>
            <DownloadOutlined />
          </template>
          导出排行榜
        </a-button>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h } from 'vue';
import { message, Tag } from 'ant-design-vue';
import { DownloadOutlined } from '@ant-design/icons-vue';
import { useTaolinkStore } from '/@/store/modules/taolink';

defineOptions({ name: 'TaolinkMonitorRankings' });

const taolink = useTaolinkStore();

// 响应式数据
const rankingType = ref('sales');
interface RankingItem {
  rank: number;
  productId: string;
  productName: string;
  sales: number;
  amount: number;
  inventory: number;
  status: string;
}

const rankings = ref<RankingItem[]>([]);
const loading = ref(false);

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
    ellipsis: true,
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
  },
  {
    title: '库存',
    dataIndex: 'inventory',
    key: 'inventory',
    width: 100,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    customRender: ({ record }) =>
      h(Tag, { color: record.status === 'listed' ? 'green' : 'default' }, () =>
        record.status === 'listed' ? '在售' : '下架',
      ),
  },
];

// 处理排行榜类型变化
const handleRankingTypeChange = () => {
  getRankings();
};

// 获取排行榜数据
const getRankings = async () => {
  const shopId = taolink.currentShopId;
  if (!shopId) {
    message.warning('请先选择店铺');
    return;
  }

  loading.value = true;
  try {
    // 模拟接口调用
    // const response = await taolinkApi.getShopRankings(shopId, rankingType.value);
    // rankings.value = response.data;
    
    // 模拟数据
    if (rankingType.value === 'sales') {
      rankings.value = [
        { rank: 1, productId: '1', productName: '智能手表 Pro 2026款 多功能运动监测防水', sales: 120, amount: 12000, inventory: 50, status: 'listed' },
        { rank: 2, productId: '2', productName: '无线蓝牙耳机 主动降噪 长续航', sales: 95, amount: 9500, inventory: 35, status: 'listed' },
        { rank: 3, productId: '3', productName: '蓝牙音箱 便携户外防水', sales: 88, amount: 8800, inventory: 42, status: 'listed' },
        { rank: 4, productId: '4', productName: '运动手环 心率监测 睡眠分析', sales: 76, amount: 7600, inventory: 28, status: 'listed' },
        { rank: 5, productId: '5', productName: '充电宝 20000mAh 双向快充', sales: 65, amount: 6500, inventory: 60, status: 'listed' },
        { rank: 6, productId: '6', productName: '智能体脂秤 精准测量', sales: 58, amount: 5800, inventory: 30, status: 'listed' },
        { rank: 7, productId: '7', productName: '数据线 快充 1.5米', sales: 45, amount: 4500, inventory: 100, status: 'listed' },
        { rank: 8, productId: '8', productName: '手机壳 防摔 透明', sales: 38, amount: 3800, inventory: 80, status: 'listed' },
        { rank: 9, productId: '9', productName: '屏幕保护膜 钢化玻璃', sales: 32, amount: 3200, inventory: 120, status: 'listed' },
        { rank: 10, productId: '10', productName: '车载充电器 快充', sales: 28, amount: 2800, inventory: 45, status: 'listed' },
      ];
    } else if (rankingType.value === 'amount') {
      rankings.value = [
        { rank: 1, productId: '1', productName: '智能手表 Pro 2026款 多功能运动监测防水', sales: 120, amount: 12000, inventory: 50, status: 'listed' },
        { rank: 2, productId: '2', productName: '无线蓝牙耳机 主动降噪 长续航', sales: 95, amount: 9500, inventory: 35, status: 'listed' },
        { rank: 3, productId: '3', productName: '蓝牙音箱 便携户外防水', sales: 88, amount: 8800, inventory: 42, status: 'listed' },
        { rank: 4, productId: '4', productName: '运动手环 心率监测 睡眠分析', sales: 76, amount: 7600, inventory: 28, status: 'listed' },
        { rank: 5, productId: '5', productName: '充电宝 20000mAh 双向快充', sales: 65, amount: 6500, inventory: 60, status: 'listed' },
      ];
    } else if (rankingType.value === 'inventory') {
      rankings.value = [
        { rank: 1, productId: '7', productName: '数据线 快充 1.5米', sales: 45, amount: 4500, inventory: 100, status: 'listed' },
        { rank: 2, productId: '9', productName: '屏幕保护膜 钢化玻璃', sales: 32, amount: 3200, inventory: 120, status: 'listed' },
        { rank: 3, productId: '8', productName: '手机壳 防摔 透明', sales: 38, amount: 3800, inventory: 80, status: 'listed' },
        { rank: 4, productId: '5', productName: '充电宝 20000mAh 双向快充', sales: 65, amount: 6500, inventory: 60, status: 'listed' },
        { rank: 5, productId: '1', productName: '智能手表 Pro 2026款 多功能运动监测防水', sales: 120, amount: 12000, inventory: 50, status: 'listed' },
      ];
    }
  } catch (error) {
    message.error('获取排行榜数据失败');
    console.error('获取排行榜数据失败:', error);
  } finally {
    loading.value = false;
  }
};

// 导出排行榜
const exportRankings = () => {
  message.info('导出功能开发中');
};

// 生命周期
onMounted(() => {
  getRankings();
});
</script>

<style scoped>
.taolink-monitor-rankings {
  padding: 16px;
}

.ranking-tabs {
  margin-bottom: 20px;
}

.rank-number {
  display: inline-block;
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-radius: 50%;
  color: white;
  font-size: 14px;
  font-weight: bold;
}

.rank-top-1 {
  background-color: #ff4d4f;
}

.rank-top-2 {
  background-color: #fa8c16;
}

.rank-top-3 {
  background-color: #faad14;
}

.product-name {
  display: block;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.inventory-low {
  color: #ff4d4f;
  font-weight: bold;
}

.inventory-medium {
  color: #fa8c16;
}

.inventory-high {
  color: #52c41a;
}

.export-section {
  margin-top: 20px;
  text-align: right;
}
</style>
