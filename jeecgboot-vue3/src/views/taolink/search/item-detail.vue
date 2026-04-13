<template>
  <div class="item-detail">
    <a-card title="商品详情" :bordered="false">
      <!-- 商品基本信息 -->
      <div class="item-info" v-if="itemDetail">
        <a-row :gutter="[24, 24]">
          <a-col :span="8">
            <a-carousel autoplay>
              <div v-for="(pic, index) in itemDetail.pics" :key="index">
                <img :src="pic" style="width: 100%; height: 300px; object-fit: contain" />
              </div>
            </a-carousel>
          </a-col>
          <a-col :span="16">
            <h2 class="item-title">{{ itemDetail.title }}</h2>
            <div class="item-meta">
              <div class="meta-item">
                <span class="label">价格：</span>
                <span class="value price">¥{{ itemDetail.price }}</span>
              </div>
              <div class="meta-item">
                <span class="label">卖家：</span>
                <span class="value">{{ itemDetail.seller_nick }}</span>
              </div>
              <div class="meta-item" v-if="itemDetail.location">
                <span class="label">发货地：</span>
                <span class="value">{{ itemDetail.location }}</span>
              </div>
              <div class="meta-item" v-if="itemDetail.min_num">
                <span class="label">起批量：</span>
                <span class="value">{{ itemDetail.min_num }}</span>
              </div>
              <div class="meta-item">
                <span class="label">平台：</span>
                <span class="value">{{ platform }}</span>
              </div>
            </div>
            <div class="item-actions">
              <a-button type="primary" @click="handleImport">
                <a-icon type="download" />
                一键导入
              </a-button>
              <a-button style="margin-left: 8px" @click="handleViewOriginal">
                <a-icon type="link" />
                查看原始链接
              </a-button>
              <a-button style="margin-left: 8px" @click="handleRefresh">
                <a-icon type="reload" />
                刷新商品信息
              </a-button>
            </div>
          </a-col>
        </a-row>

        <!-- 商品详情 -->
        <div class="item-detail-content" style="margin-top: 24px">
          <a-card title="商品描述" :bordered="false">
            <div v-html="itemDetail.desc || '暂无描述'" style="white-space: pre-wrap"></div>
          </a-card>
        </div>

        <!-- 采购建议 -->
        <div class="purchase-suggestion" style="margin-top: 24px">
          <a-card title="采购建议" :bordered="false">
            <a-list item-layout="horizontal">
              <a-list-item>
                <a-list-item-meta
                  title="建议采购数量"
                  description="根据历史销售数据，建议采购 10-20 件"
                />
              </a-list-item>
              <a-list-item>
                <a-list-item-meta
                  title="建议销售价格"
                  description="建议售价：¥{{ (Number(itemDetail.price) * 1.5).toFixed(2) }}"
                />
              </a-list-item>
              <a-list-item>
                <a-list-item-meta
                  title="预计利润"
                  description="预计利润率：33%"
                />
              </a-list-item>
            </a-list>
          </a-card>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-else-if="loading" class="loading">
        <a-spin tip="加载中..." />
      </div>

      <!-- 无数据提示 -->
      <div v-else class="no-data">
        <a-empty description="暂无商品信息" />
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { searchApi } from '@/api/taolink/search';
import { message } from 'ant-design-vue';
import { useRoute } from 'vue-router';

const route = useRoute();

// 商品详情
const itemDetail = ref<any>(null);
// 加载状态
const loading = ref(false);
// 平台
const platform = ref('1688');
// 商品ID
const numIid = ref('');

// 加载商品详情
const loadItemDetail = async () => {
  loading.value = true;
  try {
    // 这里应该调用商品详情接口
    // 实际项目中需要根据平台和商品ID调用相应的接口
    // 先从缓存中获取，缓存不存在时再调用接口
    const res = await searchApi.getItemDetail({
      platform: platform.value,
      num_iid: numIid.value
    });
    itemDetail.value = res;
  } catch (error) {
    message.error('加载商品详情失败');
  } finally {
    loading.value = false;
  }
};

// 一键导入
const handleImport = async () => {
  if (!itemDetail.value) return;

  loading.value = true;
  try {
    await searchApi.importToSourceOffer({
      items: [itemDetail.value],
    });
    message.success('导入成功');
  } catch (error) {
    message.error('导入失败');
  } finally {
    loading.value = false;
  }
};

// 查看原始链接
const handleViewOriginal = () => {
  if (itemDetail.value?.detail_url) {
    window.open(itemDetail.value.detail_url, '_blank');
  }
};

// 初始化
onMounted(() => {
  // 从路由参数获取平台和商品ID
  platform.value = route.query.platform as string || '1688';
  numIid.value = route.params.id as string || '';
  if (numIid.value) {
    loadItemDetail();
  }
});

// 监听路由参数变化
watch(() => [route.params.id, route.query.platform], ([newId, newPlatform]) => {
  if (newId) {
    platform.value = newPlatform as string || '1688';
    numIid.value = newId as string;
    loadItemDetail();
  }
}, { deep: true });

// 刷新商品信息
const handleRefresh = async () => {
  if (numIid.value) {
    await loadItemDetail();
  }
};
</script>

<style scoped>
.item-detail {
  padding: 20px;
}

.item-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 16px;
}

.item-meta {
  margin-bottom: 20px;
}

.meta-item {
  margin-bottom: 8px;
}

.label {
  font-weight: bold;
  margin-right: 8px;
}

.price {
  color: #ff4d4f;
  font-weight: bold;
}

.item-actions {
  margin-top: 20px;
}

.loading {
  padding: 40px 0;
  text-align: center;
}

.no-data {
  padding: 40px 0;
  text-align: center;
}
</style>
