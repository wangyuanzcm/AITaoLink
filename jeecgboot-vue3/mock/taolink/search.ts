import { MockMethod } from 'vite-plugin-mock';
import { resultSuccess } from '../_util';

// 淘宝商品数据
const taobaoItems = [
  {
    num_iid: '678901',
    title: '女装2023夏季新款短袖T恤女宽松韩版百搭上衣',
    pic_url: 'https://img.alicdn.com/imgextra/i4/220678901/O1CN01abcdefghijklmnop_!!220678901.jpg',
    price: '89.90',
    seller_nick: '时尚女装店',
    location: '杭州'
  },
  {
    num_iid: '678902',
    title: '夏季新款连衣裙女收腰显瘦气质女神范裙子',
    pic_url: 'https://img.alicdn.com/imgextra/i3/220678902/O1CN01pqrstuvwxyz1234_!!220678902.jpg',
    price: '159.90',
    seller_nick: '优雅女装',
    location: '广州'
  },
  {
    num_iid: '678903',
    title: '韩版宽松牛仔裤女2023夏季新款高腰直筒裤',
    pic_url: 'https://img.alicdn.com/imgextra/i2/220678903/O1CN01abcdefghijklmnop_!!220678903.jpg',
    price: '129.90',
    seller_nick: '牛仔风尚',
    location: '佛山'
  },
  {
    num_iid: '678904',
    title: '夏季防晒衣女薄款外套防紫外线透气防晒衫',
    pic_url: 'https://img.alicdn.com/imgextra/i1/220678904/O1CN01pqrstuvwxyz1234_!!220678904.jpg',
    price: '69.90',
    seller_nick: '阳光防晒',
    location: '义乌'
  },
  {
    num_iid: '678905',
    title: '运动鞋女2023夏季新款轻便透气跑步鞋',
    pic_url: 'https://img.alicdn.com/imgextra/i4/220678905/O1CN01abcdefghijklmnop_!!220678905.jpg',
    price: '199.90',
    seller_nick: '运动潮流',
    location: '泉州'
  }
];

// 1688商品数据
const alibabaItems = [
  {
    num_iid: '669093370636',
    title: '男装2022夏季短袖情侣t恤男女同款时尚简约休闲圆领T恤上衣服批发',
    pic_url: 'https://cbu01.alicdn.com/img/ibank/O1CN01vH2Jpd1MNmKzbiavS_!!2213203691423-0-cib.jpg',
    price: '29.90',
    seller_nick: '石狮市领川服装厂',
    location: '泉州市'
  },
  {
    num_iid: '668773906932',
    title: '2022夏季新款印花短袖t恤女网红同款衣服女韩版百搭中长款上衣女',
    pic_url: 'https://cbu01.alicdn.com/img/ibank/O1CN01Ek7mv41S5oV5YcCvp_!!2744312196-0-cib.jpg',
    price: '35.90',
    seller_nick: '普宁市洪阳戴紫妮服装厂',
    location: '普宁市'
  },
  {
    num_iid: '670585170610',
    title: '抖音质量磨毛双面德绒短袖t恤女立体卡通夏2022新款上衣大码女装',
    pic_url: 'https://cbu01.alicdn.com/img/ibank/O1CN01ihTAw31Vqw6C0UByX_!!2213014272705-0-cib.jpg',
    price: '42.90',
    seller_nick: '广州克里贸易有限公司',
    location: '广州市'
  },
  {
    num_iid: '668912014686',
    title: '韩路女装 2022春夏新款卡通印花短袖T恤女ins风纯棉宽松潮流体恤',
    pic_url: 'https://cbu01.alicdn.com/img/ibank/O1CN01vPvBu722OtAF9rxw2_!!3910967111-0-cib.jpg',
    price: '39.90',
    seller_nick: '瑞安市萌越贸易有限公司',
    location: '瑞安市'
  },
  {
    num_iid: '671716125800',
    title: '撞色条纹短袖针织衫女2022夏季新款休闲百搭别致上衣ins显瘦薄款',
    pic_url: 'https://cbu01.alicdn.com/img/ibank/O1CN01ztb9V421Yg1BzxUC7_!!2213249306997-0-cib.jpg',
    price: '59.90',
    seller_nick: '广州小仙妮子服装厂',
    location: '广州市'
  }
];

