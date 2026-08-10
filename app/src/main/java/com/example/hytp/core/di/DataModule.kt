package com.example.hytp.core.di

import android.content.Context
import com.example.hytp.core.data.AddressRepository
import com.example.hytp.core.data.AiRepository
import com.example.hytp.core.data.AuthRepository
import com.example.hytp.core.data.CartRepository
import com.example.hytp.core.data.ChatRepository
import com.example.hytp.core.data.ContentRepository
import com.example.hytp.core.data.GroupRepository
import com.example.hytp.core.data.TryonRepository
import com.example.hytp.core.data.HomeRepository
import com.example.hytp.core.data.OrderRepository
import com.example.hytp.core.data.PaymentRepository
import com.example.hytp.core.data.ShopRepository
import com.example.hytp.core.data.SocialRepository
import com.example.hytp.core.data.TaskRepository
import com.example.hytp.core.data.TokenStore
import com.example.hytp.core.data.UploadRepository
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
    fun provideTaskRepository(
        api: HytpApiService,
    ): TaskRepository = TaskRepository(api)

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

    @Provides
    @Singleton
    fun provideUploadRepository(
        api: HytpApiService,
        @ApplicationContext context: Context,
    ): UploadRepository = UploadRepository(api, context)

    @Provides
    @Singleton
    fun provideHomeRepository(
        api: HytpApiService,
    ): HomeRepository = HomeRepository(api)

    @Provides
    @Singleton
    fun provideAiRepository(
        api: HytpApiService,
    ): AiRepository = AiRepository(api)

    @Provides
    @Singleton
    fun provideContentRepository(
        api: HytpApiService,
    ): ContentRepository = ContentRepository(api)

    @Provides
    @Singleton
    fun provideTryonRepository(
        api: HytpApiService,
    ): TryonRepository = TryonRepository(api)
}
