package com.zhd.socionet.core.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

object ShareUtils {
    fun shareGatePass(
        context: Context,
        visitorName: String,
        otp: String,
        qrUrl: String,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                val imageLoader = SingletonImageLoader.get(context)
                val request = ImageRequest.Builder(context)
                    .data(qrUrl)
                    .build()

                val result = imageLoader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = result.image.toBitmap()
                    val uri = saveBitmapToCache(context, bitmap, "gate_pass_qr.png")
                    if (uri != null) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TEXT, "Gate Pass for $visitorName\nOTP: $otp\n\nShow to Security.")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Pass"))
                    } else {
                        shareTextOnly(context, visitorName, otp)
                    }
                } else {
                    shareTextOnly(context, visitorName, otp)
                }
            } catch (e: Exception) {
                shareTextOnly(context, visitorName, otp)
            }
        }
    }

    private fun shareTextOnly(context: Context, visitorName: String, otp: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Gate Pass for $visitorName\nOTP: $otp\n\nShow to Security.")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Pass"))
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "shared_images")
            if (!cachePath.exists()) cachePath.mkdirs()
            val file = File(cachePath, fileName)
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            null
        }
    }
}
