<template>
  <div class="search-center">
    <a-card title="统一搜索中心" :bordered="false">
      <!-- 搜索表单 -->
      <div class="search-form">
        <a-row :gutter="[16, 16]">
          <a-col :span="16">
            <a-input
              v-model:value="searchForm.keyword"
              placeholder="请输入搜索关键词"
              style="width: 100%"
              @keyup.enter="handleSearch"
            >
              <template #suffix>
                <a-button type="primary" @click="handleSearch">
                  <a-icon type="search" />
                </a-button>
              </template>
            </a-input>
          </a-col>
          <a-col :span="4">
            <a-select
              v-model:value="searchForm.platform"
              placeholder="选择平台"
              style="width: 100%"
            >
              <a-option value="1688">1688</a-option>
              <a-option value="taobao">淘宝</a-option>
            </a-select>
          </a-col>
          <a-col :span="4">
            <a-button type="default" @click="handleImportAll" :disabled="!searchResult.items || searchResult.items.length === 0">
              <a-icon type="download" />
              批量导入
            </a-button>
          </a-col>
        </a-row>
      </div>

      <!-- 搜索结果 -->
      <div class="search-results" v-if="searchResult.items && searchResult.items.length > 0">
        <a-card title="搜索结果" :bordered="false" style="margin-top: 20px">
          <a-list
            :data-source="searchResult.items"
            :loading="loading"
            :pagination="{
              current: searchForm.pageNo,
              pageSize: searchForm.pageSize,
              total: searchResult.total || 0,
              onChange: handlePageChange
            }"
          >
            <template #renderItem="{ item }">
              <a-list-item key="item.num_iid">
                <a-list-item-meta
                  :avatar="item.pic_url ? { src: item.pic_url } : null"
                >
                  <template #title>
                    <a href="javascript:;" @click="handleViewDetail(item)">
                      {{ item.title }}
                    </a>
                  </template>
                  <template #description>
                    <div class="item-description">
                      <div class="price">价格：¥{{ item.price }}</div>
                      <div class="seller">卖家：{{ item.seller_nick }}</div>
                      <div class="location" v-if="item.location">发货地：{{ item.location }}</div>
                    </div>
                  </template>
                </a-list-item-meta>
                <div class="item-actions">
                  <a-button type="primary" @click="handleImportItem(item)">
                    导入
                  </a-button>
                </div>
              </a-list-item>
            </template>
          </a-list>
        </a-card>
      </div>

      <!-- 无结果提示 -->
      <div v-else-if="!loading" class="no-result">
        <a-empty description="暂无搜索结果" />
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { searchApi } from '@/api/taolink/search';
import { message } from 'ant-design-vue';

// 搜索表单
const searchForm = reactive({
  keyword: '',
  platform: '1688',
  pageNo: 1,
  pageSize: 10,
});

// 搜索结果
const searchResult = ref({
  items: [],
  total: 0,
  cacheHit: false,
});

// 加载状态
const loading = ref(false);

// 搜索
const handleSearch = async () => {
  if (!searchForm.keyword) {
    message.warning('请输入搜索关键词');
    return;
  }

  loading.value = true;
  try {
    const res = await searchApi.unifiedSearch({
      platform: searchForm.platform,
      keyword: searchForm.keyword,
      pageNo: searchForm.pageNo,
      pageSize: searchForm.pageSize,
    });
    if (res.success) {
      searchResult.value = res.result;
      if (res.result.cacheHit) {
        message.success('缓存命中');
      }
    } else {
      message.error('搜索失败：' + res.message);
    }
  } catch (error) {
    message.error('搜索失败');
  } finally {
    loading.value = false;
  }
};

// 分页
const handlePageChange = (page: number) => {
  searchForm.pageNo = page;
  handleSearch();
};

// 查看详情
const handleViewDetail = (item: any) => {
  console.log('查看详情:', item);
  // 跳转到详情页
  // router.push(`/taolink/search/item-detail/${item.num_iid}?platform=${searchForm.platform}`);
};

// 导入单个商品
const handleImportItem = async (item: any) => {
  loading.value = true;
  try {
    const res = await searchApi.importToSourceOffer({
      items: [item],
    });
    if (res.success) {
      message.success('导入成功');
    } else {
      message.error('导入失败：' + res.message);
    }
  } catch (error) {
    message.error('导入失败');
  } finally {
    loading.value = false;
  }
};

// 批量导入
const handleImportAll = async () => {
  loading.value = true;
  try {
    const res = await searchApi.importToSourceOffer({
      items: searchResult.value.items,
    });
    if (res.success) {
      message.success('批量导入成功');
    } else {
      message.error('批量导入失败：' + res.message);
    }
  } catch (error) {
    message.error('批量导入失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.search-center {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.item-description {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 8px;
}

.price {
  color: #ff4d4f;
  font-weight: bold;
}

.item-actions {
  display: flex;
  align-items: center;
}

.no-result {
  padding: 40px 0;
  text-align: center;
}
</style>
