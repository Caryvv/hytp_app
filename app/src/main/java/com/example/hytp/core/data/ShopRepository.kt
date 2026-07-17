package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.Category
import com.example.hytp.core.network.dto.PageData
import com.example.hytp.core.network.dto.ProductDetail
import com.example.hytp.core.network.dto.ProductListItem
import com.example.hytp.core.network.dto.Review
import com.example.hytp.core.network.dto.ShopPublic
import com.example.hytp.core.network.safeApiCall

/**
 * 交易区只读数据仓库：分类、商品浏览、店铺主页。
 * 均为白名单接口，无需登录。
 */
class ShopRepository(
    private val api: HytpApiService,
) {

    /** 分类树。 */
    suspend fun getCategories(): ApiResult<List<Category>> =
        safeApiCall { api.getCategories() }

    /**
     * 商品列表（筛选 + 分页）。空/默认值不拼进 query，交给后端默认处理。
     */
    suspend fun getProducts(
        categoryId: Int? = null,
        formeDynasty: Int? = null,
        formeType: String? = null,
        style: String? = null,
        tradeType: Int? = null,
        keyword: String? = null,
        sort: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): ApiResult<PageData<ProductListItem>> {
        val query = buildMap {
            categoryId?.let { if (it > 0) put("categoryId", it.toString()) }
            formeDynasty?.let { put("formeDynasty", it.toString()) }
            formeType?.takeIf { it.isNotBlank() }?.let { put("formeType", it) }
            style?.takeIf { it.isNotBlank() }?.let { put("style", it) }
            tradeType?.let { if (it > 0) put("tradeType", it.toString()) }
            keyword?.takeIf { it.isNotBlank() }?.let { put("keyword", it) }
            sort?.takeIf { it.isNotBlank() }?.let { put("sort", it) }
            put("page", page.toString())
            put("pageSize", pageSize.toString())
        }
        return safeApiCall { api.getProducts(query) }
    }

    /** 商品详情。 */
    suspend fun getProductDetail(id: Long): ApiResult<ProductDetail> =
        safeApiCall { api.getProductDetail(id) }

    /** 商品评价列表。 */
    suspend fun getProductReviews(id: Long, page: Int = 1, pageSize: Int = 20): ApiResult<PageData<Review>> =
        safeApiCall { api.getProductReviews(id, page, pageSize) }

    /** 店铺主页。 */
    suspend fun getShop(id: Long): ApiResult<ShopPublic> =
        safeApiCall { api.getShop(id) }

    /** 店铺在售商品。 */
    suspend fun getShopProducts(id: Long, page: Int = 1, pageSize: Int = 20): ApiResult<PageData<ProductListItem>> =
        safeApiCall { api.getShopProducts(id, page, pageSize) }
}
