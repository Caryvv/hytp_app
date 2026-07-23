package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.BannerItem
import com.example.hytp.core.network.dto.Feed
import com.example.hytp.core.network.dto.PageData
import com.example.hytp.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val api: HytpApiService,
) {
    suspend fun getBanners(): ApiResult<List<BannerItem>> =
        safeApiCall { api.getBanners() }

    suspend fun getHomeFeed(page: Int, pageSize: Int = 10): ApiResult<PageData<Feed>> =
        safeApiCall { api.getHomeFeed(mapOf("page" to page.toString(), "pageSize" to pageSize.toString())) }
}
