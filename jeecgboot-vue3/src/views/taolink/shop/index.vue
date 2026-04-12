<template>
  <div class="shop-list-container">
    <a-card title="店铺管理" :bordered="false">
      <!-- 搜索区域 -->
      <div class="search-area">
        <a-form :model="searchForm" layout="inline" @keyup.enter="handleSearch">
          <a-form-item label="店铺名称">
            <a-input v-model:value="searchForm.taobaoSellerNick" placeholder="请输入店铺名称" />
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model:value="searchForm.status" placeholder="请选择状态">
              <a-select-option value="active">活跃</a-select-option>
              <a-select-option value="disabled">禁用</a-select-option>
              <a-select-option value="unbound">已解绑</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="resetForm">重置</a-button>
          </a-form-item>
        </a-form>
      </div>

      <!-- 操作区域 -->
      <div class="action-area">
        <a-button type="primary" @click="handleBindShop">绑定新店</a-button>
      </div>

      <!-- 店铺列表 -->
      <a-table
        :columns="columns"
        :data-source="dataList"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
      >
        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">{{ getStatusText(record.status) }}</a-tag>
        </template>
        <template #apiStatus="{ record }">
          <a-tag :color="getApiStatusColor(record.apiExpireAt)">{{ getApiStatusText(record.apiExpireAt) }}</a-tag>
        </template>
        <template #action="{ record }">
          <a-button type="link" @click="handleEdit(record)">编辑</a-button>
          <a-button type="link" danger @click="handleUnbind(record.id)">解绑</a-button>
          <a-button type="link" @click="handleDetail(record.id)">详情</a-button>
          <a-button type="link" v-if="record.status === 'active'" @click="handleReauthorize(record.id)">重新授权</a-button>
        </template>
      </a-table>
    </a-card>

    <!-- 编辑店铺对话框 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form :model="formData" :rules="formRules" ref="formRef">
        <a-form-item label="状态" name="status">
          <a-select v-model:value="formData.status">
            <a-select-option value="active">活跃</a-select-option>
            <a-select-option value="disabled">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="绑定平台" name="bindPlatforms">
          <a-select v-model:value="formData.bindPlatforms" mode="tags" placeholder="请选择绑定平台">
            <a-select-option value="taobao">淘宝</a-select-option>
            <a-select-option value="1688">1688</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="formData.remark" placeholder="请输入备注信息" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { taolinkShopApi } from '@/api/taolink/shop';
import { message } from 'ant-design-vue';

// 搜索表单
const searchForm = reactive({
  taobaoSellerNick: '',
  status: '',
});

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '50', '100'],
  onChange: (current, pageSize) => {
    pagination.current = current;
    pagination.pageSize = pageSize;
    loadShopList();
  },
});

// 数据列表
const dataList = ref<any[]>([]);
const loading = ref(false);
const router = useRouter();

// 模态框
const modalVisible = ref(false);
const modalTitle = ref('编辑店铺');
const formRef = ref();
const formData = reactive({
  id: '',
  status: 'active',
  bindPlatforms: [],
  remark: '',
});

// 表单规则
const formRules = {
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};

// 加载店铺列表
const loadShopList = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchForm,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    };
    const res = await taolinkShopApi.list(params);
    if (res.success) {
      dataList.value = res.result.records;
      pagination.total = res.result.total;
    }
  } catch (error) {
    message.error('加载店铺列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadShopList();
};

// 重置
const resetForm = () => {
  searchForm.taobaoSellerNick = '';
  searchForm.status = '';
  pagination.current = 1;
  loadShopList();
};

// 绑定店铺
const handleBindShop = async () => {
  try {
    const res = await taolinkShopApi.getAuthorizeUrl({});
    if (res.success) {
      window.location.href = res.result;
    } else {
      message.error('获取授权链接失败');
    }
  } catch (error) {
    message.error('网络错误，请稍后重试');
  }
};

// 编辑
const handleEdit = (record: any) => {
  modalTitle.value = '编辑店铺';
  Object.assign(formData, record);
  // 转换bindPlatforms为数组
  if (record.bindPlatforms) {
    formData.bindPlatforms = Array.isArray(record.bindPlatforms) 
      ? record.bindPlatforms 
      : JSON.parse(record.bindPlatforms);
  } else {
    formData.bindPlatforms = [];
  }
  modalVisible.value = true;
};

// 解绑
const handleUnbind = async (id: string) => {
  try {
    const res = await taolinkShopApi.unbind({ id });
    if (res.success) {
      message.success('解绑成功');
      loadShopList();
    }
  } catch (error) {
    message.error('解绑失败');
  }
};

// 查看详情
const handleDetail = (id: string) => {
  // 跳转到详情页
  router.push(`/taolink/shop/detail/${id}`);
};

// 重新授权
const handleReauthorize = async (id: string) => {
  try {
    const res = await taolinkShopApi.reauthorize({ id });
    if (res.success) {
      window.location.href = res.result;
    } else {
      message.error('获取授权链接失败');
    }
  } catch (error) {
    message.error('网络错误，请稍后重试');
  }
};

// 模态框确定
const handleModalOk = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
    // 处理bindPlatforms为字符串
    const submitData = {
      ...formData,
      bindPlatforms: JSON.stringify(formData.bindPlatforms),
    };
    const res = await taolinkShopApi.edit(submitData);
    if (res.success) {
      message.success('编辑成功');
      modalVisible.value = false;
      loadShopList();
    }
  } catch (error) {
    console.error('提交失败:', error);
  }
};

// 模态框取消
const handleModalCancel = () => {
  modalVisible.value = false;
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

// 获取状态颜色
const getStatusColor = (status: string) => {
  switch (status) {
    case 'active':
      return 'green';
    case 'disabled':
      return 'orange';
    case 'unbound':
      return 'red';
    default:
      return 'default';
  }
};

// 获取状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'active':
      return '活跃';
    case 'disabled':
      return '禁用';
    case 'unbound':
      return '已解绑';
    default:
      return status;
  }
};

// 获取API状态颜色
const getApiStatusColor = (expireAt: string) => {
  if (!expireAt) return 'red';
  const expireDate = new Date(expireAt);
  const now = new Date();
  const daysLeft = Math.floor((expireDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
  if (daysLeft < 0) return 'red';
  if (daysLeft < 7) return 'orange';
  return 'green';
};

// 获取API状态文本
const getApiStatusText = (expireAt: string) => {
  if (!expireAt) return '未授权';
  const expireDate = new Date(expireAt);
  const now = new Date();
  const daysLeft = Math.floor((expireDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
  if (daysLeft < 0) return '已过期';
  if (daysLeft < 7) return `即将过期(${daysLeft}天)`;
  return '正常';
};

// 表格列配置
const columns = [
  {
    title: '店铺名称',
    dataIndex: 'taobaoSellerNick',
    key: 'taobaoSellerNick',
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    slots: { customRender: 'status' },
  },
  {
    title: 'API状态',
    key: 'apiStatus',
    slots: { customRender: 'apiStatus' },
  },
  {
    title: '绑定平台',
    dataIndex: 'bindPlatforms',
    key: 'bindPlatforms',
    render: (text: any) => {
      if (!text) return '-';
      try {
        const platforms = Array.isArray(text) ? text : JSON.parse(text);
        return platforms.join(', ');
      } catch {
        return text;
      }
    },
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

// 初始加载
onMounted(() => {
  loadShopList();
});
</script>

<style scoped>
.shop-list-container {
  padding: 20px;
}

.search-area {
  margin-bottom: 16px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 4px;
}

.action-area {
  margin-bottom: 16px;
}
</style>
