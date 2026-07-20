package com.example.hytp.core.di

import android.content.Context
import com.example.hytp.core.data.AddressRepository
import com.example.hytp.core.data.AuthRepository
import com.example.hytp.core.data.CartRepository
import com.example.hytp.core.data.ChatRepository
import com.example.hytp.core.data.GroupRepository
import com.example.hytp.core.data.OrderRepository
import com.example.hytp.core.data.PaymentRepository
import com.example.hytp.core.data.ShopRepository
import com.example.hytp.core.data.SocialRepository
import com.example.hytp.core.data.TokenStore
import com.example.hytp.core.data.UserSessionManager
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
    fun provideUserSessionManager(): UserSessionManager = UserSessionManager()

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: HytpApiService,
        tokenStore: TokenStore,
        sessionManager: UserSessionManager,
    ): AuthRepository = AuthRepository(api, tokenStore, sessionManager)

    @Provides
    @Singleton
    fun provideShopRepository(
        api: HytpApiService,
    ): ShopRepository = ShopRepository(api)

    @Provides
    @Singleton
    fun provideCartRepository(
        api: HytpApiService,
    ): CartRepository = CartRepository(api)

    @Provides
    @Singleton
    fun provideAddressRepository(
        api: HytpApiService,
    ): AddressRepository = AddressRepository(api)

    @Provides
    @Singleton
    fun provideOrderRepository(
        api: HytpApiService,
    ): OrderRepository = OrderRepository(api)

    @Provides
    @Singleton
    fun providePaymentRepository(
        api: HytpApiService,
    ): PaymentRepository = PaymentRepository(api)

    @Provides
    @Singleton
    fun provideSocialRepository(
        api: HytpApiService,
    ): SocialRepository = SocialRepository(api)

    @Provides
    @Singleton
    fun provideChatRepository(
        api: HytpApiService,
    ): ChatRepository = ChatRepository(api)

    @Provides
    @Singleton
    fun provideGroupRepository(
        api: HytpApiService,
    ): GroupRepository = GroupRepository(api)
}
