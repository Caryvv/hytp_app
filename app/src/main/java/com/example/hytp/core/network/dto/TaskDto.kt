package com.example.hytp.core.network.dto

// ---------------- 任务系统（赚同袍币） ----------------

/** 任务项（对齐 TaskService::list）。 */
data class TaskItem(
    val key: String = "",
    val name: String = "",
    val reward: Int = 0,
    val daily: Boolean = false,
    val claimable: Boolean = false, // 是否支持主动领取（v1 仅签到）
    val done: Boolean = false,      // 当前周期是否已完成
)

/** 任务列表响应 data。 */
data class TaskListResult(
    val list: List<TaskItem> = emptyList(),
)

/** 领取请求体。 */
data class ClaimRequest(
    val taskKey: String,
)

/** 领取响应 data（对齐 TaskService::claim）。 */
data class ClaimResult(
    val taskKey: String = "",
    val reward: Int = 0,
    val balanceCoin: Int = 0,
)