// 淘宝商品详情数据
const taobaoItemDetail = {
  num_iid: '520813250866',
  title: '三刃木折叠刀过安检创意迷你钥匙扣钥匙刀军刀随身多功能小刀包邮',
  desc_short: '',
  price: '25.80',
  total_price: 0,
  suggestive_price: 0,
  orginal_price: '25.80',
  nick: '欢乐购客栈',
  num: '832',
  min_num: 0,
  detail_url: 'http://item.taobao.com/item.htm?id=520813250866',
  pic_url: '//img.alicdn.com/imgextra/i4/2596264565/TB2p30elFXXXXXQXpXXXXXXXXXX_!!2596264565.jpg',
  brand: '三刃木',
  brandId: '4036703',
  rootCatId: '50013886',
  cid: '50014822',
  favcount: '4824',
  fanscount: '1469',
  crumbs: [],
  created_time: '',
  modified_time: '',
  delist_time: '',
  desc: '这是一款高品质的折叠刀，适合日常使用，方便携带。',
  item_imgs: [
    {
      url: '//img.alicdn.com/imgextra/i4/2596264565/TB2p30elFXXXXXQXpXXXXXXXXXX_!!2596264565.jpg'
    },
    {
      url: '//img.alicdn.com/imgextra/i2/2596264565/TB2onxRlVXXXXcDXpXXXXXXXXXX_!!2596264565.jpg'
    }
  ],
  item_weight: '0',
  item_size: '',
  location: '广东深圳',
  post_fee: '',
  express_fee: '0.00',
  ems_fee: '',
  shipping_to: '广东广州白云区',
  has_discount: 'false',
  video: [],
  is_virtual: '',
  sample_id: '',
  is_promotion: 'false',
  props_name: '1627207:1347647754:颜色分类:长方形带开瓶器+送工具刀卡+链子',
  prop_imgs: {
    prop_img: [
      {
        properties: '1627207:1347647754',
        url: '//img.alicdn.com/imgextra/i3/2596264565/TB2.XeblVXXXXXkXpXXXXXXXXXX_!!2596264565.jpg'
      }
    ]
  },
  property_alias: '1627207:1347647754:长方形带开瓶器+送工具刀卡+链子',
  props: [
    {
      name: '品牌',
      value: '三刃木'
    },
    {
      name: '产地',
      value: '中国'
    }
  ],
  total_sold: '1',
  pics: [
    '//img.alicdn.com/imgextra/i4/2596264565/TB2p30elFXXXXXQXpXXXXXXXXXX_!!2596264565.jpg',
    '//img.alicdn.com/imgextra/i2/2596264565/TB2onxRlVXXXXcDXpXXXXXXXXXX_!!2596264565.jpg'
  ]
};

