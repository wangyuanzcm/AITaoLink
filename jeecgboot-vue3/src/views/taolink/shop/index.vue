<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleBindShop">绑定新店</a-button>
      </template>
      <template #status="{ record }">
        <a-tag :color="getStatusColor(record.status)">{{ getStatusText(record.status) }}</a-tag>
      </template>
      <template #apiStatus="{ record }">
        <a-tag :color="getApiStatusColor(record.apiExpireAt)">{{ getApiStatusText(record.apiExpireAt) }}</a-tag>
      </template>
      <template #bindPlatforms="{ text }">
        <span>{{ formatBindPlatforms(text) }}</span>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableActions(record)" />
      </template>
    </BasicTable>

    <BasicModal @register="registerModal" title="编辑店铺" width="600px" @ok="handleModalOk" destroyOnClose>
      <BasicForm @register="registerForm" />
    </BasicModal>
  </div>
</template>

<script setup lang="ts">
import type { ActionItem, BasicColumn, FormSchema } from '/@/components/Table';
import { ref, unref } from 'vue';
import { useRouter } from 'vue-router';
import { BasicTable, TableAction } from '/@/components/Table';
import { BasicModal, useModal } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import { taolinkShopApi } from '@/api/taolink/shop';

defineOptions({ name: 'TaolinkShopIndex' });

const router = useRouter();
const { createMessage } = useMessage();

const columns: BasicColumn[] = [
  {
    title: '店铺名称',
    dataIndex: 'taobaoSellerNick',
    width: 200,
    align: 'left',
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 100,
    slots: { customRender: 'status' },
  },
  {
    title: 'API状态',
    dataIndex: 'apiExpireAt',
    width: 150,
    slots: { customRender: 'apiStatus' },
  },
  {
    title: '绑定平台',
    dataIndex: 'bindPlatforms',
    width: 160,
    slots: { customRender: 'bindPlatforms' },
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
  },
];

const searchFormSchema: FormSchema[] = [
  {
    field: 'taobaoSellerNick',
    label: '店铺名称',
    component: 'Input',
    componentProps: {
      placeholder: '请输入店铺名称',
    },
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: '状态',
    component: 'Select',
    componentProps: {
      allowClear: true,
      placeholder: '请选择状态',
      options: [
        { label: '活跃', value: 'active' },
        { label: '禁用', value: 'disabled' },
        { label: '已解绑', value: 'unbound' },
      ],
    },
    colProps: { span: 8 },
  },
];

const { tableContext } = useListPage({
  designScope: 'taolink-shop-template',
  tableProps: {
    title: '店铺管理',
    api: taolinkShopApi.list,
    columns,
    formConfig: {
      schemas: searchFormSchema,
    },
    actionColumn: {
      width: 220,
      fixed: 'right',
    },
  },
});

const [registerTable, { reload }] = tableContext;

const editFormSchema: FormSchema[] = [
  {
    field: 'status',
    label: '状态',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '活跃', value: 'active' },
        { label: '禁用', value: 'disabled' },
      ],
    },
  },
  {
    field: 'bindPlatforms',
    label: '绑定平台',
    component: 'Select',
    componentProps: {
      mode: 'tags',
      placeholder: '请选择绑定平台',
      options: [
        { label: '淘宝', value: 'taobao' },
        { label: '1688', value: '1688' },
      ],
    },
  },
  {
    field: 'remark',
    label: '备注',
    component: 'InputTextArea',
    componentProps: {
      placeholder: '请输入备注信息',
    },
  },
];

const currentEditId = ref<string>('');
const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
  schemas: editFormSchema,
  showActionButtonGroup: false,
});

const [registerModal, { openModal, closeModal, setModalProps }] = useModal();

const parseBindPlatforms = (value: unknown): string[] => {
  if (!value) return [];
  if (Array.isArray(value)) return value.map((v) => String(v));
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value);
      if (Array.isArray(parsed)) return parsed.map((v) => String(v));
      return [value];
    } catch {
      return [value];
    }
  }
  return [];
};

const formatBindPlatforms = (text: unknown) => {
  const list = parseBindPlatforms(text);
  return list.length ? list.join(', ') : '-';
};

const getAuthorizeUrlFromResult = (res: unknown) => {
  if (!res) return '';
  if (typeof res === 'string') return res;
  if (typeof res === 'object') {
    const anyRes = res as any;
    return anyRes.url || anyRes.authorizeUrl || '';
  }
  return '';
};

const handleBindShop = async () => {
  try {
    const res = await taolinkShopApi.getAuthorizeUrl({});
    const url = getAuthorizeUrlFromResult(res);
    if (!url) {
      createMessage.error('获取授权链接失败');
      return;
    }
    window.location.href = url;
  } catch {
    createMessage.error('网络错误，请稍后重试');
  }
};

const handleUnbind = async (id: string) => {
  try {
    await taolinkShopApi.unbind({ id });
    createMessage.success('解绑成功');
    await reload();
  } catch {
    createMessage.error('解绑失败');
  }
};

const handleDetail = (id: string) => {
  router.push(`/taolink/shop/detail/${id}`);
};

const handleReauthorize = async (id: string) => {
  try {
    const res = await taolinkShopApi.reauthorize({ id });
    const url = getAuthorizeUrlFromResult(res);
    if (!url) {
      createMessage.error('获取授权链接失败');
      return;
    }
    window.location.href = url;
  } catch {
    createMessage.error('网络错误，请稍后重试');
  }
};

const handleEdit = async (record: any) => {
  await resetFields();
  currentEditId.value = record?.id || '';
  await setFieldsValue({
    status: record?.status,
    bindPlatforms: parseBindPlatforms(record?.bindPlatforms),
    remark: record?.remark,
  });
  openModal(true);
};

const handleModalOk = async () => {
  try {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    const submitData = {
      ...values,
      id: unref(currentEditId),
      bindPlatforms: JSON.stringify(values.bindPlatforms || []),
    };
    await taolinkShopApi.edit(submitData);
    createMessage.success('编辑成功');
    closeModal();
    await reload();
  } finally {
    setModalProps({ confirmLoading: false });
  }
};

const getTableActions = (record: any): ActionItem[] => {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
    },
    {
      label: '解绑',
      color: 'error',
      popConfirm: {
        title: '确认解绑该店铺？',
        placement: 'left',
        confirm: handleUnbind.bind(null, record.id),
      },
    },
    {
      label: '详情',
      onClick: handleDetail.bind(null, record.id),
    },
    {
      label: '重新授权',
      ifShow: record?.status === 'active',
      onClick: handleReauthorize.bind(null, record.id),
    },
  ];
};

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

const getApiStatusColor = (expireAt: string) => {
  if (!expireAt) return 'red';
  const expireDate = new Date(expireAt);
  const now = new Date();
  const daysLeft = Math.floor((expireDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
  if (daysLeft < 0) return 'red';
  if (daysLeft < 7) return 'orange';
  return 'green';
};

const getApiStatusText = (expireAt: string) => {
  if (!expireAt) return '未授权';
  const expireDate = new Date(expireAt);
  const now = new Date();
  const daysLeft = Math.floor((expireDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
  if (daysLeft < 0) return '已过期';
  if (daysLeft < 7) return `即将过期(${daysLeft}天)`;
  return '正常';
};
</script>

<style scoped></style>
