<template>
  <div class="purchase-management">
    <a-card title="采购任务管理" :bordered="false">
      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <a-row :gutter="[16, 16]">
          <a-col :span="8">
            <a-input
              v-model:value="searchForm.supplierName"
              placeholder="供应商名称"
              style="width: 100%"
            />
          </a-col>
          <a-col :span="8">
            <a-select
              v-model:value="searchForm.status"
              placeholder="采购状态"
              style="width: 100%"
            >
              <a-option value="">全部</a-option>
              <a-option value="pending">待处理</a-option>
              <a-option value="processing">处理中</a-option>
              <a-option value="completed">已完成</a-option>
              <a-option value="cancelled">已取消</a-option>
            </a-select>
          </a-col>
          <a-col :span="8">
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

      <!-- 采购任务表格 -->
      <a-table
        :columns="columns"
        :data-source="purchaseList"
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
            <a-button type="link" @click="handleEdit(record)">
              编辑
            </a-button>
            <a-button type="link" @click="handleDelete(record)">
              删除
            </a-button>
          </a-space>
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
import { purchaseApi } from '@/api/taolink/purchase';
import { message } from 'ant-design-vue';

// 搜索表单
const searchForm = reactive({
  supplierName: '',
  status: '',
});

// 表格数据
const purchaseList = ref<any[]>([]);
const loading = ref(false);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 表格列定义
const columns = [
  {
    title: '采购单ID',
    dataIndex: 'id',
    key: 'id',
  },
  {
    title: '供应商名称',
    dataIndex: 'supplierName',
    key: 'supplierName',
  },
  {
    title: '供应商ID',
    dataIndex: 'supplierId',
    key: 'supplierId',
  },
  {
    title: '采购数量',
    dataIndex: 'totalQty',
    key: 'totalQty',
  },
  {
    title: '采购金额',
    dataIndex: 'totalAmount',
    key: 'totalAmount',
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

// 状态颜色
const getStatusColor = (status: string) => {
  switch (status) {
    case 'pending':
      return 'blue';
    case 'processing':
      return 'orange';
    case 'completed':
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
      return '待处理';
    case 'processing':
      return '处理中';
    case 'completed':
      return '已完成';
    case 'cancelled':
      return '已取消';
    default:
      return status;
  }
};

// 加载采购任务数据
const loadPurchaseList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
      ...searchForm,
    };
    const res = await purchaseApi.getPurchaseList(params);
    if (res.success) {
      purchaseList.value = res.result.records || [];
      pagination.value.total = res.result.total || 0;
    } else {
      message.error('加载采购任务数据失败');
    }
  } catch (error) {
    message.error('加载采购任务数据失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.value.current = 1;
  loadPurchaseList();
};

// 重置
const resetForm = () => {
  searchForm.supplierName = '';
  searchForm.status = '';
  pagination.value.current = 1;
  loadPurchaseList();
};

// 表格分页
const handleTableChange = (pagination: any) => {
  pagination.value = pagination;
  loadPurchaseList();
};

// 查看详情
const handleViewDetail = (record: any) => {
  console.log('查看详情:', record);
};

// 编辑
const handleEdit = (record: any) => {
  console.log('编辑:', record);
};

// 删除
const handleDelete = (record: any) => {
  console.log('删除:', record);
};

// 初始化
onMounted(() => {
  loadPurchaseList();
});
</script>

<style scoped>
.purchase-management {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}
</style>
