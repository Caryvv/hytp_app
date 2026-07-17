package com.example.hytp

import android.app.Application
import com.example.hytp.core.data.TokenStore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 应用入口（Hilt 容器根）。启动时预热 token 缓存供拦截器同步读取。
 */
@HiltAndroidApp
class HytpApp : Application() {

    @Inject
    lateinit var tokenStore: TokenStore

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { tokenStore.warmUp() }
    }
}
