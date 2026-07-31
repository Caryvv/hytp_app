package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.AppVersionCheck
import com.example.hytp.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppVersionRepository @Inject constructor(
    private val api: HytpApiService,
) {
    /** 检查更新。currentCode 传当前 BuildConfig.VERSION_CODE。 */
    suspend fun check(currentCode: Int, platform: String = "android"): ApiResult<AppVersionCheck> =
        safeApiCall { api.checkAppVersion(platform, currentCode) }
}
