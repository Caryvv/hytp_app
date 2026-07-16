# 03 后端 API 规范（Yii2）

## 1. Yii2 项目结构（advanced 模板改造）

```
hytp-backend/
├── common/
│   ├── models/            # ActiveRecord
│   ├── services/          # 业务逻辑层
│   ├── enums/             # 枚举常量
│   ├── dto/               # 请求/响应结构
│   ├── behaviors/         # JwtAuth、Timestamp 等
│   └── config/            # 共享配置（db、params）
├── api/                   # 用户端入口
│   ├── controllers/
│   ├── modules/{shop,social,travel,culture,learn}/
│   └── config/
├── merchant/              # 商家端入口
├── admin/                 # 管理端入口
├── console/               # 迁移、定时任务
└── environments/
```

## 2. RESTful 约定

- Base URL：`https://api.hytp.com/v1`
- 资源用名词复数：`/products`、`/feeds`、`/orders`。
- 方法语义：`GET` 查询、`POST` 创建、`PUT/PATCH` 更新、`DELETE` 删除。
- 控制器继承自定义 `ApiController`（统一鉴权、响应格式、异常处理）。
- 用 Yii2 `yii\rest\Controller` + 自定义 `serializeData`，或纯 action 手写响应（推荐后者，控制力强）。

## 3. 统一响应格式

所有接口返回 JSON，HTTP 状态码始终 200（业务错误走 body `code`），除非网关/鉴权层错误：

```json
{
  "code": 0,
  "message": "success",
  "data": { }
}
```

- `code = 0` 表示成功，非 0 为业务错误码。
- 列表分页 `data` 结构：

```json
{ "list": [], "pagination": { "page": 1, "pageSize": 20, "total": 135 } }
```

统一实现建议：在 `ApiController::afterAction` 或响应 formatter 里包装，业务代码只 `return $data` 或抛 `BizException($code, $msg)`。

## 4. 错误码规范

分段管理，便于定位：

| 段         | 含义                                   |
| ---------- | -------------------------------------- |
| 0          | 成功                                   |
| 1000–1099 | 通用（参数错误、未登录、无权限、限流） |
| 1100–1199 | 账号/会员                              |
| 1200–1299 | 商品/交易                              |
| 1300–1399 | 支付                                   |
| 1400–1499 | 社交                                   |
| 1500–1599 | 文旅                                   |
| 1600–1699 | 文化/内容                              |
| 1700–1799 | 商家端                                 |
| 1800–1899 | AI 服务                                |
| 5000+      | 服务端内部错误                         |

常用：

| code | message             |
| ---- | ------------------- |
| 1001 | 参数校验失败        |
| 1002 | 未登录 / token 失效 |
| 1003 | 无权限              |
| 1004 | 请求过于频繁        |
| 1005 | 资源不存在          |
| 1201 | 库存不足            |
| 1202 | 商品已下架          |
| 1301 | 支付失败            |
| 1801 | AI 服务超时/不可用  |

> 客户端只依据 `code` 分支处理，`message` 仅用于兜底提示（可被服务端文案覆盖）。

## 5. 鉴权（JWT）

- 登录成功返回 `accessToken`（2h）+ `refreshToken`（30d，存 Redis 白名单）。
- 请求头：`Authorization: Bearer <accessToken>`。
- Payload：`{ sub: userId, aud: "app"|"merchant"|"admin", exp, jti }`。三端 `aud` 不同、密钥可不同，互不通用。
- 用 `firebase/php-jwt`，封装 `JwtAuthBehavior`（Yii `behaviors()` 中挂载），校验失败抛 1002。
- 刷新：`POST /auth/refresh`，用 refreshToken 换新 accessToken；退出登录时从 Redis 移除 refreshToken（jti 拉黑）。
- 白名单接口（无需登录）：科普浏览、活动广场、商品浏览、登录/注册/发短信。在 behavior 里配 `optional` 或 `except`。

## 6. 公共请求约定

- 分页：`?page=1&pageSize=20`（pageSize 上限 50）。
- 排序：`?sort=-created_at`（`-` 降序）。
- 筛选：具体字段作 query 参数，复杂筛选用 `POST /xxx/search` + body。
- 时间：统一秒级时间戳（整数）。
- 幂等：下单、支付回调等写接口，用 `Idempotency-Key` 头或业务单号去重。
- 客户端标识：`X-App-Version`、`X-Platform: android`、`X-Device-Id`（埋点与风控）。

## 7. 文件上传（OSS 直传）

1. 客户端请求 `POST /upload/sts` → 后端签发 OSS STS 临时凭证 + 上传路径前缀。
2. 客户端直传 OSS，拿到最终 URL/key。
3. 业务接口只提交 URL/key。

- 服务端对 key 前缀、文件类型、大小做二次校验；图片走 OSS 图片处理样式生成缩略图。

## 8. 短信验证码

- `POST /sms/send`：`{ phone, scene(login/register/reset) }`，限流：同号 60s 一次、同 IP 每日上限；验证码存 Redis（5min）。
- 校验在登录/注册接口内完成，失败计数超阈值锁定。

## 9. 限流与安全

- 网关层（Nginx）+ 应用层（Redis 计数）双重限流：默认单用户 60 req/min，敏感接口更严。
- 所有写接口做参数白名单校验（Yii Model rules / DTO）；富文本内容过滤 XSS。
- SQL 一律用 AR/QueryBuilder 参数绑定，禁止拼接。
- 越权校验：涉及 `user_id/shop_id` 归属的操作，Service 层校验资源属主。

## 10. 接口文档与联调

- 用 OpenAPI 3（可用 `zircote/swagger-php` 注解生成）产出 `swagger.json`，供 Android/Web 联调。
- 各模块文档（05–13）给出该模块的端点清单，本规范为总约定。
- Mock：联调期用 Apifox/YApi 托管 Mock，字段与本规范一致。
