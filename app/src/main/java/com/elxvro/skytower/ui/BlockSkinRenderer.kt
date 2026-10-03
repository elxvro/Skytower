package com.elxvro.skytower.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.elxvro.skytower.game.BlockSkin
import kotlin.math.max

class BlockSkinRenderer {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun draw(canvas: Canvas, rect: RectF, paletteColor: Int, skin: BlockSkin) {
        val radius = max(3f, rect.height() * .10f)
        paint.shader = null
        paint.clearShadowLayer()
        paint.style = Paint.Style.FILL
        when (skin) {
            BlockSkin.CLASSIC -> classic(canvas, rect, paletteColor, radius)
            BlockSkin.CRYSTAL -> crystal(canvas, rect, paletteColor, radius)
            BlockSkin.MARBLE -> marble(canvas, rect, radius)
            BlockSkin.GOLD -> gold(canvas, rect, radius)
            BlockSkin.ICE -> ice(canvas, rect, radius)
            BlockSkin.NEON -> neon(canvas, rect, radius)
        }
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.clearShadowLayer()
    }

    private fun classic(canvas: Canvas, rect: RectF, color: Int, radius: Float) {
        fillAndStroke(canvas, rect, color, 0x55607793, radius)
        paint.color = 0x66FFFFFF
        val highlight = RectF(
            rect.left + rect.width() * .08f,
            rect.top + rect.height() * .10f,
            rect.right - rect.width() * .08f,
            rect.top + rect.height() * .23f,
        )
        canvas.drawRoundRect(highlight, highlight.height() / 2f, highlight.height() / 2f, paint)
    }

    private fun crystal(canvas: Canvas, rect: RectF, color: Int, radius: Float) {
        val muted = Color.rgb(
            (Color.red(color) * .70f + 90f).toInt().coerceIn(0, 255),
            (Color.green(color) * .70f + 70f).toInt().coerceIn(0, 255),
            (Color.blue(color) * .70f + 70f).toInt().coerceIn(0, 255),
        )
        fillAndStroke(canvas, rect, muted, 0xFFF5FBFF.toInt(), radius, rect.height() * .035f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(1.4f, rect.height() * .022f)
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = 0xEFFFFFFF.toInt()
        val y1 = rect.top + rect.height() * .70f
        val y2 = rect.top + rect.height() * .22f
        canvas.drawLine(rect.left + rect.width() * .18f, y1, rect.left + rect.width() * .48f, y2, paint)
        canvas.drawLine(rect.left + rect.width() * .48f, y2, rect.right - rect.width() * .12f, rect.top + rect.height() * .38f, paint)
        paint.strokeCap = Paint.Cap.BUTT
        paint.style = Paint.Style.FILL
    }

    private fun marble(canvas: Canvas, rect: RectF, radius: Float) {
        fillAndStroke(canvas, rect, 0xFFF2F1EF.toInt(), 0xFFB9BDC4.toInt(), radius, rect.height() * .025f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(1.2f, rect.height() * .018f)
        paint.color = 0xFF9DA2AA.toInt()
        val vein = Path().apply {
            moveTo(rect.left + rect.width() * .08f, rect.centerY())
            cubicTo(
                rect.left + rect.width() * .30f, rect.top + rect.height() * .28f,
                rect.left + rect.width() * .56f, rect.bottom - rect.height() * .10f,
                rect.right - rect.width() * .08f, rect.top + rect.height() * .42f,
            )
        }
        canvas.drawPath(vein, paint)
        paint.style = Paint.Style.FILL
    }

    private fun gold(canvas: Canvas, rect: RectF, radius: Float) {
        fillAndStroke(canvas, rect, 0xFF557B7F.toInt(), 0xFF47686E.toInt(), radius, rect.height() * .025f)
        paint.color = 0xFFBCD2D4.toInt()
        val line = RectF(
            rect.left + rect.width() * .08f,
            rect.top + rect.height() * .11f,
            rect.right - rect.width() * .10f,
            rect.top + rect.height() * .22f,
        )
        canvas.drawRoundRect(line, line.height() / 2f, line.height() / 2f, paint)
    }

    private fun ice(canvas: Canvas, rect: RectF, radius: Float) {
        fillAndStroke(canvas, rect, 0xFF9AD8EE.toInt(), 0xFFEAFDFF.toInt(), radius, rect.height() * .025f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(1.5f, rect.height() * .022f)
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = 0xF2FFFFFF.toInt()
        val p = Path().apply {
            moveTo(rect.centerX() + rect.width() * .05f, rect.top + rect.height() * .10f)
            lineTo(rect.centerX() - rect.width() * .08f, rect.top + rect.height() * .32f)
            lineTo(rect.centerX() + rect.width() * .03f, rect.centerY())
            lineTo(rect.centerX() - rect.width() * .12f, rect.bottom - rect.height() * .08f)
        }
        canvas.drawPath(p, paint)
        paint.strokeCap = Paint.Cap.BUTT
        paint.style = Paint.Style.FILL
    }

    private fun neon(canvas: Canvas, rect: RectF, radius: Float) {
        fillAndStroke(canvas, rect, 0xFF10172A.toInt(), 0xFF14DDF2.toInt(), radius, rect.height() * .045f)
        paint.color = 0xFFFF3CC7.toInt()
        val line = RectF(
            rect.left + rect.width() * .20f,
            rect.top + rect.height() * .17f,
            rect.right - rect.width() * .15f,
            rect.top + rect.height() * .24f,
        )
        canvas.drawRoundRect(line, line.height() / 2f, line.height() / 2f, paint)
    }

    private fun fillAndStroke(
        canvas: Canvas,
        rect: RectF,
        fill: Int,
        stroke: Int,
        radius: Float,
        strokeWidth: Float = max(1.5f, rect.height() * .025f),
    ) {
        paint.style = Paint.Style.FILL
        paint.color = fill
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = stroke
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.style = Paint.Style.FILL
    }
}