// 1688商品详情数据
const alibabaItemDetail = {
  num_iid: '601824709263',
  title: '韩版仿兔毛围巾秋冬毛毛围巾学生毛毛绒围脖冬季女套头防寒户外',
  desc_short: '',
  price: '2.20',
  total_price: 0,
  suggestive_price: 0,
  orginal_price: '2.20',
  nick: '_sopid@BBBEbRNm7OS0uGCXPJucMBy2w',
  num: 3786174,
  min_num: 2,
  detail_url: 'https://detail.1688.com/offer/601824709263.html',
  pic_url: 'https://cbu01.alicdn.com/img/ibank/11921650503_1818272533.jpg',
  brand: '',
  brandId: '',
  rootCatId: 0,
  cid: 325,
  crumbs: [],
  created_time: '',
  modified_time: '',
  delist_time: '',
  desc: '<div id="offer-template-0"></div><div > </div>\r\n<div ><span ><strong> <span >材质：双面仿兔毛毛绒面料，</span></strong></span></div>\r\n<div ><span ><strong><span >不掉毛（不是薄薄的单面哦）</span></strong></span></div>\r\n<div ><span ><strong ><span >厚款3.39元</span></strong></span></div>\r\n<div ><span ><strong><span ><span >普通款2.38元</span></span></strong></span></div>\r\n<div ><span ><strong><span ><span >每条围巾都是独立OPP袋子包装，放心选购！</span></span></strong></span></div>',
  desc_img: [
    'https://cbu01.alicdn.com/img/ibank/O1CN01emutXa27pyd6vYZzm_!!2206475177847-0-cib.jpg',
    'https://img.alicdn.com/imgextra/i3/2272041426/O1CN01kFnNOl1MP9H5642lo_!!2272041426.jpg'
  ],
  item_imgs: [
    {
      url: 'https://cbu01.alicdn.com/img/ibank/11921650503_1818272533.jpg'
    },
    {
      url: 'https://cbu01.alicdn.com/img/ibank/O1CN01emutXa27pyd6vYZzm_!!2206475177847-0-cib.jpg'
    }
  ],
  item_weight: '',
  item_size: '',
  location: '浙江义乌',
  post_fee: '',
  express_fee: '',
  ems_fee: '',
  shipping_to: '',
  has_discount: false,
  video: [],
  is_virtual: '',
  sample_id: '',
  is_promotion: false,
  props_name: '',
  prop_imgs: {
    prop_img: []
  },
  property_alias: '',
  props: [],
  total_sold: '',
  pics: [
    'https://cbu01.alicdn.com/img/ibank/11921650503_1818272533.jpg',
    'https://cbu01.alicdn.com/img/ibank/O1CN01emutXa27pyd6vYZzm_!!2206475177847-0-cib.jpg'
  ]
};

export default [
  {
    url: '/jeecgboot/taolink/search/unified',
    method: 'get',
    response: ({ query }) => {
      const { platform, keyword, pageNo = 1, pageSize = 10 } = query;
      
      // 根据平台选择数据
      const items = platform === 'taobao' ? taobaoItems : alibabaItems;
      
      // 模拟分页
      const start = (Number(pageNo) - 1) * Number(pageSize);
      const end = start + Number(pageSize);
      const paginatedItems = items.slice(start, end);
      
      return resultSuccess({
        items: paginatedItems,
        total: items.length,
        cacheHit: Math.random() > 0.5 // 随机模拟缓存命中
      });
    }
  },
  {
    url: '/jeecgboot/taolink/search/item-detail',
    method: 'get',
    response: ({ query }) => {
      const { platform, num_iid } = query;
      
      // 根据平台选择数据
      const itemDetail = platform === 'taobao' ? taobaoItemDetail : alibabaItemDetail;
      
      // 模拟缓存命中
      const cacheHit = Math.random() > 0.5;
      
      return resultSuccess({
        ...itemDetail,
        num_iid: num_iid, // 使用传入的商品ID
        cacheHit: cacheHit
      });
    }
  },
  {
    url: '/jeecgboot/taolink/search/import-to-source-offer',
    method: 'post',
    response: ({ body }) => {
      const { items } = body;
      return resultSuccess({
        imported: items.length,
        success: true
      });
    }
  },
  {
    url: '/jeecgboot/taolink/search/stats',
    method: 'get',
    response: () => {
      return resultSuccess({
        totalRequests: 1250,
        cacheHits: 875,
        cacheHitRate: '70%',
        totalCacheSize: '15.2 MB'
      });
    }
  },
  {
    url: '/jeecgboot/taolink/search/clear-expired',
    method: 'delete',
    response: () => {
      return resultSuccess({
        cleared: 156,
        success: true
      });
    }
  }
] as MockMethod[];
