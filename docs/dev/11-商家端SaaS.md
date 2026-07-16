# 11 商家端 · SaaS 后台

> 对应策划案「三、（二）商家端（SaaS 后台）」，数据赋能，解决商家痛点。Web 端（Vue3 + Element Plus），复用后端 `merchant/` 入口。

## 1. 模块概述

面向汉服商家（品牌店、中小商户、手作匠人、妆造师、摄影师、文旅/非遗商家），提供店铺管理、数据驾驶舱、营销工具、供应链对接，降低数字化门槛。

## 2. 功能清单

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 商家入驻/资质 | P0 | 注册、资质上传、审核状态 |
| 店铺管理 | P0 | 商品 CRUD、价格、库存、上下架 |
| 订单管理 | P0 | 订单处理、发货、售后 |
| 租赁/档期 | P1 | 可租状态、摄影师档期日历、核销 |
| 数据驾驶舱 | P1 | 用户画像、销量预测、热门款式、评论关键词 |
| 营销工具 | P1 | 优惠券、限时折扣、拼团、直播带货、活动报名 |
| 供应链对接 | P2 | 面料/生产厂家对接、设计灵感、产业带专区 |

## 3. 页面与交互

### 3.1 登录/入驻
- 商家账号登录；入驻申请（填资料+上传资质）→ 等待管理端审核。

### 3.2 工作台（首页）
- 核心指标概览：今日订单/销售额/待处理售后/店铺评分；待办提醒。

### 3.3 商品管理
- 列表（筛选/搜索）→ 新增/编辑（标题、分类、形制、SKU、价格、库存、图文详情、试穿素材）→ 上下架。
- 租赁商家：设置"可租状态"；摄影师/妆造：设置档期日历。

### 3.4 订单管理
- 订单列表（状态/类型筛选）→ 详情 → 发货/处理售后/核销文旅预约。

### 3.5 数据驾驶舱
- 用户画像（年龄/性别/地域/偏好/消费能力）、销量预测、热门款式分析、评论情感关键词（指导品控，如"线头"改进）。

### 3.6 营销工具
- 创建优惠券/限时折扣/拼团/直播；报名平台活动（赛事/新品首发）获流量；中小商家低门槛推广套餐。

## 4. 数据模型（相关子集）

`shop`、`shop_qualification`、`shop_credit_log`、`product`、`product_sku`、`shop_order`、`order_item`、`order_refund`、`product_review`、`coupon`、`travel_merchant`、`travel_package`、`travel_reservation`、`data_stat_daily`。

## 5. API 接口（merchant 入口，路径前缀 `/merchant`）

### 账号与店铺
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/merchant/auth/login` | 商家登录 |
| POST | `/merchant/register` | 入驻申请 |
| POST | `/merchant/qualifications` | 上传资质 |
| GET/PUT | `/merchant/shop` | 店铺信息 |

### 商品
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/merchant/products` | 商品列表 |
| POST/PUT | `/merchant/products` | 新增/编辑 |
| POST | `/merchant/products/{id}/toggle` | 上下架 |
| PUT | `/merchant/products/{id}/stock` | 库存/可租状态 |
| GET/PUT | `/merchant/schedule` | 档期日历（摄影/妆造） |

### 订单
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/merchant/orders` | 订单列表 |
| POST | `/merchant/orders/{orderNo}/ship` | 发货 |
| POST | `/merchant/orders/{orderNo}/refund/handle` | 处理售后 |
| POST | `/merchant/reservations/{id}/verify` | 核销文旅预约 |

### 数据驾驶舱
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/merchant/dashboard/overview` | 概览指标 |
| GET | `/merchant/dashboard/portrait` | 用户画像 |
| GET | `/merchant/dashboard/sales-forecast` | 销量预测（13） |
| GET | `/merchant/dashboard/hot-styles` | 热门款式 |
| GET | `/merchant/dashboard/review-keywords` | 评论情感关键词（13） |

### 营销
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/merchant/coupons` | 创建优惠券 |
| POST | `/merchant/promotions` | 限时折扣/拼团 |
| POST | `/merchant/lives` | 直播带货 |
| POST | `/merchant/activities/{id}/join` | 报名平台活动 |

## 6. 权限与安全

- 商家账号 `aud=merchant`，与用户 token 隔离。
- 所有商品/订单操作 Service 层校验 `shop_id` 归属，防越权操作他店数据。
- 数据驾驶舱只返回本店相关的脱敏聚合数据，不暴露平台其他商家/用户隐私明细。

## 7. AI/数据接入

- 销量预测、热门款式、评论关键词均调 13 的数据分析服务，基于平台用户行为与本店评论。
- 供应链设计灵感/地域文化融合方案为 P2，可先占位。

## 8. 验收标准

- 商家可入驻、上传资质、待审核；审核通过后可上架商品。
- 商品/订单/库存/档期管理可用，发货与售后处理正确改单。
- 数据驾驶舱展示本店画像/预测/热门/评论关键词，数据隔离。
- 可创建营销活动并对用户端生效（优惠券可被领取使用）。
