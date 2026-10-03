package com.elxvro.skytower.ui.assets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

 data class SourceSize(val width: Float, val height: Float)
 data class FloatRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

class UiBitmapRenderer {
    fun drawAspectFit(canvas: Canvas, bitmap: Bitmap, target: RectF, paint: Paint? = null) {
        val dest = aspectFit(
            SourceSize(bitmap.width.toFloat(), bitmap.height.toFloat()),
            FloatRect(target.left, target.top, target.right, target.bottom),
        ) ?: return
        canvas.drawBitmap(bitmap, null, RectF(dest.left, dest.top, dest.right, dest.bottom), paint)
    }

    fun drawCenterCrop(canvas: Canvas, bitmap: Bitmap, target: RectF, paint: Paint? = null) {
        val source = centerCropSource(
            SourceSize(bitmap.width.toFloat(), bitmap.height.toFloat()),
            FloatRect(target.left, target.top, target.right, target.bottom),
        ) ?: return
        canvas.drawBitmap(
            bitmap,
            Rect(source.left.toInt(), source.top.toInt(), source.right.toInt(), source.bottom.toInt()),
            target,
            paint,
        )
    }

    companion object {
        fun aspectFit(source: SourceSize, target: FloatRect): FloatRect? {
            if (source.width <= 0f || source.height <= 0f || target.width <= 0f || target.height <= 0f) return null
            val scale = min(target.width / source.width, target.height / source.height)
            val width = source.width * scale
            val height = source.height * scale
            val left = target.left + (target.width - width) * 0.5f
            val top = target.top + (target.height - height) * 0.5f
            return FloatRect(left, top, left + width, top + height)
        }

        fun centerCropSource(source: SourceSize, target: FloatRect): FloatRect? {
            if (source.width <= 0f || source.height <= 0f || target.width <= 0f || target.height <= 0f) return null
            val targetAspect = target.width / target.height
            val sourceAspect = source.width / source.height
            return if (sourceAspect > targetAspect) {
                val cropWidth = source.height * targetAspect
                val left = (source.width - cropWidth) * 0.5f
                FloatRect(left, 0f, left + cropWidth, source.height)
            } else {
                val cropHeight = source.width / targetAspect
                val top = max(0f, (source.height - cropHeight) * 0.5f)
                FloatRect(0f, top, source.width, top + cropHeight)
            }
        }
    }
}
