<template>
  <div class="taolink-shop-detail">
    <a-card title="店铺详情" :bordered="false">
      <template #extra>
        <a-button @click="goBack" type="primary">返回列表</a-button>
      </template>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <a-spin tip="加载中..." size="large" />
      </div>
      
      <!-- 店铺信息 -->
      <div v-else class="shop-info">
        <!-- 基本信息 -->
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="店铺昵称">
            {{ shop.taobaoSellerNick }}
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(shop.status)">{{ getStatusText(shop.status) }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="绑定平台">
            <span v-if="shop.bindPlatforms">
              <a-tag v-for="platform in JSON.parse(shop.bindPlatforms)" :key="platform" color="blue">{{ platform }}</a-tag>
            </span>
            <span v-else>未绑定</span>
          </a-descriptions-item>
          <a-descriptions-item label="监控状态">
            <a-switch v-model:checked="shop.monitoringEnabled" disabled />
          </a-descriptions-item>
          <a-descriptions-item label="监控天数">
            {{ shop.monitoringDays || 7 }} 天
          </a-descriptions-item>
          <a-descriptions-item label="API授权过期时间">
            {{ shop.apiExpireAt ? formatDate(shop.apiExpireAt) : '未设置' }}
          </a-descriptions-item>
          <a-descriptions-item label="备注" :span="2">
            {{ shop.remark || '无' }}
          </a-descriptions-item>
        </a-descriptions>
        
        <!-- API配额信息 -->
        <a-card title="API配额信息" class="mt-4" :bordered="false">
          <a-descriptions :column="3" bordered>
            <a-descriptions-item label="今日调用次数">
              {{ apiStats.todayCalls || 0 }}
            </a-descriptions-item>
            <a-descriptions-item label="剩余配额">
              {{ apiStats.remainingQuota || 0 }}
            </a-descriptions-item>
            <a-descriptions-item label="配额上限">
              {{ apiStats.quotaLimit || 0 }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
        
        <!-- 监控指标摘要 -->
        <a-card title="监控指标摘要" class="mt-4" :bordered="false">
          <a-row :gutter="[16, 16]">
            <a-col :span="6">
              <a-statistic title="商品总数" :value="monitorStats.productCount || 0" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="在售商品数" :value="monitorStats.listedCount || 0" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="近7天订单数" :value="monitorStats.recentOrderCount || 0" />
            </a-col>
            <a-col :span="6">
              <a-statistic title="近7天GMV" :value="monitorStats.recentGmv || 0" suffix="元" />
            </a-col>
          </a-row>
        </a-card>
        
        <!-- 操作按钮 -->
        <div class="mt-4 flex justify-end">
          <a-button v-if="shop.status === 'active'" @click="handleUnbind" danger>解绑店铺</a-button>
          <a-button v-else @click="handleBind" type="primary" class="ml-2">重新绑定</a-button>
          <a-button v-if="shop.status === 'active'" @click="handleReauthorize" class="ml-2">重新授权</a-button>
          <a-button @click="handleEdit" class="ml-2">编辑信息</a-button>
        </div>
      </div>
    </a-card>
    
    <!-- 编辑对话框 -->
    <a-modal
      v-model:visible="editModalVisible"
      title="编辑店铺信息"
      @ok="handleEditOk"
    >
      <a-form :model="editForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="监控状态">
          <a-switch v-model:checked="editForm.monitoringEnabled" />
        </a-form-item>
        <a-form-item label="监控天数">
          <a-input-number v-model:value="editForm.monitoringDays" :min="1" :max="30" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="editForm.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message } from 'ant-design-vue';
import { format } from 'date-fns';
import { taolinkShopApi } from '@/api/taolink/shop';
import { taolinkMonitorApi } from '@/api/taolink/monitor';

const router = useRouter();
const route = useRoute();
const shopId = computed(() => route.params.id as string);

const loading = ref(false);
const shop = ref<any>({});
const apiStats = ref({});
const monitorStats = ref({});
const editModalVisible = ref(false);
const editForm = ref({});

// 获取店铺详情
const getShopDetail = async () => {
  loading.value = true;
  try {
    const response = await taolinkShopApi.queryById({ id: shopId.value });
    if (response.success) {
      shop.value = response.result;
      // 加载API统计信息
      loadApiStats();
      // 加载监控统计信息
      loadMonitorStats();
    } else {
      message.error('获取店铺详情失败');
    }
  } catch (error) {
    message.error('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 加载API统计信息
const loadApiStats = async () => {
  try {
    // 这里应该调用API统计接口，暂时使用模拟数据
    apiStats.value = {
      todayCalls: 123,
      remainingQuota: 877,
      quotaLimit: 1000
    };
  } catch (error) {
    console.error('加载API统计失败', error);
  }
};

// 加载监控统计信息
const loadMonitorStats = async () => {
  try {
    const response = await taolinkMonitorApi.getOverview({ shopId: shopId.value });
    if (response.success) {
      monitorStats.value = response.result;
    }
  } catch (error) {
    console.error('加载监控统计失败', error);
    // 使用模拟数据
    monitorStats.value = {
      productCount: 156,
      listedCount: 120,
      recentOrderCount: 89,
      recentGmv: 12500
    };
  }
};

// 状态颜色
const getStatusColor = (status: string) => {
  switch (status) {
    case 'active':
      return 'green';
    case 'disabled':
      return 'red';
    case 'unbound':
      return 'orange';
    default:
      return 'default';
  }
};

// 状态文本
const getStatusText = (status: string) => {
  switch (status) {
    case 'active':
      return '已激活';
    case 'disabled':
      return '已禁用';
    case 'unbound':
      return '未绑定';
    default:
      return status;
  }
};

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '';
  return format(new Date(date), 'yyyy-MM-dd HH:mm:ss');
};

// 返回列表
const goBack = () => {
  router.push('/taolink/shop');
};

// 解绑店铺
const handleUnbind = async () => {
  try {
    const response = await taolinkShopApi.unbind({ id: shopId.value });
    if (response.success) {
      message.success('解绑成功');
      getShopDetail();
    } else {
      message.error('解绑失败');
    }
  } catch (error) {
    message.error('网络错误，请稍后重试');
  }
};

// 重新绑定
const handleBind = async () => {
  try {
    const response = await taolinkShopApi.getAuthorizeUrl({});
    if (response.success) {
      window.location.href = response.result;
    } else {
      message.error('获取授权链接失败');
    }
  } catch (error) {
    message.error('网络错误，请稍后重试');
  }
};

// 重新授权
const handleReauthorize = async () => {
  try {
    const response = await taolinkShopApi.reauthorize({ id: shopId.value });
    if (response.success) {
      window.location.href = response.result;
    } else {
      message.error('获取授权链接失败');
    }
  } catch (error) {
    message.error('网络错误，请稍后重试');
  }
};

// 编辑信息
const handleEdit = () => {
  editForm.value = {
    monitoringEnabled: shop.value.monitoringEnabled,
    monitoringDays: shop.value.monitoringDays,
    remark: shop.value.remark
  };
  editModalVisible.value = true;
};

// 保存编辑
const handleEditOk = async () => {
  try {
    const response = await taolinkShopApi.edit({
      id: shopId.value,
      ...editForm.value
    });
    if (response.success) {
      message.success('更新成功');
      editModalVisible.value = false;
      getShopDetail();
    } else {
      message.error('更新失败');
    }
  } catch (error) {
    message.error('网络错误，请稍后重试');
  }
};

// 初始化
onMounted(() => {
  getShopDetail();
});
</script>

<style scoped>
.taolink-shop-detail {
  padding: 20px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.shop-info {
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
