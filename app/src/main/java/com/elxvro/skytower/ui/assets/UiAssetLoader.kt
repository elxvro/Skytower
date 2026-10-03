package com.elxvro.skytower.ui.assets

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.Base64

class UiAssetLoader(private val assetManager: AssetManager) {
    private val cache = mutableMapOf<String, Bitmap?>()
    private var embeddedIndex: Map<String, String>? = null

    private val embeddedPackPaths = listOf(
        "ui/home/pack_core.txt",
        "ui/home/pack_center.txt",
        "ui/home/pack_cards_a.txt",
        "ui/home/pack_cards_b.txt",
    )

    fun bitmap(path: String): Bitmap? {
        if (cache.containsKey(path)) return cache[path]
        val decoded = decodeDirect(path) ?: decodeEmbedded(path)
        cache[path] = decoded
        return decoded
    }

    private fun decodeDirect(path: String): Bitmap? = try {
        assetManager.open(path).use { BitmapFactory.decodeStream(it) }
    } catch (_: Exception) {
        null
    }

    private fun decodeEmbedded(path: String): Bitmap? {
        val encoded = embeddedAssets()[path] ?: return null
        return try {
            val bytes = Base64.getDecoder().decode(encoded)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }

    private fun embeddedAssets(): Map<String, String> {
        embeddedIndex?.let { return it }
        val loaded = linkedMapOf<String, String>()
        embeddedPackPaths.forEach { packPath ->
            val text = try {
                assetManager.open(packPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
            } catch (_: Exception) {
                null
            }
            if (text != null) loaded.putAll(UiEmbeddedAssetPack.parse(text))
        }
        embeddedIndex = loaded
        return loaded
    }

    fun clear() {
        cache.values.filterNotNull().distinct().forEach { bitmap ->
            if (!bitmap.isRecycled) bitmap.recycle()
        }
        cache.clear()
        embeddedIndex = null
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
