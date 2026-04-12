<template>
  <a-dropdown trigger="click" @select="handleSelect">
    <div class="shop-selector">
      <a-avatar :size="32" :src="selectedShop?.avatar || defaultAvatar" style="margin-right: 8px" />
      <span class="shop-name">{{ selectedShop?.taobaoSellerNick || '选择店铺' }}</span>
      <a-icon type="down" />
    </div>
    <template #overlay>
      <a-menu>
        <a-menu-item key="add" @click="handleAddShop">
          <a-icon type="plus" /> 绑定新店铺
        </a-menu-item>
        <a-menu-divider />
        <a-menu-item v-if="shopList.length === 0" disabled>
          暂无店铺，请先绑定
        </a-menu-item>
        <a-menu-item
          v-for="shop in shopList"
          :key="shop.id"
          @click="selectShop(shop)"
        >
          <a-avatar :size="24" :src="shop.avatar || defaultAvatar" style="margin-right: 8px" />
          <span>{{ shop.taobaoSellerNick }}</span>
          <a-tag v-if="shop.status === 'active'" color="green" size="small" style="margin-left: 8px">
            活跃
          </a-tag>
          <a-tag v-else-if="shop.status === 'disabled'" color="orange" size="small" style="margin-left: 8px">
            禁用
          </a-tag>
          <a-tag v-else color="red" size="small" style="margin-left: 8px">
            已解绑
          </a-tag>
        </a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { shopApi } from '@/api/taolink/shop';
import { useTaolinkStore } from '@/store/modules/taolink';
import { message } from 'ant-design-vue';

const taolinkStore = useTaolinkStore();
const shopList = ref<any[]>([]);
const defaultAvatar = 'https://gw.alipayobjects.com/zos/rmsportal/KDpgvguMpGfqaHPjicRK.svg';

// 计算当前选中的店铺
const selectedShop = computed(() => {
  if (!taolinkStore.currentShopId) return null;
  return shopList.value.find(shop => shop.id === taolinkStore.currentShopId);
});

// 加载店铺列表
const loadShopList = async () => {
  try {
    const res = await shopApi.getShopList({});
    if (res.success) {
      shopList.value = res.result.records || [];
    }
  } catch (error) {
    message.error('加载店铺列表失败');
  }
};

// 选择店铺
const selectShop = (shop: any) => {
  taolinkStore.setCurrentShopId(shop.id);
  message.success(`已切换到店铺：${shop.taobaoSellerNick}`);
};

// 处理选择事件
const handleSelect = (key: string) => {
  // 这里可以处理下拉菜单的选择事件
};

// 绑定新店铺
const handleAddShop = () => {
  // 这里可以跳转到店铺绑定页面
  console.log('绑定新店铺');
};

// 初始化
onMounted(() => {
  loadShopList();
});
</script>

<style scoped>
.shop-selector {
  display: flex;
  align-items: center;
  padding: 0 16px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
}

.shop-selector:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.shop-name {
  margin-right: 8px;
  font-size: 14px;
  font-weight: 500;
}
</style>
