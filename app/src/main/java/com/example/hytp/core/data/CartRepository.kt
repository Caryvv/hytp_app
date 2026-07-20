package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.AddCartRequest
import com.example.hytp.core.network.dto.CartItem
import com.example.hytp.core.network.dto.CartList
import com.example.hytp.core.network.dto.UpdateCartRequest
import com.example.hytp.core.network.safeApiCall

/**
 * 购物车仓库（需登录，token 由 AuthInterceptor 自动附加）。
 */
class CartRepository(
    private val api: HytpApiService,
) {
    suspend fun getCart(): ApiResult<CartList> =
        safeApiCall { api.getCart() }

    suspend fun add(productId: Long, skuId: Long? = null, qty: Int = 1): ApiResult<CartItem> =
        safeApiCall { api.addToCart(AddCartRequest(productId, skuId, qty)) }

    suspend fun updateQty(id: Long, qty: Int): ApiResult<CartItem> =
        safeApiCall { api.updateCartQty(id, UpdateCartRequest(qty)) }

    suspend fun remove(id: Long): ApiResult<Unit> =
        safeApiCall { api.deleteCartItem(id) }

    suspend fun clear(): ApiResult<Unit> =
        safeApiCall { api.clearCart() }
}
