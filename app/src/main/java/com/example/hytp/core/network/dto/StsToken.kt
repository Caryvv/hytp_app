package com.example.hytp.core.network.dto

/**
 * OSS 直传临时凭证（对齐 AliyunStsService::assumeRole）。
 * enabled=false 时其余字段为空，客户端回退服务器中转上传。
 */
data class StsToken(
    val enabled: Boolean = false,
    val accessKeyId: String = "",
    val accessKeySecret: String = "",
    val securityToken: String = "",
    val expiration: String = "",
    val region: String = "",
    val bucket: String = "",
    val endpoint: String = "",
    val dir: String = "",
)
