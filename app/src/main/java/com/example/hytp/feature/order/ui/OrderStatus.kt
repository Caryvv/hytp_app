package com.example.hytp.feature.order.ui

/** 订单状态文案（对齐后端 ShopOrder 状态常量）。 */
fun orderStatusText(status: Int): String = when (status) {
    0 -> "待付款"
    1 -> "待发货"
    2 -> "待收货"
    4 -> "已完成"
    5 -> "已取消"
    6 -> "售后中"
    else -> "未知"
}
