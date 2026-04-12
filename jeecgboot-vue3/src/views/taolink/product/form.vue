<template>
  <div class="product-form-container">
    <a-card title="{{ isEdit ? '编辑商品' : '新增商品' }}" :bordered="false">
      <template #extra>
        <a-button @click="goBack">返回列表</a-button>
      </template>
      
      <a-form :model="formData" :rules="formRules" ref="formRef">
        <a-form-item label="商品名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入商品名称" />
        </a-form-item>
        
        <a-form-item label="所属店铺" name="shopId">
          <a-select v-model:value="formData.shopId" placeholder="请选择店铺">
            <a-select-option v-for="shop in shopList" :key="shop.id" :value="shop.id">{{ shop.taobaoSellerNick }}</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="商品描述">
          <a-textarea v-model:value="formData.description" placeholder="请输入商品描述" :rows="4" />
        </a-form-item>
        
        <a-form-item label="商品状态" name="status">
          <a-select v-model:value="formData.status">
            <a-select-option value="normal">正常</a-select-option>
            <a-select-option value="disabled">禁用</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="上下架状态" name="listingStatus">
          <a-select v-model:value="formData.listingStatus">
            <a-select-option value="draft">草稿</a-select-option>
            <a-select-option value="listed">已上架</a-select-option>
            <a-select-option value="delisted">已下架</a-select-option>
          </a-select>
        </a-form-item>
        
        <!-- SKU配置 -->
        <a-form-item label="SKU配置">
          <a-button type="primary" @click="addSku" class="mb-2">添加SKU</a-button>
          <a-table
            :columns="skuColumns"
            :data-source="formData.skus"
            row-key="skuCode"
            :pagination="false"
          >
            <template #price="{ record }">
              <a-input-number v-model:value="record.price" :min="0" :step="0.01" style="width: 100%" />
            </template>
            <template #costPrice="{ record }">
              <a-input-number v-model:value="record.costPrice" :min="0" :step="0.01" style="width: 100%" />
            </template>
            <template #action="{ record, index }">
              <a-button type="link" danger @click="removeSku(index)">删除</a-button>
            </template>
          </a-table>
        </a-form-item>
        
        <a-form-item>
          <a-button type="primary" @click="handleSubmit">保存</a-button>
          <a-button @click="goBack" class="ml-2">取消</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message } from 'ant-design-vue';
import { taolinkProductApi } from '@/api/taolink/product';
import { taolinkShopApi } from '@/api/taolink/shop';

const router = useRouter();
const route = useRoute();
const productId = computed(() => route.params.id as string);
const isEdit = computed(() => !!productId.value);

const formRef = ref();
const loading = ref(false);
const shopList = ref<any[]>([]);

// 表单数据
const formData = reactive({
  id: '',
  name: '',
  shopId: '',
  description: '',
  status: 'normal',
  listingStatus: 'draft',
  skus: [
    {
      skuCode: 'SKU001',
      skuName: '默认SKU',
      price: 0,
      costPrice: 0
    }
  ]
});

// 表单规则
const formRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  shopId: [{ required: true, message: '请选择所属店铺', trigger: 'change' }],
  status: [{ required: true, message: '请选择商品状态', trigger: 'change' }],
  listingStatus: [{ required: true, message: '请选择上下架状态', trigger: 'change' }],
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

// 加载商品详情
const loadProductDetail = async () => {
  if (!isEdit.value) return;
  loading.value = true;
  try {
    const res = await taolinkProductApi.queryById({ id: productId.value });
    if (res.success) {
      const product = res.result;
      Object.assign(formData, product);
      // 处理SKU数据
      if (!formData.skus || formData.skus.length === 0) {
        formData.skus = [
          {
            skuCode: 'SKU001',
            skuName: '默认SKU',
            price: 0,
            costPrice: 0
          }
        ];
      }
    }
  } catch (error) {
    message.error('加载商品详情失败');
  } finally {
    loading.value = false;
  }
};

// 添加SKU
const addSku = () => {
  const newSku = {
    skuCode: `SKU${String(formData.skus.length + 1).padStart(3, '0')}`,
    skuName: `SKU ${formData.skus.length + 1}`,
    price: 0,
    costPrice: 0
  };
  formData.skus.push(newSku);
};

// 删除SKU
const removeSku = (index: number) => {
  if (formData.skus.length > 1) {
    formData.skus.splice(index, 1);
  } else {
    message.warning('至少保留一个SKU');
  }
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  loading.value = true;
  try {
    await formRef.value.validate();
    const submitData = { ...formData };
    let res;
    if (isEdit.value) {
      res = await taolinkProductApi.edit(submitData);
    } else {
      res = await taolinkProductApi.add(submitData);
    }
    if (res.success) {
      message.success(isEdit.value ? '编辑成功' : '新增成功');
      goBack();
    }
  } catch (error) {
    console.error('提交失败:', error);
    message.error('提交失败');
  } finally {
    loading.value = false;
  }
};

// 返回列表
const goBack = () => {
  router.push('/taolink/product');
};

// SKU列定义
const skuColumns = [
  {
    title: 'SKU编码',
    dataIndex: 'skuCode',
    key: 'skuCode',
    width: 120,
  },
  {
    title: 'SKU名称',
    dataIndex: 'skuName',
    key: 'skuName',
    width: 150,
  },
  {
    title: '售价',
    key: 'price',
    slots: { customRender: 'price' },
    width: 120,
  },
  {
    title: '成本价',
    key: 'costPrice',
    slots: { customRender: 'costPrice' },
    width: 120,
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
    width: 80,
  },
];

// 初始化
onMounted(() => {
  loadShopList();
  loadProductDetail();
});
</script>

<style scoped>
.product-form-container {
  padding: 20px;
}

.mb-2 {
  margin-bottom: 8px;
}

.ml-2 {
  margin-left: 8px;
}
</style>
