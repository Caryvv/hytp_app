package com.example.hytp.core.network.dto

/**
 * 钱包流水（对齐后端 WalletTransaction::toArray）。
 * amount 以同袍币整数记账，+入账 -出账；balanceAfter 为该笔后的余额快照。
 */
data class WalletTxn(
    val id: Long,
    val txnNo: String = "",
    val type: Int = 0,
    val amount: Int = 0,
    val balanceAfter: Int = 0,
    val channel: Int = 0,
    val refType: String = "",
    val refId: String = "",
    val remark: String = "",
    val status: Int = 1,
    val createdAt: Long = 0,
)

/** 流水类型文案（对齐后端 WalletTransaction TYPE_* 常量）。 */
fun walletTxnTypeText(type: Int): String = when (type) {
    1 -> "充值"
    2 -> "任务奖励"
    3 -> "消费"
    4 -> "退款"
    5 -> "系统赠送"
    6 -> "提现"
    else -> "其他"
}
