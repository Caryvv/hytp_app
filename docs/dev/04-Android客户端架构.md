# 04 Android 客户端架构（用户端 App）

## 1. 技术栈与基线

沿用现有脚手架：AGP 9.3.0 / Kotlin 2.2.10 / Compose BOM 2026.02.01 / compileSdk 36.1 / minSdk 24 / 包名 `com.example.hytp`。

**推荐架构**：单 Activity + Jetpack Compose + Navigation-Compose，MVVM + Repository，UDF（单向数据流）。

## 2. 需新增的依赖（libs.versions.toml）

| 用途 | 库 | 说明 |
|------|-----|------|
| 导航 | `androidx.navigation:navigation-compose` | 页面路由 |
| DI | `com.google.dagger:hilt-android` + hilt-navigation-compose | 依赖注入 |
| 网络 | `com.squareup.retrofit2:retrofit` + `converter-moshi` + `okhttp` | REST |
| 序列化 | `com.squareup.moshi:moshi-kotlin` 或 kotlinx-serialization | JSON |
| 图片 | `io.coil-kt:coil-compose` | 加载网络图/视频封面 |
| 分页 | `androidx.paging:paging-compose` | 列表分页（动态/商品） |
| 本地存储 | `androidx.datastore:datastore-preferences` | token/设置 |
| 数据库 | `androidx.room:room-runtime` + ktx | 本地缓存/草稿 |
| 协程 | `kotlinx-coroutines-android` | 异步 |
| 视频 | `androidx.media3:media3-exoplayer` | 短视频播放 |
| 权限 | `com.google.accompanist:accompanist-permissions` | 相机/定位 |
| 相机 | `androidx.camera:camera-*`（试穿拍照） | CameraX |
| 推送 | 极光/个推 SDK | 通知 |
| 地图 | 高德地图 SDK | 文旅定位 |

> 具体版本联调时用最新稳定版；新增后同步 `app/build.gradle.kts` 的 `dependencies`。

## 3. 目录结构（feature-based 分包）

```
com.example.hytp/
├── HytpApp.kt              # Application(@HiltAndroidApp)
├── MainActivity.kt         # 单 Activity 承载 NavHost
├── core/
│   ├── network/            # Retrofit、OkHttp、拦截器(JWT/日志)、ApiResult
│   ├── data/               # DataStore、Room、通用 Repository 基类
│   ├── model/              # 通用数据模型、分页封装
│   ├── ui/                 # 主题(theme)、通用组件(HanfuButton…)、国风控件
│   └── common/             # 常量、扩展、工具、Result 封装
├── navigation/             # NavGraph、路由常量、BottomBar
└── feature/
    ├── auth/               # 登录注册（05）
    ├── home/               # 首页（05）
    ├── mine/               # 我的、会员（05）
    ├── beginner/           # 萌新入门（06）
    ├── social/             # 同袍社交（07）
    ├── shop/               # 交易区（08）
    ├── travel/             # 文旅（09）
    ├── culture/            # 文化传承（10）
    └── tryon/              # 虚拟试穿/AR（13）
```

每个 feature 内部：`ui/`（Screen + Composable）、`vm/`（ViewModel + UiState）、`data/`（Repository + Api 接口 + dto）。

## 4. 分层与数据流（UDF）

```
Screen(Composable) ──event──▶ ViewModel ──▶ Repository ──▶ ApiService(Retrofit)
     ▲                          │                            │
     └──────UiState(StateFlow)──┘◀──────Result/Flow──────────┘
```

- **ViewModel** 持有 `StateFlow<XxxUiState>`，Screen 用 `collectAsStateWithLifecycle()` 订阅。
- **UiState** 密封类或 data class：`data class HomeUiState(val loading, val data, val error)`。
- **Repository** 返回 `Result<T>` / `Flow<PagingData<T>>`，屏蔽网络细节。
- 副作用（导航、Toast）用 `Channel`/一次性事件（`SharedFlow`）。

## 5. 网络层

- `ApiResult<T>` 统一解析 `{code,message,data}`：`code==0` → Success，否则 → Error(code,message)。用 OkHttp 拦截器或 Retrofit CallAdapter 实现。
- **AuthInterceptor**：自动加 `Authorization` 头；`X-App-Version`/`X-Platform`/`X-Device-Id`。
- **TokenAuthenticator**：401/1002 时用 refreshToken 自动续签并重放请求，续签失败跳登录。
- baseUrl 用 `BuildConfig`（debug 指 test 环境，release 指 prod）。在 `app/build.gradle.kts` 的 `buildTypes` 里配 `buildConfigField`。

## 6. 导航

- 单 `NavHost`，底部 4 Tab：**首页 / 社交 / 商城 / 我的**（对齐策划案）。
- 路由常量集中在 `navigation/Routes.kt`；带参路由用类型安全的 Navigation（`navigation-compose` 的 type-safe API）。
- 未登录可浏览：科普、活动广场、商品浏览；触发交易/社交互动时拦截跳登录（`requireLogin { }` 封装）。

## 7. 主题与国风设计

- 现有 `ui/theme` 扩展：主色调 **黛青 / 月白 / 朱红**（对齐策划案 UI）。在 `Color.kt` 定义命名色值，`Theme.kt` 组织 Material3 `ColorScheme`（明/暗）。
- 字体：标题用书法体（自带字体资源 `res/font`），正文用系统简约体；`Type.kt` 配 `Typography`。
- 通用国风组件放 `core/ui`：`HanfuButton`（盘扣造型）、`HanfuCard`、`DynastyTag`、`SectionTitle` 等。
- 图标/按钮融入盘扣、披帛造型（切图或矢量 drawable）。

## 8. 关键页面清单（对应策划案 UI 模型）

| 页面 | feature | 文档 |
|------|---------|------|
| 启动页（3s 可跳过，缠枝纹+LOGO+Slogan） | app | 05 |
| 登录/注册（手机验证码 + 微信/QQ） | auth | 05 |
| 首页（搜索+轮播+功能导航+个性化推荐+底部Tab） | home | 05 |
| 萌新入门（形制科普/入门路线/智能问答/虚拟试穿） | beginner | 06 |
| 同袍社交（动态/同袍/活动 三子Tab） | social | 07 |
| 汉服商城（分类导航+商家筛选+卡片列表+详情） | shop | 08 |
| 文旅服务（本地服务/文旅套餐/出行攻略） | travel | 09 |
| 文化传承（汉服课堂/历史脉络/非遗专区） | culture | 10 |
| 我的（头像/会员/多类订单/收藏/关注/设置） | mine | 05 |
| 虚拟试穿页（上传照片/虚拟形象+试穿调节） | tryon | 06/13 |

## 9. 通用能力

- **错误与空态**：统一 `LoadingView` / `EmptyView` / `ErrorView`（可重试）组件。
- **图片/视频**：Coil 加载，列表用缩略图（OSS 样式），详情原图；视频用 media3。
- **分页**：Paging3 + `LazyColumn`，下拉刷新用 `PullToRefresh`。
- **离线/草稿**：动态发布草稿、浏览缓存存 Room；token/设置存 DataStore。
- **埋点**：统一埋点封装，关键行为（浏览、试穿、下单、分享）上报，供 13 的数据分析。

## 10. 质量

- 单元测试：ViewModel、Repository（协程 test + fake api）。
- UI 测试：关键流程（登录、下单）用 Compose UI Test（现有 `androidTest` 已含 junit4）。
- 代码规范：ktlint/detekt；模块间只通过 feature 的 public API 交互，避免耦合。
