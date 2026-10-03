package com.elxvro.skytower.ui.assets

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class UiAssetLoader(private val assetManager: AssetManager) {
    private val cache = mutableMapOf<String, Bitmap?>()

    fun bitmap(path: String): Bitmap? {
        if (cache.containsKey(path)) return cache[path]
        val decoded = try {
            assetManager.open(path).use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            null
        }
        cache[path] = decoded
        return decoded
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
