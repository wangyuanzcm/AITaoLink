<template>
  <div class="product-list-container">
    <a-card title="商品管理" :bordered="false">
      <!-- 搜索区域 -->
      <div class="search-area">
        <a-form :model="searchForm" layout="inline" @keyup.enter="handleSearch">
          <a-form-item label="商品名称">
            <a-input v-model:value="searchForm.name" placeholder="请输入商品名称" />
          </a-form-item>
          <a-form-item label="上下架状态">
            <a-select v-model:value="searchForm.listingStatus" placeholder="请选择状态">
              <a-select-option value="listed">已上架</a-select-option>
              <a-select-option value="delisted">已下架</a-select-option>
              <a-select-option value="draft">草稿</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="所属店铺">
            <a-select v-model:value="searchForm.shopId" placeholder="请选择店铺">
              <a-select-option v-for="shop in shopList" :key="shop.id" :value="shop.id">{{ shop.taobaoSellerNick }}</a-select-option>
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
        <a-button type="primary" @click="handleAdd">新增商品</a-button>
        <a-button @click="handleBatchList" class="ml-2" v-if="selectedRowKeys.length > 0">批量上架</a-button>
        <a-button @click="handleBatchDelist" class="ml-2" v-if="selectedRowKeys.length > 0" danger>批量下架</a-button>
      </div>

      <!-- 商品列表 -->
      <a-table
        :columns="columns"
        :data-source="dataList"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        :row-selection="rowSelection"
      >
        <template #listingStatus="{ record }">
          <a-tag :color="getListingStatusColor(record.listingStatus)">{{ getListingStatusText(record.listingStatus) }}</a-tag>
        </template>
        <template #action="{ record }">
          <a-button type="link" @click="handleEdit(record.id)">编辑</a-button>
          <a-button type="link" @click="handleDetail(record.id)">详情</a-button>
          <a-button type="link" v-if="record.listingStatus === 'listed'" @click="handleDelist(record.id)" danger>下架</a-button>
          <a-button type="link" v-else @click="handleList(record.id)" class="ml-2">上架</a-button>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { taolinkProductApi } from '@/api/taolink/product';
import { taolinkShopApi } from '@/api/taolink/shop';

const router = useRouter();
const loading = ref(false);
const dataList = ref<any[]>([]);
const shopList = ref<any[]>([]);
const selectedRowKeys = ref<string[]>([]);

// 搜索表单
const searchForm = reactive({
  name: '',
  listingStatus: '',
  shopId: '',
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
    loadProductList();
  },
});

// 行选择配置
const rowSelection = {
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => {
    selectedRowKeys.value = keys;
  },
};

// 加载商品列表
const loadProductList = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchForm,
      pageNo: pagination.current,
      pageSize: pagination.pageSize,
    };
    const res = await taolinkProductApi.list(params);
    if (res.success) {
      dataList.value = res.result.records;
      pagination.total = res.result.total;
    }
  } catch (error) {
    message.error('加载商品列表失败');
  } finally {
    loading.value = false;
  }
};

// 加载店铺列表
const loadShopList = async () => {
  try {
    const res = await taolinkShopApi.list({});
    if (res.success) {
      shopList.value = res.result.records;
    }
  } catch (error) {
    console.error('加载店铺列表失败', error);
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  loadProductList();
};

// 重置
const resetForm = () => {
  searchForm.name = '';
  searchForm.listingStatus = '';
  searchForm.shopId = '';
  pagination.current = 1;
  loadProductList();
};

// 新增商品
const handleAdd = () => {
  router.push('/taolink/product/form');
};

// 编辑商品
const handleEdit = (id: string) => {
  router.push(`/taolink/product/form/${id}`);
};

// 查看详情
const handleDetail = (id: string) => {
  router.push(`/taolink/product/detail/${id}`);
};

// 上架商品
const handleList = async (id: string) => {
  try {
    const res = await taolinkProductApi.listed({ id });
    if (res.success) {
      message.success('上架成功');
      loadProductList();
    }
  } catch (error) {
    message.error('上架失败');
  }
};

// 下架商品
const handleDelist = async (id: string) => {
  try {
    const res = await taolinkProductApi.delisted({ id });
    if (res.success) {
      message.success('下架成功');
      loadProductList();
    }
  } catch (error) {
    message.error('下架失败');
  }
};

// 批量上架
const handleBatchList = async () => {
  try {
    // 这里应该调用批量上架接口，暂时模拟
    message.success(`批量上架 ${selectedRowKeys.value.length} 个商品`);
    selectedRowKeys.value = [];
    loadProductList();
  } catch (error) {
    message.error('批量上架失败');
  }
};

// 批量下架
const handleBatchDelist = async () => {
  try {
    // 这里应该调用批量下架接口，暂时模拟
    message.success(`批量下架 ${selectedRowKeys.value.length} 个商品`);
    selectedRowKeys.value = [];
    loadProductList();
  } catch (error) {
    message.error('批量下架失败');
  }
};

// 上下架状态颜色
const getListingStatusColor = (status: string) => {
  switch (status) {
    case 'listed':
      return 'green';
    case 'delisted':
      return 'red';
    case 'draft':
      return 'orange';
    default:
      return 'default';
  }
};

// 上下架状态文本
const getListingStatusText = (status: string) => {
  switch (status) {
    case 'listed':
      return '已上架';
    case 'delisted':
      return '已下架';
    case 'draft':
      return '草稿';
    default:
      return status;
  }
};

// 表格列配置
const columns = [
  {
    title: '商品名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '上下架状态',
    dataIndex: 'listingStatus',
    key: 'listingStatus',
    slots: { customRender: 'listingStatus' },
  },
  {
    title: '所属店铺',
    dataIndex: 'shopName',
    key: 'shopName',
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

// 初始化
onMounted(() => {
  loadShopList();
  loadProductList();
});
</script>

<style scoped>
.product-list-container {
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

.ml-2 {
  margin-left: 8px;
}
</style>
