<template>
  <div class="taolink-monitor-alerts">
    <a-card title="异常告警列表" :bordered="false">
      <!-- 告警筛选 -->
      <div class="alert-filters">
        <a-form layout="inline" :model="filterForm" @submit.prevent="handleFilter">
          <a-form-item label="告警类型">
            <a-select v-model:value="filterForm.alertType" placeholder="请选择告警类型">
              <a-select-option value="">全部</a-select-option>
              <a-select-option value="low_stock">低库存</a-select-option>
              <a-select-option value="overstock">积压库存</a-select-option>
              <a-select-option value="order_exception">订单异常</a-select-option>
              <a-select-option value="refund_exception">退款异常</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model:value="filterForm.status" placeholder="请选择状态">
              <a-select-option value="">全部</a-select-option>
              <a-select-option value="pending">待处理</a-select-option>
              <a-select-option value="resolved">已解决</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" html-type="submit">
              <template #icon>
                <SearchOutlined />
              </template>
              筛选
            </a-button>
          </a-form-item>
          <a-form-item>
            <a-button @click="resetFilter">重置</a-button>
          </a-form-item>
        </a-form>
      </div>

      <!-- 告警列表 -->
      <a-spin :spinning="loading" tip="加载中...">
        <a-table
          :columns="alertColumns"
          :data-source="alerts"
          row-key="id"
          size="middle"
          :pagination="{
            current: page.current,
            pageSize: page.size,
            total: page.total,
            onChange: (page, pageSize) => handlePageChange(page, pageSize)
          }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'alertType'">
              <a-tag :color="getAlertTypeColor(record.alertType)">
                {{ getAlertTypeText(record.alertType) }}
              </a-tag>
            </template>
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 'pending' ? 'warning' : 'success'">
                {{ record.status === 'pending' ? '待处理' : '已解决' }}
              </a-tag>
            </template>
            <template v-if="column.key === 'action'">
              <a-button 
                v-if="record.status === 'pending'" 
                type="primary" 
                size="small"
                @click="handleResolve(record)"
              >
                解决
              </a-button>
              <a-button 
                v-else 
                size="small"
                disabled
              >
                已解决
              </a-button>
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { SearchOutlined } from '@ant-design/icons-vue';
import { useTaolinkStore } from '/@/store/modules/taolink';

defineOptions({ name: 'TaolinkMonitorAlerts' });

const taolink = useTaolinkStore();

// 响应式数据
interface AlertItem {
  id: string;
  alertType: string;
  target: string;
  message: string;
  createdAt: string;
  status: string;
}

const alerts = ref<AlertItem[]>([]);
const loading = ref(false);
const page = ref({
  current: 1,
  size: 10,
  total: 0
});
const filterForm = ref({
  alertType: '',
  status: ''
});

// 表格列定义
const alertColumns = [
  {
    title: '告警ID',
    dataIndex: 'id',
    key: 'id',
    width: 100,
  },
  {
    title: '告警类型',
    dataIndex: 'alertType',
    key: 'alertType',
    width: 120,
  },
  {
    title: '告警对象',
    dataIndex: 'target',
    key: 'target',
  },
  {
    title: '告警消息',
    dataIndex: 'message',
    key: 'message',
  },
  {
    title: '发生时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 150,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '操作',
    dataIndex: 'action',
    key: 'action',
    width: 100,
  },
];

// 获取告警类型颜色
const getAlertTypeColor = (type: string) => {
  const colorMap = {
    'low_stock': 'red',
    'overstock': 'orange',
    'order_exception': 'blue',
    'refund_exception': 'purple'
  };
  return colorMap[type] || 'default';
};

// 获取告警类型文本
const getAlertTypeText = (type: string) => {
  const textMap = {
    'low_stock': '低库存',
    'overstock': '积压库存',
    'order_exception': '订单异常',
    'refund_exception': '退款异常'
  };
  return textMap[type] || type;
};

// 处理分页变化
const handlePageChange = (current: number, pageSize: number) => {
  page.value.current = current;
  page.value.size = pageSize;
  getAlerts();
};

// 处理筛选
const handleFilter = () => {
  page.value.current = 1;
  getAlerts();
};

// 重置筛选
const resetFilter = () => {
  filterForm.value = {
    alertType: '',
    status: ''
  };
  page.value.current = 1;
  getAlerts();
};

// 处理解决告警
const handleResolve = (record: AlertItem) => {
  Modal.confirm({
    title: '确认解决告警',
    content: `确定要解决该告警(${record.id})吗？`,
    onOk: async () => {
      try {
        // 模拟接口调用
        // await taolinkApi.resolveAlert(record.id);
        // 实际项目中应该调用真实的 API
        
        // 模拟成功
        message.success('告警已解决');
        getAlerts();
      } catch (error) {
        message.error('解决告警失败');
        console.error('解决告警失败:', error);
      }
    }
  });
};

// 获取告警列表
const getAlerts = async () => {
  const shopId = taolink.currentShopId;
  if (!shopId) {
    message.warning('请先选择店铺');
    return;
  }

  loading.value = true;
  try {
    // 模拟接口调用
    // const response = await taolinkApi.getAlerts({
    //   shopId,
    //   alertType: filterForm.value.alertType,
    //   status: filterForm.value.status,
    //   page: page.value.current,
    //   pageSize: page.value.size
    // });
    // alerts.value = response.data.records;
    // page.value.total = response.data.total;
    
    // 模拟数据
    alerts.value = [
      {
        id: '1',
        alertType: 'low_stock',
        target: '智能手表 Pro 2026款',
        message: '库存不足，当前库存: 5，预警阈值: 10',
        createdAt: '2026-04-12 10:30:00',
        status: 'pending'
      },
      {
        id: '2',
        alertType: 'overstock',
        target: '数据线 快充 1.5米',
        message: '库存积压，当前库存: 100，预警阈值: 50',
        createdAt: '2026-04-12 09:15:00',
        status: 'pending'
      },
      {
        id: '3',
        alertType: 'order_exception',
        target: '订单 #20260412001',
        message: '订单支付后30分钟未发货',
        createdAt: '2026-04-12 08:45:00',
        status: 'resolved'
      },
      {
        id: '4',
        alertType: 'refund_exception',
        target: '订单 #20260411005',
        message: '退款申请超过48小时未处理',
        createdAt: '2026-04-12 07:30:00',
        status: 'pending'
      },
      {
        id: '5',
        alertType: 'low_stock',
        target: '无线蓝牙耳机',
        message: '库存不足，当前库存: 8，预警阈值: 15',
        createdAt: '2026-04-11 16:20:00',
        status: 'resolved'
      }
    ];
    page.value.total = alerts.value.length;
  } catch (error) {
    message.error('获取告警列表失败');
    console.error('获取告警列表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 生命周期
onMounted(() => {
  getAlerts();
});
</script>

<style scoped>
.taolink-monitor-alerts {
  padding: 16px;
}

.alert-filters {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

:deep(.ant-form-inline .ant-form-item) {
  margin-right: 16px;
}
</style>
