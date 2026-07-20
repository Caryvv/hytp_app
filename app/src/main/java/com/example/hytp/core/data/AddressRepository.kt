package com.example.hytp.core.data

import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.Address
import com.example.hytp.core.network.dto.AddressList
import com.example.hytp.core.network.dto.AddressRequest
import com.example.hytp.core.network.safeApiCall

/**
 * 收货地址仓库（需登录）。
 */
class AddressRepository(
    private val api: HytpApiService,
) {
    suspend fun list(): ApiResult<AddressList> =
        safeApiCall { api.getAddresses() }

    suspend fun create(body: AddressRequest): ApiResult<Address> =
        safeApiCall { api.createAddress(body) }

    suspend fun update(id: Long, body: AddressRequest): ApiResult<Address> =
        safeApiCall { api.updateAddress(id, body) }

    suspend fun remove(id: Long): ApiResult<Unit> =
        safeApiCall { api.deleteAddress(id) }

    suspend fun setDefault(id: Long): ApiResult<Address> =
        safeApiCall { api.setDefaultAddress(id) }
}
