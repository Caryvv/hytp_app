package com.example.hytp.core.di

import android.content.Context
import com.example.hytp.core.data.AuthRepository
import com.example.hytp.core.data.ShopRepository
import com.example.hytp.core.data.TokenStore
import com.example.hytp.core.network.HytpApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 提供本地存储与仓库依赖。
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideTokenStore(@ApplicationContext context: Context): TokenStore =
        TokenStore(context)

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: HytpApiService,
        tokenStore: TokenStore,
    ): AuthRepository = AuthRepository(api, tokenStore)

    @Provides
    @Singleton
    fun provideShopRepository(
        api: HytpApiService,
    ): ShopRepository = ShopRepository(api)
}
