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
}
