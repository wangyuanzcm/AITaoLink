<template>
  <div class="taolink-product-detail">
    <a-card title="商品详情" :bordered="false">
      <template #extra>
        <a-button @click="goBack" type="primary">返回列表</a-button>
      </template>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <a-spin tip="加载中..." size="large" />
      </div>
      
      <!-- 商品信息 -->
      <div v-else class="product-info">
        <!-- 基本信息 -->
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="商品名称">
            {{ product.name }}
          </a-descriptions-item>
          <a-descriptions-item label="上下架状态">
            <a-tag :color="getListingStatusColor(product.listingStatus)">{{ getListingStatusText(product.listingStatus) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="商品状态">
            <a-tag :color="getStatusColor(product.status)">{{ getStatusText(product.status) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="所属店铺">
            {{ product.shopName || '未分配' }}
          </a-descriptions-item>
          <a-descriptions-item label="上架时间">
            {{ product.listedAt ? formatDate(product.listedAt) : '未上架' }}
          </a-descriptions-item>
          <a-descriptions-item label="下架时间">
            {{ product.delistedAt ? formatDate(product.delistedAt) : '未下架' }}
          </a-descriptions-item>
          <a-descriptions-item label="商品描述" :span="2">
            {{ product.description || '无' }}
          </a-descriptions-item>
        </a-descriptions>
        
        <!-- SKU列表 -->
        <a-card title="SKU列表" class="mt-4" :bordered="false">
          <a-table
            :columns="skuColumns"
            :data-source="productSkus"
            row-key="id"
            :pagination="false"
          >
            <template #inventoryStatus="{ record }">
              <a-tag :color="getInventoryStatusColor(record.inventoryStatus)">{{ record.inventoryStatus }}</a-tag>
            </template>
          </a-table>
        </a-card>
        
        <!-- 关联订单摘要 -->
        <a-card title="关联订单摘要" class="mt-4" :bordered="false">
          <a-row :gutter="[16, 16]">
            <a-col :span="6">
              <a-statistic title="近7天销量" :value="orderSummary.recentSales || 0" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="近7天销售额" :value="orderSummary.recentSalesAmount || 0" suffix="元" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="总销量" :value="orderSummary.totalSales || 0" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="总销售额" :value="orderSummary.totalSalesAmount || 0" suffix="元" />
            </a-col>
          </a-row>
          <a-button type="link" class="mt-4" @click="viewOrders">查看所有关联订单</a-button>
        </a-card>
        
        <!-- 关联货源 -->
        <a-card title="关联货源" class="mt-4" :bordered="false">
          <a-table
            :columns="sourceColumns"
            :data-source="sourceOffers"
            row-key="id"
            :pagination="false"
          >
            <template #action="{ record }">
              <a-button type="link" size="small" @click="viewSourceDetail(record.id)">查看详情</a-button>
            </template>
          </a-table>
        </a-card>
        
        <!-- 操作按钮 -->
        <div class="mt-4 flex justify-end">
          <a-button v-if="product.listingStatus === 'listed'" @click="handleDelist" danger>下架</a-button>
          <a-button v-else @click="handleList" type="primary" class="ml-2">上架</a-button>
          <a-button @click="handleEdit" class="ml-2">编辑</a-button>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message } from 'ant-design-vue';
import { format } from 'date-fns';
import { taolinkProductApi } from '@/api/taolink/product';

const router = useRouter();
const route = useRoute();
const productId = computed(() => route.params.id as string);

const loading = ref(false);
const product = ref<any>({});
const productSkus = ref<any[]>([]);
const sourceOffers = ref<any[]>([]);
const orderSummary = ref({});

// 获取商品详情
const getProductDetail = async () => {
  loading.value = true;
  try {
    const response = await taolinkProductApi.queryById({ id: productId.value });
    if (response.success) {
      product.value = response.result;
      // 加载SKU列表
      loadProductSkus();
      // 加载关联货源
      loadSourceOffers();
      // 加载订单摘要
      loadOrderSummary();
    } else {
      message.error('获取商品详情失败');
    }
  } catch (error) {
    message.error('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 加载SKU列表
const loadProductSkus = async () => {
  try {
    // 这里应该调用SKU列表接口，暂时使用模拟数据
    productSkus.value = [
      {
        id: '1',
        skuCode: 'SKU001',
        skuName: '红色-小码',
        price: 99.99,
        costPrice: 69.99,
        inventoryStatus: '有货',
        onHand: 100,
        reserved: 10,
        available: 90
      },
      {
        id: '2',
        skuCode: 'SKU002',
        skuName: '蓝色-中码',
        price: 109.99,
        costPrice: 79.99,
        inventoryStatus: '缺货',
        onHand: 0,
        reserved: 0,
        available: 0
      },
      {
        id: '3',
        skuCode: 'SKU003',
        skuName: '黑色-大码',
        price: 119.99,
        costPrice: 89.99,
        inventoryStatus: '有货',
        onHand: 50,
        reserved: 5,
        available: 45
      }
    ];
  } catch (error) {
    console.error('加载SKU列表失败', error);
  }
};

// 加载关联货源
const loadSourceOffers = async () => {
  try {
    // 这里应该调用货源列表接口，暂时使用模拟数据
    sourceOffers.value = [
      {
        id: '1',
        platform: '1688',
        sourceId: '123456',
        sourceName: '优质供应商',
        price: 69.99,
        minOrder: 10,
        link: 'https://1688.com/product/123456.html'
      },
      {
        id: '2',
        platform: '1688',
        sourceId: '789012',
        sourceName: '备用供应商',
        price: 74.99,
        minOrder: 5,
        link: 'https://1688.com/product/789012.html'
      }
    ];
  } catch (error) {
    console.error('加载关联货源失败', error);
  }
};

// 加载订单摘要
const loadOrderSummary = async () => {
  try {
    // 这里应该调用订单摘要接口，暂时使用模拟数据
    orderSummary.value = {
      recentSales: 25,
      recentSalesAmount: 2899.75,
      totalSales: 120,
      totalSalesAmount: 14399.40
    };
  } catch (error) {
    console.error('加载订单摘要失败', error);
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

// 商品状态颜色
const getStatusColor = (status: string) => {
  switch (status) {
    case 'normal':
      return 'green';
    case 'disabled':
      return 'red';
    default:
      return 'default';
  }
};

// 商品状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'normal':
      return '正常';
    case 'disabled':
      return '禁用';
    default:
      return status;
  }
};

// 库存状态颜色
const getInventoryStatusColor = (status: string) => {
  switch (status) {
    case '有货':
      return 'green';
    case '缺货':
      return 'red';
    case '即将缺货':
      return 'orange';
    default:
      return 'default';
  }
};

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '';
  return format(new Date(date), 'yyyy-MM-dd HH:mm:ss');
};

// 返回列表
const goBack = () => {
  router.push('/taolink/product');
};

// 下架商品
const handleDelist = async () => {
  try {
    const response = await taolinkProductApi.delisted({ id: productId.value });
    if (response.success) {
      message.success('下架成功');
      getProductDetail();
    } else {
      message.error('下架失败');
    }
  } catch (error) {
    message.error('网络错误，请稍后重试');
  }
};

// 上架商品
const handleList = async () => {
  try {
    const response = await taolinkProductApi.listed({ id: productId.value });
    if (response.success) {
      message.success('上架成功');
      getProductDetail();
    } else {
      message.error('上架失败');
    }
  } catch (error) {
    message.error('网络错误，请稍后重试');
  }
};

// 编辑商品
const handleEdit = () => {
  router.push(`/taolink/product/form/${productId.value}`);
};

// 查看关联订单
const viewOrders = () => {
  router.push(`/taolink/order?productId=${productId.value}`);
};

// 查看货源详情
const viewSourceDetail = (sourceId: string) => {
  // 跳转到货源详情页
  console.log('查看货源详情:', sourceId);
};

// SKU列定义
const skuColumns = [
  {
    title: 'SKU编码',
    dataIndex: 'skuCode',
    key: 'skuCode',
  },
  {
    title: 'SKU名称',
    dataIndex: 'skuName',
    key: 'skuName',
  },
  {
    title: '售价',
    dataIndex: 'price',
    key: 'price',
    render: (text: number) => `¥${text.toFixed(2)}`,
  },
  {
    title: '成本价',
    dataIndex: 'costPrice',
    key: 'costPrice',
    render: (text: number) => `¥${text.toFixed(2)}`,
  },
  {
    title: '库存状态',
    key: 'inventoryStatus',
    slots: { customRender: 'inventoryStatus' },
  },
  {
    title: '在手库存',
    dataIndex: 'onHand',
    key: 'onHand',
  },
  {
    title: '预留库存',
    dataIndex: 'reserved',
    key: 'reserved',
  },
  {
    title: '可用库存',
    dataIndex: 'available',
    key: 'available',
  },
];

// 货源列定义
const sourceColumns = [
  {
    title: '平台',
    dataIndex: 'platform',
    key: 'platform',
  },
  {
    title: '货源ID',
    dataIndex: 'sourceId',
    key: 'sourceId',
  },
  {
    title: '供应商',
    dataIndex: 'sourceName',
    key: 'sourceName',
  },
  {
    title: '采购价',
    dataIndex: 'price',
    key: 'price',
    render: (text: number) => `¥${text.toFixed(2)}`,
  },
  {
    title: '起订量',
    dataIndex: 'minOrder',
    key: 'minOrder',
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
  },
];

// 初始化
onMounted(() => {
  getProductDetail();
});
</script>

<style scoped>
.taolink-product-detail {
  padding: 20px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.product-info {
  margin-top: 20px;
}

.mt-4 {
  margin-top: 16px;
}

.flex {
  display: flex;
}

.justify-end {
  justify-content: flex-end;
}

.ml-2 {
  margin-left: 8px;
}
</style>
