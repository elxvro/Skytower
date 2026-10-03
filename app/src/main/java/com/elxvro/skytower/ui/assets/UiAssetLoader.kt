package com.elxvro.skytower.ui.assets

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.Base64

class UiAssetLoader(private val assetManager: AssetManager) {
    private val cache = mutableMapOf<String, Bitmap?>()

    fun bitmap(path: String): Bitmap? {
        if (cache.containsKey(path)) return cache[path]
        val decoded = decodeDirect(path) ?: decodeBase64Fallback(path)
        cache[path] = decoded
        return decoded
    }

    private fun decodeDirect(path: String): Bitmap? = try {
        assetManager.open(path).use { BitmapFactory.decodeStream(it) }
    } catch (_: Exception) {
        null
    }

    private fun decodeBase64Fallback(path: String): Bitmap? = try {
        val encoded = assetManager.open("$path.b64")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
            .filterNot(Char::isWhitespace)
        val bytes = Base64.getDecoder().decode(encoded)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (_: Exception) {
        null
    }

    fun clear() {
        cache.values.filterNotNull().distinct().forEach { bitmap ->
            if (!bitmap.isRecycled) bitmap.recycle()
        }
        cache.clear()
    }
}

object UiAssetRuntime {
    @Volatile
    var loader: UiAssetLoader? = null
        private set

    fun install(assetManager: AssetManager) {
        loader = UiAssetLoader(assetManager)
    }
}
