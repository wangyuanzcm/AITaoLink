<template>
  <div class="inventory-movements">
    <a-card title="出入库流水日志" :bordered="false">
      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <a-row :gutter="[16, 16]">
          <a-col :span="6">
            <a-input
              v-model:value="searchForm.productSkuId"
              placeholder="SKU ID"
              style="width: 100%"
            />
          </a-col>
          <a-col :span="6">
            <a-input
              v-model:value="searchForm.warehouseId"
              placeholder="仓库ID"
              style="width: 100%"
            />
          </a-col>
          <a-col :span="6">
            <a-select
              v-model:value="searchForm.type"
              placeholder="操作类型"
              style="width: 100%"
            >
              <a-option value="">全部</a-option>
              <a-option value="reserve">预占</a-option>
              <a-option value="release">释放</a-option>
              <a-option value="adjust">调整</a-option>
              <a-option value="deduct">扣减</a-option>
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

      <!-- 流水日志表格 -->
      <a-table
        :columns="columns"
        :data-source="movementsList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <!-- 数量列 -->
        <template #column:qty="{ record }">
          <span :style="{ color: record.qty > 0 ? 'green' : 'red' }">
            {{ record.qty > 0 ? '+' : '' }}{{ record.qty }}
          </span>
        </template>

        <!-- 操作类型列 -->
        <template #column:type="{ record }">
          <a-tag :color="getTypeColor(record.type)">
            {{ getTypeText(record.type) }}
          </a-tag>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { inventoryApi } from '@/api/taolink/inventory';
import { message } from 'ant-design-vue';

// 搜索表单
const searchForm = reactive({
  productSkuId: '',
  warehouseId: '',
  type: '',
});

// 表格数据
const movementsList = ref<any[]>([]);
const loading = ref(false);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 表格列定义
const columns = [
  {
    title: '流水ID',
    dataIndex: 'id',
    key: 'id',
  },
  {
    title: 'SKU ID',
    dataIndex: 'productSkuId',
    key: 'productSkuId',
  },
  {
    title: '仓库ID',
    dataIndex: 'warehouseId',
    key: 'warehouseId',
  },
  {
    title: '操作类型',
    dataIndex: 'type',
    key: 'type',
    slots: { customRender: 'type' },
  },
  {
    title: '数量',
    dataIndex: 'qty',
    key: 'qty',
    slots: { customRender: 'qty' },
  },
  {
    title: '关联类型',
    dataIndex: 'refType',
    key: 'refType',
  },
  {
    title: '关联ID',
    dataIndex: 'refId',
    key: 'refId',
  },
  {
    title: '操作时间',
    dataIndex: 'createTime',
    key: 'createTime',
  },
];

// 操作类型颜色
const getTypeColor = (type: string) => {
  switch (type) {
    case 'reserve':
      return 'blue';
    case 'release':
      return 'orange';
    case 'adjust':
      return 'purple';
    case 'deduct':
      return 'red';
    default:
      return 'default';
  }
};

// 操作类型文本
const getTypeText = (type: string) => {
  switch (type) {
    case 'reserve':
      return '预占';
    case 'release':
      return '释放';
    case 'adjust':
      return '调整';
    case 'deduct':
      return '扣减';
    default:
      return type;
  }
};

// 加载流水数据
const loadMovementsList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
      ...searchForm,
    };
    const res = await inventoryApi.getMovementList(params);
    if (res.success) {
      movementsList.value = res.result.records || [];
      pagination.value.total = res.result.total || 0;
    } else {
      message.error('加载流水数据失败');
    }
  } catch (error) {
    message.error('加载流水数据失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.value.current = 1;
  loadMovementsList();
};

// 重置
const resetForm = () => {
  searchForm.productSkuId = '';
  searchForm.warehouseId = '';
  searchForm.type = '';
  pagination.value.current = 1;
  loadMovementsList();
};

// 表格分页
const handleTableChange = (pagination: any) => {
  pagination.value = pagination;
  loadMovementsList();
};

// 初始化
onMounted(() => {
  loadMovementsList();
});
</script>

<style scoped>
.inventory-movements {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}
</style>
