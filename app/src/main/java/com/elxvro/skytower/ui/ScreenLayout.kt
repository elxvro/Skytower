package com.elxvro.skytower.ui

import kotlin.math.max

data class UiRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    fun contains(x: Float, y: Float): Boolean = x in left..right && y in top..bottom
}

class ScreenLayout(
    val viewportWidth: Float,
    val viewportHeight: Float,
) {
    val scale: Float = viewportWidth.coerceAtLeast(1f) / REFERENCE_WIDTH
    val minTouch: Float = 96f * scale
    val backgroundExtra: Float = max(0f, viewportHeight - REFERENCE_HEIGHT * scale)

    fun cardHeight(referenceHeight: Float): Float = referenceHeight * scale

    fun referenceRect(left: Float, top: Float, width: Float, height: Float): UiRect = UiRect(
        left * scale,
        top * scale,
        (left + width) * scale,
        (top + height) * scale,
    )

    fun touchRect(centerX: Float, centerY: Float, width: Float, height: Float): UiRect {
        val safeWidth = max(width, minTouch)
        val safeHeight = max(height, minTouch)
        return UiRect(centerX - safeWidth / 2f, centerY - safeHeight / 2f, centerX + safeWidth / 2f, centerY + safeHeight / 2f)
    }

    companion object {
        const val REFERENCE_WIDTH = 1080f
        const val REFERENCE_HEIGHT = 1920f
    }
}
