<template>
  <div class="inventory-management">
    <a-card title="全局库存台账" :bordered="false">
      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <a-row :gutter="[16, 16]">
          <a-col :span="8">
            <a-input
              v-model:value="searchForm.productSkuId"
              placeholder="SKU ID"
              style="width: 100%"
            />
          </a-col>
          <a-col :span="8">
            <a-input
              v-model:value="searchForm.warehouseId"
              placeholder="仓库ID"
              style="width: 100%"
            />
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

      <!-- 库存台账表格 -->
      <a-table
        :columns="columns"
        :data-source="inventoryList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <!-- 操作列 -->
        <template #column:action="{ record }">
          <a-space size="small">
            <a-button type="link" @click="handleEditThreshold(record)">
              设置预警阈值
            </a-button>
            <a-button type="link" @click="handleViewMovements(record)">
              查看流水
            </a-button>
          </a-space>
        </template>

        <!-- 状态列 -->
        <template #column:status="{ record }">
          <a-tag
            :color="record.available < (record.warningMin || 5) ? 'red' : 'green'"
          >
            {{ record.available < (record.warningMin || 5) ? '低库存' : '正常' }}
          </a-tag>
        </template>
      </a-table>
    </a-card>

    <!-- 预警阈值设置弹窗 -->
    <a-modal
      v-model:visible="thresholdModalVisible"
      title="设置预警阈值"
      @ok="handleThresholdOk"
      @cancel="thresholdModalVisible = false"
    >
      <a-form
        :model="thresholdForm"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="低库存预警值">
          <a-input-number
            v-model:value="thresholdForm.warningMin"
            :min="0"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="积压天数阈值">
          <a-input-number
            v-model:value="thresholdForm.overstockDays"
            :min="0"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>
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
});

// 表格数据
const inventoryList = ref<any[]>([]);
const loading = ref(false);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 预警阈值设置弹窗
const thresholdModalVisible = ref(false);
const thresholdForm = reactive({
  id: '',
  warningMin: 5,
  overstockDays: 30,
});

// 表格列定义
const columns = [
  {
    title: '库存ID',
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
    title: '在仓数量',
    dataIndex: 'onHand',
    key: 'onHand',
  },
  {
    title: '已预留',
    dataIndex: 'reserved',
    key: 'reserved',
  },
  {
    title: '可用库存',
    dataIndex: 'available',
    key: 'available',
  },
  {
    title: '低库存预警值',
    dataIndex: 'warningMin',
    key: 'warningMin',
  },
  {
    title: '积压天数阈值',
    dataIndex: 'overstockDays',
    key: 'overstockDays',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    slots: { customRender: 'status' },
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
  },
];

// 加载库存数据
const loadInventoryList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
      ...searchForm,
    };
    const res = await inventoryApi.getInventoryList(params);
    if (res.success) {
      inventoryList.value = res.result.records || [];
      pagination.value.total = res.result.total || 0;
    } else {
      message.error('加载库存数据失败');
    }
  } catch (error) {
    message.error('加载库存数据失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.value.current = 1;
  loadInventoryList();
};

// 重置
const resetForm = () => {
  searchForm.productSkuId = '';
  searchForm.warehouseId = '';
  pagination.value.current = 1;
  loadInventoryList();
};

// 表格分页
const handleTableChange = (pagination: any) => {
  pagination.value = pagination;
  loadInventoryList();
};

// 编辑预警阈值
const handleEditThreshold = (record: any) => {
  thresholdForm.id = record.id;
  thresholdForm.warningMin = record.warningMin || 5;
  thresholdForm.overstockDays = record.overstockDays || 30;
  thresholdModalVisible.value = true;
};

// 确认设置预警阈值
const handleThresholdOk = async () => {
  try {
    const res = await inventoryApi.updateThreshold(thresholdForm.id, {
      warningMin: thresholdForm.warningMin,
      overstockDays: thresholdForm.overstockDays,
    });
    if (res.success) {
      message.success('预警阈值设置成功');
      thresholdModalVisible.value = false;
      loadInventoryList();
    } else {
      message.error('预警阈值设置失败');
    }
  } catch (error) {
    message.error('预警阈值设置失败');
  }
};

// 查看流水
const handleViewMovements = (record: any) => {
  // 跳转到流水日志页
  console.log('查看流水:', record);
};

// 初始化
onMounted(() => {
  loadInventoryList();
});
</script>

<style scoped>
.inventory-management {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}
</style>
