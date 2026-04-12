<template>
  <div class="monthly-statement">
    <a-card title="月度对账单" :bordered="false">
      <!-- 查询条件 -->
      <div class="search-bar">
        <a-row :gutter="[16, 16]">
          <a-col :span="8">
            <a-input
              v-model:value="queryForm.supplierId"
              placeholder="供应商ID"
              style="width: 100%"
            />
          </a-col>
          <a-col :span="8">
            <a-date-picker
              v-model:value="datePickerValue"
              format="YYYY-MM"
              picker="month"
              style="width: 100%"
              @change="handleDateChange"
            />
          </a-col>
          <a-col :span="8">
            <a-button type="primary" @click="handleQuery">
              <a-icon type="search" />
              查询
            </a-button>
            <a-button style="margin-left: 8px" @click="handleExport" :disabled="!statementData.totalAmount">
              <a-icon type="download" />
              导出
            </a-button>
          </a-col>
        </a-row>
      </div>

      <!-- 对账单汇总信息 -->
      <div class="summary-card" v-if="statementData.totalAmount">
        <a-card title="汇总信息" :bordered="false">
          <a-row :gutter="[16, 16]">
            <a-col :span="8">
              <a-statistic title="供应商ID" :value="statementData.supplierId" />
            </a-col>
            <a-col :span="8">
              <a-statistic title="月份" :value="statementData.month" />
            </a-col>
            <a-col :span="8">
              <a-statistic title="总金额" :value="statementData.totalAmount" suffix="元" />
            </a-col>
            <a-col :span="8">
              <a-statistic title="记录条数" :value="statementData.recordCount" />
            </a-col>
          </a-row>
        </a-card>
      </div>

      <!-- 对账单明细 -->
      <div class="detail-card" v-if="statementData.records && statementData.records.length > 0">
        <a-card title="明细记录" :bordered="false">
          <a-table
            :columns="columns"
            :data-source="statementData.records"
            :loading="loading"
            :pagination="false"
          >
            <!-- 结算类型列 -->
            <template #column:settleType="{ record }">
              <a-tag :color="record.settleType === 'purchase' ? 'blue' : 'green'">
                {{ record.settleType === 'purchase' ? '采购' : '运费' }}
              </a-tag>
            </template>

            <!-- 状态列 -->
            <template #column:status="{ record }">
              <a-tag :color="record.status === 'settled' ? 'green' : 'blue'">
                {{ record.status === 'settled' ? '已结算' : '待结算' }}
              </a-tag>
            </template>
          </a-table>
        </a-card>
      </div>

      <!-- 无数据提示 -->
      <div v-else-if="!loading" class="no-data">
        <a-empty description="暂无对账单数据" />
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { settlementApi } from '@/api/taolink/settlement';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';

// 查询表单
const queryForm = reactive({
  supplierId: '',
  month: dayjs().format('YYYY-MM'),
});

// 日期选择器值
const datePickerValue = ref(dayjs());

// 对账单数据
const statementData = ref({
  supplierId: '',
  month: '',
  totalAmount: 0,
  recordCount: 0,
  records: [],
});

// 加载状态
const loading = ref(false);

// 表格列定义
const columns = [
  {
    title: '结算记录ID',
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
    title: '结算类型',
    dataIndex: 'settleType',
    key: 'settleType',
    slots: { customRender: 'settleType' },
  },
  {
    title: '金额',
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
    title: '备注',
    dataIndex: 'remark',
    key: 'remark',
  },
];

// 日期变更
const handleDateChange = (date: any) => {
  if (date) {
    queryForm.month = date.format('YYYY-MM');
  }
};

// 查询对账单
const handleQuery = async () => {
  if (!queryForm.supplierId) {
    message.warning('请输入供应商ID');
    return;
  }

  loading.value = true;
  try {
    const res = await settlementApi.getMonthlyStatement(queryForm.supplierId, queryForm.month);
    if (res.success) {
      statementData.value = res.result;
    } else {
      message.error('查询对账单失败');
    }
  } catch (error) {
    message.error('查询对账单失败');
  } finally {
    loading.value = false;
  }
};

// 导出对账单
const handleExport = () => {
  // 这里可以实现导出逻辑
  console.log('导出对账单');
  message.success('导出功能开发中');
};

// 初始化
onMounted(() => {
  // 默认查询当前月份的对账单
  // handleQuery();
});
</script>

<style scoped>
.monthly-statement {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.summary-card {
  margin-bottom: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.no-data {
  padding: 40px 0;
  text-align: center;
}
</style>
