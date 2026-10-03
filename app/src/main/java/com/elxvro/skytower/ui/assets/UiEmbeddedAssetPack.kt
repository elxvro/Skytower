package com.elxvro.skytower.ui.assets

object UiEmbeddedAssetPack {
    fun parse(text: String): Map<String, String> = buildMap {
        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEach
            val separator = line.indexOf('|')
            if (separator <= 0 || separator == line.lastIndex) return@forEach
            val key = line.substring(0, separator).trim()
            val value = line.substring(separator + 1).trim()
            if (key.isNotEmpty() && value.isNotEmpty()) put(key, value)
        }
    }
}
