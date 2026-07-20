package com.example.hytp.navigation

/**
 * 路由常量（阶段1 骨架：启动页 → 登录 → 首页占位）。
 * 后续 Tab（社交/商城/我的）在阶段2 扩展。
 */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"

    // 交易区（阶段2）
    const val MALL = "mall"
    const val PRODUCT_DETAIL = "product/{id}"
    const val SHOP = "shop/{id}"

    fun productDetail(id: Long): String = "product/$id"
    fun shop(id: Long): String = "shop/$id"

    // 交易闭环（阶段3）
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val ADDRESS = "address"
    const val ORDER_LIST = "orders"
    const val ORDER_DETAIL = "order/{orderNo}"
    const val REVIEW = "review/{orderNo}/{productId}"

    fun orderDetail(orderNo: String): String = "order/$orderNo"
    fun review(orderNo: String, productId: Long): String = "review/$orderNo/$productId"

    // 社交（阶段4 P0）
    const val FEED_LIST = "feeds"
    const val FEED_PUBLISH = "feed/publish"
    const val FEED_DETAIL = "feed/{id}"
    const val USER_PROFILE = "user/{id}"

    fun feedDetail(id: Long): String = "feed/$id"
    fun userProfile(id: Long): String = "user/$id"
}
