<template>
  <div class="settlement-management">
    <a-card title="结算记录管理" :bordered="false">
      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <a-row :gutter="[16, 16]">
          <a-col :span="6">
            <a-input
              v-model:value="searchForm.supplierName"
              placeholder="供应商名称"
              style="width: 100%"
            />
          </a-col>
          <a-col :span="6">
            <a-input
              v-model:value="searchForm.orderId"
              placeholder="订单ID"
              style="width: 100%"
            />
          </a-col>
          <a-col :span="6">
            <a-select
              v-model:value="searchForm.status"
              placeholder="结算状态"
              style="width: 100%"
            >
              <a-option value="">全部</a-option>
              <a-option value="pending">待结算</a-option>
              <a-option value="settled">已结算</a-option>
              <a-option value="cancelled">已取消</a-option>
            </a-select>
          </a-col>
          <a-col :span="6">
            <a-button type="primary" @click="handleSearch">
              <a-icon type="search" />
              搜索
            </a-button>
            <a-button style="margin-left: 8px" @click="resetForm">
              重置
            </a-button>
          </a-col>
        </a-row>
      </div>

      <!-- 结算记录表格 -->
      <a-table
        :columns="columns"
        :data-source="settlementList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <!-- 操作列 -->
        <template #column:action="{ record }">
          <a-space size="small">
            <a-button type="link" @click="handleViewDetail(record)">
              查看详情
            </a-button>
            <a-button v-if="record.status === 'pending'" type="primary" @click="handleSettle(record)">
              标记结算
            </a-button>
            <a-button v-if="record.status === 'pending'" danger @click="handleCancel(record)">
              取消结算
            </a-button>
          </a-space>
        </template>

        <!-- 结算类型列 -->
        <template #column:settleType="{ record }">
          <a-tag :color="record.settleType === 'purchase' ? 'blue' : 'green'">
            {{ record.settleType === 'purchase' ? '采购' : '运费' }}
          </a-tag>
        </template>

        <!-- 状态列 -->
        <template #column:status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { settlementApi } from '@/api/taolink/settlement';
import { message } from 'ant-design-vue';

// 搜索表单
const searchForm = reactive({
  supplierName: '',
  orderId: '',
  status: '',
});

// 表格数据
const settlementList = ref<any[]>([]);
const loading = ref(false);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 表格列定义
const columns = [
  {
    title: '结算记录ID',
    dataIndex: 'id',
    key: 'id',
  },
  {
    title: '供应商名称',
    dataIndex: 'supplierName',
    key: 'supplierName',
  },
  {
    title: '订单ID',
    dataIndex: 'orderId',
    key: 'orderId',
  },
  {
    title: '采购单ID',
    dataIndex: 'purchaseId',
    key: 'purchaseId',
  },
  {
    title: '结算类型',
    dataIndex: 'settleType',
    key: 'settleType',
    slots: { customRender: 'settleType' },
  },
  {
    title: '结算金额',
    dataIndex: 'amount',
    key: 'amount',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    slots: { customRender: 'status' },
  },
  {
    title: '结算时间',
    dataIndex: 'settleTime',
    key: 'settleTime',
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
  },
];

// 状态颜色
const getStatusColor = (status: string) => {
  switch (status) {
    case 'pending':
      return 'blue';
    case 'settled':
      return 'green';
    case 'cancelled':
      return 'red';
    default:
      return 'default';
  }
};

// 状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'pending':
      return '待结算';
    case 'settled':
      return '已结算';
    case 'cancelled':
      return '已取消';
    default:
      return status;
  }
};

// 加载结算记录数据
const loadSettlementList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
      ...searchForm,
    };
    const res = await settlementApi.getSettlementList(params);
    if (res.success) {
      settlementList.value = res.result.records || [];
      pagination.value.total = res.result.total || 0;
    } else {
      message.error('加载结算记录数据失败');
    }
  } catch (error) {
    message.error('加载结算记录数据失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.value.current = 1;
  loadSettlementList();
};

// 重置
const resetForm = () => {
  searchForm.supplierName = '';
  searchForm.orderId = '';
  searchForm.status = '';
  pagination.value.current = 1;
  loadSettlementList();
};

// 表格分页
const handleTableChange = (pagination: any) => {
  pagination.value = pagination;
  loadSettlementList();
};

// 查看详情
const handleViewDetail = (record: any) => {
  console.log('查看详情:', record);
};

// 标记结算
const handleSettle = async (record: any) => {
  try {
    const res = await settlementApi.settleRecord(record.id);
    if (res.success) {
      message.success('标记结算成功');
      loadSettlementList();
    } else {
      message.error('标记结算失败');
    }
  } catch (error) {
    message.error('标记结算失败');
  }
};

// 取消结算
const handleCancel = async (record: any) => {
  try {
    const res = await settlementApi.cancelRecord(record.id);
    if (res.success) {
      message.success('取消结算成功');
      loadSettlementList();
    } else {
      message.error('取消结算失败');
    }
  } catch (error) {
    message.error('取消结算失败');
  }
};

// 初始化
onMounted(() => {
  loadSettlementList();
});
</script>

<style scoped>
.settlement-management {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}
</style>
