package com.example.hytp.core.data

import java.util.concurrent.atomic.AtomicReference

/**
 * 当前登录用户会话缓存（@Singleton）。
 * 社交场景频繁判断"是不是我自己/我有没有关注 TA"，避免每次调 getProfile。
 * 登录成功时 AuthRepository 写入 userId；退出清空。内存缓存，供 ViewModel 同步读。
 */
class UserSessionManager {

    private val userIdRef = AtomicReference<Long?>(null)

    /** 当前登录用户 id，未登录/未初始化返回 null。 */
    fun currentUserId(): Long? = userIdRef.get()

    fun setUserId(id: Long?) {
        userIdRef.set(id)
    }

    fun clear() {
        userIdRef.set(null)
    }
}
