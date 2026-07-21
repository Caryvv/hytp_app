package com.example.hytp.navigation

/**
 * 路由常量。
 * 顶层：SPLASH → LOGIN → MAIN（底部 4 Tab）。
 * Tab 内部子页面路由在各自的嵌套 NavHost 中注册。
 */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MAIN = "main"

    // ── 交易区路由（商城 & 我的 Tab 共用） ──
    const val MALL = "mall"
    const val PRODUCT_DETAIL = "product/{id}"
    const val SHOP = "shop/{id}"

    fun productDetail(id: Long): String = "product/$id"
    fun shop(id: Long): String = "shop/$id"

    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val ADDRESS = "address"
    const val ORDER_LIST = "orders"
    const val ORDER_DETAIL = "order/{orderNo}"
    const val REVIEW = "review/{orderNo}/{productId}"

    fun orderDetail(orderNo: String): String = "order/$orderNo"
    fun review(orderNo: String, productId: Long): String = "review/$orderNo/$productId"

    // ── 社交路由（社交 Tab） ──
    const val FEED_LIST = "feeds"
    const val FEED_PUBLISH = "feed/publish"
    const val FEED_DETAIL = "feed/{id}"
    const val USER_PROFILE = "user/{id}"

    fun feedDetail(id: Long): String = "feed/$id"
    fun userProfile(id: Long): String = "user/$id"

    const val CONVERSATION_LIST = "conversations"
    const val CHAT = "chat/{id}"
    const val GROUP_LIST = "groups"
    const val GROUP_CHAT = "group/{id}"

    fun chat(id: Long): String = "chat/$id"
    fun groupChat(id: Long): String = "group/$id"
}

/**
 * Tab 根路由（每 Tab 嵌套 NavHost 的起始目的地）。
 * 使用独立路由名避免与子页面路由冲突。
 */
object TabRoutes {
    const val HOME_ROOT = "tab_home"
    const val SOCIAL_ROOT = "tab_social"
    const val MALL_ROOT = "tab_mall"
    const val MINE_ROOT = "tab_mine"

    fun getRoot(tab: com.example.hytp.core.ui.BottomTab): String = when (tab) {
        com.example.hytp.core.ui.BottomTab.Home -> HOME_ROOT
        com.example.hytp.core.ui.BottomTab.Social -> SOCIAL_ROOT
        com.example.hytp.core.ui.BottomTab.Mall -> MALL_ROOT
        com.example.hytp.core.ui.BottomTab.Mine -> MINE_ROOT
    }
}
