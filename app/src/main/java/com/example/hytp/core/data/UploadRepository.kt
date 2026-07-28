package com.example.hytp.core.data

import android.content.Context
import android.net.Uri
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.StsToken
import com.example.hytp.core.network.dto.UploadResult
import com.example.hytp.core.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 文件上传仓库。先复制到缓存目录再上传（避免直接读 content URI 问题）。
 *
 * 上传策略（对调用方透明，uploadImage 签名不变）：
 *   1) 先向后端取 OSS 直传临时凭证（STS）。启用 → 客户端直传 OSS，字节不经服务器。
 *   2) 未配置 STS / 取凭证失败 / 直传异常 → 回退服务器中转 multipart 上传（本地开发默认走这条）。
 */
@Singleton
class UploadRepository @Inject constructor(
    private val api: HytpApiService,
    @ApplicationContext private val context: Context,
) {
    /**
     * 上传图片文件。uri 为 content:// 或 file://。
     */
    suspend fun uploadImage(uri: Uri): ApiResult<UploadResult> {
        val file = copyToCache(uri) ?: return ApiResult.Failure(Exception("无法读取文件"))
        return try {
            val sts = fetchStsToken()
            if (sts != null && sts.enabled) {
                directUploadToOss(file, sts) ?: relayUpload(file)
            } else {
                relayUpload(file)
            }
        } finally {
            file.delete()
        }
    }

    /** 取 STS 凭证；任何失败返 null → 走中转回退。 */
    private suspend fun fetchStsToken(): StsToken? =
        when (val r = safeApiCall { api.getStsToken() }) {
            is ApiResult.Success -> r.data
            else -> null
        }

    /**
     * OSS 直传（STS 临时凭证）。成功返回可访问 URL；任何异常返 null 让上层回退中转。
     * OSS SDK putObject 为阻塞调用，切到 IO 线程。
     */
    private suspend fun directUploadToOss(file: File, sts: StsToken): ApiResult<UploadResult>? =
        withContext(Dispatchers.IO) {
            try {
                val credentialProvider = OSSStsTokenCredentialProvider(
                    sts.accessKeyId,
                    sts.accessKeySecret,
                    sts.securityToken,
                )
                // endpoint 需带协议
                val endpoint = if (sts.endpoint.startsWith("http")) sts.endpoint else "https://${sts.endpoint}"
                val oss = OSSClient(context.applicationContext, endpoint, credentialProvider)

                val objectKey = sts.dir + file.name  // dir 已含 app/{userId}/{YYYYMM}/，STS 权限也限于此前缀
                val request = PutObjectRequest(sts.bucket, objectKey, file.absolutePath)
                oss.putObject(request) // 阻塞，失败抛异常

                // 公开访问 URL：https://{bucket}.{host}/{key}
                val host = endpoint.removePrefix("https://").removePrefix("http://")
                val url = "https://${sts.bucket}.$host/$objectKey"
                ApiResult.Success(UploadResult(url = url, path = objectKey))
            } catch (_: Exception) {
                null // 直传失败 → 上层回退中转
            }
        }

    /** 服务器中转 multipart 上传（原有逻辑）。 */
    private suspend fun relayUpload(file: File): ApiResult<UploadResult> {
        // 用真实 MIME（如 image/png），后端按具体类型白名单校验，image/* 会被拒
        val mime = context.contentResolver.getType(Uri.fromFile(file)) ?: guessMime(file)
        val requestBody = file.asRequestBody(mime.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        return safeApiCall { api.uploadFile(part) }
    }

    private fun guessMime(file: File): String = when (file.extension.lowercase()) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        else -> "image/jpeg"
    }

    private fun copyToCache(uri: Uri): File? {
        return try {
            val ext = context.contentResolver.getType(uri)?.let { mime ->
                when {
                    mime.contains("png") -> ".png"
                    mime.contains("webp") -> ".webp"
                    mime.contains("gif") -> ".gif"
                    else -> ".jpg"
                }
            } ?: ".jpg"
            val tmp = File.createTempFile("upload_", ext, context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tmp).use { output ->
                    input.copyTo(output)
                }
            }
            if (tmp.length() > 0) tmp else null
        } catch (_: Exception) {
            null
        }
    }
}
