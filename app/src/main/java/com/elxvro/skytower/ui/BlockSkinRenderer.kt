package com.elxvro.skytower.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import com.elxvro.skytower.game.BlockSkin
import kotlin.math.max

class BlockSkinRenderer {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun draw(canvas: Canvas, rect: RectF, paletteColor: Int, skin: BlockSkin) {
        val radius = rect.height() * .20f
        paint.style = Paint.Style.FILL
        paint.shader = null
        when (skin) {
            BlockSkin.CLASSIC -> classic(canvas, rect, paletteColor, radius)
            BlockSkin.CRYSTAL -> crystal(canvas, rect, paletteColor, radius)
            BlockSkin.MARBLE -> marble(canvas, rect, radius)
            BlockSkin.GOLD -> gold(canvas, rect, radius)
            BlockSkin.ICE -> ice(canvas, rect, radius)
            BlockSkin.NEON -> neon(canvas, rect, radius)
        }
    }

    private fun shadow(canvas: Canvas, rect: RectF, radius: Float) {
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(55, 0, 0, 0)
        val shadow = RectF(rect).apply { offset(0f, rect.height() * .10f) }
        canvas.drawRoundRect(shadow, radius, radius, paint)
    }

    private fun classic(canvas: Canvas, rect: RectF, color: Int, radius: Float) {
        shadow(canvas, rect, radius)
        paint.color = color
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.color = Color.argb(100, 255, 255, 255)
        canvas.drawRoundRect(RectF(rect.left + rect.width()*.05f, rect.top + rect.height()*.08f, rect.right - rect.width()*.05f, rect.top + rect.height()*.34f), radius*.65f, radius*.65f, paint)
    }

    private fun crystal(canvas: Canvas, rect: RectF, color: Int, radius: Float) {
        shadow(canvas, rect, radius)
        paint.color = Color.argb(145, Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(3f, rect.height() * .06f)
        paint.color = 0xD9E9FCFF.toInt()
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.strokeWidth = max(2f, rect.height() * .035f)
        canvas.drawLine(rect.left + rect.width()*.15f, rect.bottom - rect.height()*.12f, rect.left + rect.width()*.48f, rect.top + rect.height()*.12f, paint)
        canvas.drawLine(rect.left + rect.width()*.48f, rect.top + rect.height()*.12f, rect.right - rect.width()*.12f, rect.top + rect.height()*.32f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun marble(canvas: Canvas, rect: RectF, radius: Float) {
        shadow(canvas, rect, radius)
        paint.color = 0xFFF0EEF0.toInt()
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(1.5f, rect.height()*.025f)
        paint.color = 0xFF9896A3.toInt()
        val vein = Path().apply {
            moveTo(rect.left + rect.width()*.08f, rect.top + rect.height()*.72f)
            cubicTo(rect.left + rect.width()*.28f, rect.top + rect.height()*.28f, rect.left + rect.width()*.52f, rect.bottom, rect.right - rect.width()*.08f, rect.top + rect.height()*.22f)
        }
        canvas.drawPath(vein, paint)
        paint.style = Paint.Style.FILL
    }

    private fun gold(canvas: Canvas, rect: RectF, radius: Float) {
        shadow(canvas, rect, radius)
        paint.shader = LinearGradient(rect.left, rect.top, rect.right, rect.bottom, intArrayOf(0xFFFFF19A.toInt(), 0xFFFFC127.toInt(), 0xFFCE7A00.toInt(), 0xFFFFE269.toInt()), null, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.shader = null
        paint.color = 0x99FFFFFF.toInt()
        canvas.drawRoundRect(RectF(rect.left + rect.width()*.06f, rect.top + rect.height()*.08f, rect.right - rect.width()*.15f, rect.top + rect.height()*.28f), radius*.5f, radius*.5f, paint)
    }

    private fun ice(canvas: Canvas, rect: RectF, radius: Float) {
        shadow(canvas, rect, radius)
        paint.color = 0xCCB9F2FF.toInt()
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(2f, rect.height()*.035f)
        paint.color = 0xFFE8FDFF.toInt()
        canvas.drawRoundRect(rect, radius, radius, paint)
        canvas.drawLine(rect.centerX(), rect.top + rect.height()*.12f, rect.centerX() - rect.width()*.13f, rect.centerY(), paint)
        canvas.drawLine(rect.centerX() - rect.width()*.13f, rect.centerY(), rect.centerX() + rect.width()*.10f, rect.bottom - rect.height()*.10f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun neon(canvas: Canvas, rect: RectF, radius: Float) {
        shadow(canvas, rect, radius)
        paint.color = 0xFF131527.toInt()
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(4f, rect.height()*.065f)
        paint.setShadowLayer(rect.height()*.14f, 0f, 0f, 0xFF00E5FF.toInt())
        paint.color = 0xFF00E5FF.toInt()
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.clearShadowLayer()
        paint.color = 0xFFFF2BC2.toInt()
        canvas.drawLine(rect.left + rect.width()*.20f, rect.top + rect.height()*.25f, rect.right - rect.width()*.14f, rect.top + rect.height()*.25f, paint)
        paint.style = Paint.Style.FILL
    }
}
