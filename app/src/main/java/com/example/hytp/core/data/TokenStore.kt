package com.example.hytp.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicReference

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

/**
 * 登录凭证持久化（DataStore Preferences）。
 *
 * 额外维护 accessToken 的内存缓存 [currentAccessToken]，供 OkHttp 拦截器同步读取，
 * 避免每个请求都阻塞读 DataStore。应用启动时由 [warmUp] 预热。
 */
class TokenStore(private val context: Context) {

    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
    }

    private val accessCache = AtomicReference<String?>(null)

    /** 供拦截器同步读取的 accessToken（内存缓存）。 */
    fun currentAccessToken(): String? = accessCache.get()

    val isLoggedInFlow: Flow<Boolean> =
        context.authDataStore.data.map { !it[Keys.ACCESS].isNullOrBlank() }

    /** 启动时预热内存缓存。 */
    suspend fun warmUp() {
        accessCache.set(context.authDataStore.data.first()[Keys.ACCESS])
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.ACCESS] = accessToken
            prefs[Keys.REFRESH] = refreshToken
        }
        accessCache.set(accessToken)
    }

    suspend fun readRefreshToken(): String? =
        context.authDataStore.data.first()[Keys.REFRESH]

    suspend fun clear() {
        context.authDataStore.edit { it.clear() }
        accessCache.set(null)
    }
}
