package com.example.hytp.core.data

import android.content.Context
import android.net.Uri
import com.example.hytp.core.network.ApiResult
import com.example.hytp.core.network.HytpApiService
import com.example.hytp.core.network.dto.UploadResult
import com.example.hytp.core.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 文件上传仓库。先复制到缓存目录再上传（避免直接读 content URI 问题）。
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
        // 用真实 MIME（如 image/png），后端按具体类型白名单校验，image/* 会被拒
        val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
        val requestBody = file.asRequestBody(mime.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val result = safeApiCall { api.uploadFile(part) }
        // 上传后清理临时文件
        file.delete()
        return result
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
