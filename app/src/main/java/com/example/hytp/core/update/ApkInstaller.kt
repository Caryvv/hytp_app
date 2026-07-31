package com.example.hytp.core.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/**
 * APK 下载 + 拉起系统安装器。
 *
 * ★仅用于内测直发 APK。上架国内应用商店时，商店禁止 app 自行下载安装，
 *   届时改为跳应用商店/外链下载（本类不再使用，弹窗按钮改行为即可）。
 */
object ApkInstaller {

    sealed interface Progress {
        data class Downloading(val percent: Int) : Progress
        data class Done(val file: File) : Progress
        data class Failed(val error: String) : Progress
    }

    /** 下载 APK，边下边发进度；完成后发 Done(file)。 */
    fun download(context: Context, url: String): Flow<Progress> = flow {
        val client = OkHttpClient()
        val resp = client.newCall(Request.Builder().url(url).build()).execute()
        if (!resp.isSuccessful) {
            emit(Progress.Failed("下载失败(${resp.code})"))
            return@flow
        }
        val body = resp.body ?: run {
            emit(Progress.Failed("下载内容为空"))
            return@flow
        }
        val total = body.contentLength()
        val outFile = File(context.getExternalFilesDir("apk"), "update.apk")
        outFile.parentFile?.mkdirs()

        body.byteStream().use { input ->
            outFile.outputStream().use { output ->
                val buf = ByteArray(8 * 1024)
                var readTotal = 0L
                var read: Int
                var lastPercent = -1
                while (input.read(buf).also { read = it } != -1) {
                    output.write(buf, 0, read)
                    readTotal += read
                    if (total > 0) {
                        val p = (readTotal * 100 / total).toInt()
                        if (p != lastPercent) {
                            lastPercent = p
                            emit(Progress.Downloading(p))
                        }
                    }
                }
            }
        }
        emit(Progress.Done(outFile))
    }.flowOn(Dispatchers.IO)

    /** 是否已允许安装未知来源应用（Android 8+；8 以下无此限制恒 true）。 */
    fun canInstall(context: Context): Boolean =
        android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()

    /** 跳到「允许未知来源」授权页。 */
    fun requestInstallPermission(context: Context) {
        val intent = Intent(
            android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** 用系统安装器打开下载好的 APK。 */
    fun install(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
