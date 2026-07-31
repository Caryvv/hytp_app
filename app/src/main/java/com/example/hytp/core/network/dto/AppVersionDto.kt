package com.example.hytp.core.network.dto

import com.squareup.moshi.JsonClass

/** 应用内更新检查结果（对齐后端 AppVersionService::checkUpdate）。 */
@JsonClass(generateAdapter = true)
data class AppVersionCheck(
    val hasUpdate: Boolean = false,
    val latest: AppVersionInfo? = null,
)

@JsonClass(generateAdapter = true)
data class AppVersionInfo(
    val versionCode: Int = 0,
    val versionName: String = "",
    val updateLog: String = "",
    val downloadUrl: String = "",
    val forceUpdate: Boolean = false,
)
