<template>
  <div class="shipment-backfill">
    <a-card title="发货回填" :bordered="false">
      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <a-row :gutter="[16, 16]">
          <a-col :span="8">
            <a-input
              v-model:value="searchForm.orderId"
              placeholder="订单ID"
              style="width: 100%"
            />
          </a-col>
          <a-col :span="8">
            <a-input
              v-model:value="searchForm.purchaseId"
              placeholder="采购单ID"
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

      <!-- 采购单行列表 -->
      <a-table
        :columns="columns"
        :data-source="purchaseLines"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-selection="rowSelection"
      >
        <!-- 物流公司列 -->
        <template #column:sourceTrackingCompany="{ record }">
          <a-select
            v-model:value="record.sourceTrackingCompany"
            placeholder="选择物流公司"
            style="width: 150px"
          >
            <a-option value="SF">顺丰</a-option>
            <a-option value="YT">圆通</a-option>
            <a-option value="YD">韵达</a-option>
            <a-option value="ZT">中通</a-option>
            <a-option value="ST">申通</a-option>
          </a-select>
        </template>

        <!-- 运单号列 -->
        <template #column:sourceTrackingNo="{ record }">
          <a-input
            v-model:value="record.sourceTrackingNo"
            placeholder="请输入运单号"
            style="width: 200px"
          />
        </template>

        <!-- 运费列 -->
        <template #column:freightCost="{ record }">
          <a-input-number
            v-model:value="record.freightCost"
            :min="0"
            style="width: 100px"
          />
        </template>

        <!-- 操作列 -->
        <template #column:action="{ record }">
          <a-button type="primary" @click="handleSingleBackfill(record)">
            单独回填
          </a-button>
        </template>
      </a-table>

      <!-- 批量操作 -->
      <div class="batch-actions" style="margin-top: 20px">
        <a-button type="primary" @click="handleBatchBackfill" :disabled="selectedRows.length === 0">
          批量回填
        </a-button>
        <a-button @click="handleSelectAll" style="margin-left: 8px">
          全选
        </a-button>
        <a-button @click="handleClearSelection" style="margin-left: 8px">
          清空选择
        </a-button>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { purchaseApi } from '@/api/taolink/purchase';
import { message } from 'ant-design-vue';

// 搜索表单
const searchForm = reactive({
  orderId: '',
  purchaseId: '',
});

// 表格数据
const purchaseLines = ref<any[]>([]);
const loading = ref(false);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

// 选中的行
const selectedRows = ref<any[]>([]);
const rowSelection = {
  onChange: (selectedRowKeys: any, selectedRows: any[]) => {
    selectedRows.value = selectedRows;
  },
};

// 表格列定义
const columns = [
  {
    title: '采购单行ID',
    dataIndex: 'id',
    key: 'id',
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
    title: 'SKU ID',
    dataIndex: 'productSkuId',
    key: 'productSkuId',
  },
  {
    title: '数量',
    dataIndex: 'qty',
    key: 'qty',
  },
  {
    title: '物流公司',
    dataIndex: 'sourceTrackingCompany',
    key: 'sourceTrackingCompany',
    slots: { customRender: 'sourceTrackingCompany' },
  },
  {
    title: '运单号',
    dataIndex: 'sourceTrackingNo',
    key: 'sourceTrackingNo',
    slots: { customRender: 'sourceTrackingNo' },
  },
  {
    title: '运费',
    dataIndex: 'freightCost',
    key: 'freightCost',
    slots: { customRender: 'freightCost' },
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
  },
];

// 加载采购单行数据
const loadPurchaseLines = async () => {
  loading.value = true;
  try {
    const params = {
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize,
      ...searchForm,
    };
    const res = await purchaseApi.getPurchaseLineList(params);
    if (res.success) {
      purchaseLines.value = res.result.records || [];
      pagination.value.total = res.result.total || 0;
    } else {
      message.error('加载采购单行数据失败');
    }
  } catch (error) {
    message.error('加载采购单行数据失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.value.current = 1;
  loadPurchaseLines();
};

// 重置
const resetForm = () => {
  searchForm.orderId = '';
  searchForm.purchaseId = '';
  pagination.value.current = 1;
  loadPurchaseLines();
};

// 表格分页
const handleTableChange = (pagination: any) => {
  pagination.value = pagination;
  loadPurchaseLines();
};

// 单独回填
const handleSingleBackfill = async (record: any) => {
  try {
    const res = await purchaseApi.fillTracking(record.id, {
      sourceTrackingCompany: record.sourceTrackingCompany,
      sourceTrackingNo: record.sourceTrackingNo,
      freightCost: record.freightCost,
      remark: '单独回填',
    });
    if (res.success) {
      message.success('回填成功');
      loadPurchaseLines();
    } else {
      message.error('回填失败');
    }
  } catch (error) {
    message.error('回填失败');
  }
};

// 批量回填
const handleBatchBackfill = async () => {
  if (selectedRows.value.length === 0) {
    message.warning('请选择要回填的记录');
    return;
  }

  try {
    for (const record of selectedRows.value) {
      if (!record.sourceTrackingCompany || !record.sourceTrackingNo) {
        message.error('请填写完整的物流公司和运单号');
        return;
      }
      
      const res = await purchaseApi.fillTracking(record.id, {
        sourceTrackingCompany: record.sourceTrackingCompany,
        sourceTrackingNo: record.sourceTrackingNo,
        freightCost: record.freightCost,
        remark: '批量回填',
      });
      
      if (!res.success) {
        message.error(`回填失败：${res.message}`);
        return;
      }
    }
    
    message.success('批量回填成功');
    loadPurchaseLines();
    selectedRows.value = [];
  } catch (error) {
    message.error('批量回填失败');
  }
};

// 全选
const handleSelectAll = () => {
  // 这里可以实现全选逻辑
  console.log('全选');
};

// 清空选择
const handleClearSelection = () => {
  selectedRows.value = [];
};

// 初始化
onMounted(() => {
  loadPurchaseLines();
});
</script>

<style scoped>
.shipment-backfill {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.batch-actions {
  display: flex;
  justify-content: flex-start;
}
</style>
