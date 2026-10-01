package com.elxvro.skytower.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import kotlin.math.max

class SkyVisualKit {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    fun drawSky(canvas: Canvas, width: Float, height: Float, top: Int = 0xFF1597FF.toInt(), bottom: Int = 0xFFB9EDFF.toInt()) {
        paint.shader = LinearGradient(0f, 0f, 0f, height, top, bottom, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null
        drawCloud(canvas, width * .16f, height * .22f, width * .11f)
        drawCloud(canvas, width * .82f, height * .32f, width * .09f)
        drawCloud(canvas, width * .52f, height * .52f, width * .07f)
        drawIsland(canvas, width * .04f, height * .63f, width * .20f)
        drawIsland(canvas, width * .78f, height * .69f, width * .18f)
    }

    fun drawLogo(canvas: Canvas, width: Float, y: Float) {
        text.textSize = width * .095f
        text.color = Color.WHITE
        text.setShadowLayer(width * .009f, 0f, width * .006f, 0x88053C78.toInt())
        canvas.drawText("SKY", width * .5f, y, text)
        text.textSize = width * .105f
        text.color = 0xFFFFC928.toInt()
        canvas.drawText("TOWER", width * .5f, y + width * .095f, text)
        text.clearShadowLayer()
    }

    fun drawBack(canvas: Canvas, rect: RectF) {
        rounded(canvas, rect, 0xFF267BEA.toInt(), rect.height() * .28f, 0xFF0B4CA7.toInt())
        text.textSize = rect.height() * .48f
        text.color = Color.WHITE
        canvas.drawText("‹", rect.centerX(), rect.centerY() + text.textSize * .34f, text)
    }

    fun drawCoinCapsule(canvas: Canvas, rect: RectF, coins: Int) {
        rounded(canvas, rect, 0xFF123B75.toInt(), rect.height() / 2f, 0xFF082B5E.toInt())
        val r = rect.height() * .27f
        paint.color = 0xFFFFC928.toInt()
        canvas.drawCircle(rect.left + rect.height() * .52f, rect.centerY(), r, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(2f, r * .18f)
        paint.color = 0xFFFFE988.toInt()
        canvas.drawCircle(rect.left + rect.height() * .52f, rect.centerY(), r * .72f, paint)
        paint.style = Paint.Style.FILL
        text.textSize = rect.height() * .38f
        text.color = Color.WHITE
        canvas.drawText(coins.coerceAtLeast(0).toString(), rect.left + rect.width() * .67f, rect.centerY() + text.textSize * .34f, text)
    }

    fun drawRibbon(canvas: Canvas, rect: RectF, title: String) {
        val tail = rect.height() * .32f
        val path = Path().apply {
            moveTo(rect.left - tail, rect.top + rect.height() * .22f)
            lineTo(rect.left, rect.top)
            lineTo(rect.right, rect.top)
            lineTo(rect.right + tail, rect.top + rect.height() * .22f)
            lineTo(rect.right, rect.bottom)
            lineTo(rect.left, rect.bottom)
            close()
        }
        paint.color = 0xFF7B53E8.toInt()
        paint.setShadowLayer(rect.height() * .08f, 0f, rect.height() * .06f, 0x550C235F)
        canvas.drawPath(path, paint)
        paint.clearShadowLayer()
        text.textSize = rect.height() * .47f
        text.color = Color.WHITE
        canvas.drawText(title, rect.centerX(), rect.centerY() + text.textSize * .34f, text)
    }

    fun drawPanel(canvas: Canvas, rect: RectF, color: Int = 0xFFE8F7FF.toInt()) {
        rounded(canvas, rect, color, rect.width() * .04f, 0xFF2577CB.toInt(), strokeWidth = max(3f, rect.width() * .006f))
    }

    fun drawBlueCard(canvas: Canvas, rect: RectF, color: Int = 0xFF2C78D8.toInt()) {
        rounded(canvas, rect, color, rect.width() * .045f, 0xFF1454A7.toInt(), strokeWidth = max(2f, rect.width() * .005f))
    }

    fun drawButton(canvas: Canvas, rect: RectF, label: String, enabled: Boolean = true, green: Boolean = true) {
        val fill = when {
            !enabled -> 0xFF9CAEC3.toInt()
            green -> 0xFF38C85A.toInt()
            else -> 0xFF287BE4.toInt()
        }
        val stroke = when {
            !enabled -> 0xFF71869E.toInt()
            green -> 0xFF158F35.toInt()
            else -> 0xFF0D51AA.toInt()
        }
        rounded(canvas, rect, fill, rect.height() * .28f, stroke, max(2f, rect.height() * .05f))
        text.textSize = rect.height() * .38f
        text.color = Color.WHITE
        canvas.drawText(label, rect.centerX(), rect.centerY() + text.textSize * .34f, text)
    }

    fun drawPrice(canvas: Canvas, rect: RectF, price: Int, locked: Boolean = true) {
        rounded(canvas, rect, 0xFF123B75.toInt(), rect.height() / 2f, 0xFF082B5E.toInt())
        text.textSize = rect.height() * .34f
        text.color = 0xFFFFD54A.toInt()
        val prefix = if (locked) "🔒 " else "● "
        canvas.drawText(prefix + price.coerceAtLeast(0), rect.centerX(), rect.centerY() + text.textSize * .34f, text)
    }

    fun drawProgress(canvas: Canvas, rect: RectF, fraction: Float, fill: Int = 0xFF43D061.toInt()) {
        rounded(canvas, rect, 0xFFB9CBE0.toInt(), rect.height() / 2f, 0xFF6B86A6.toInt(), max(1f, rect.height() * .06f))
        val safe = fraction.coerceIn(0f, 1f)
        if (safe > 0f) {
            val inner = RectF(rect.left, rect.top, rect.left + rect.width() * safe, rect.bottom)
            rounded(canvas, inner, fill, rect.height() / 2f, fill)
        }
    }

    fun drawTitle(canvas: Canvas, value: String, x: Float, y: Float, size: Float, color: Int = 0xFF123B75.toInt()) {
        text.textSize = size
        text.color = color
        canvas.drawText(value, x, y, text)
    }

    fun drawLockBadge(canvas: Canvas, rect: RectF) {
        rounded(canvas, rect, 0xFFF4C338.toInt(), rect.height() * .25f, 0xFFD09000.toInt())
        text.textSize = rect.height() * .52f
        text.color = 0xFF553A00.toInt()
        canvas.drawText("🔒", rect.centerX(), rect.centerY() + text.textSize * .30f, text)
    }

    private fun rounded(canvas: Canvas, rect: RectF, fill: Int, radius: Float, stroke: Int, strokeWidth: Float = 3f) {
        paint.style = Paint.Style.FILL
        paint.color = fill
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = stroke
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawCloud(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        paint.color = 0xEFFFFFFF.toInt()
        canvas.drawCircle(cx - radius * .55f, cy, radius * .55f, paint)
        canvas.drawCircle(cx, cy - radius * .18f, radius * .72f, paint)
        canvas.drawCircle(cx + radius * .62f, cy, radius * .48f, paint)
        canvas.drawOval(RectF(cx - radius, cy, cx + radius, cy + radius * .55f), paint)
    }

    private fun drawIsland(canvas: Canvas, left: Float, top: Float, width: Float) {
        val grass = width * .16f
        val rock = Path().apply {
            moveTo(left, top + grass)
            lineTo(left + width, top + grass)
            lineTo(left + width * .68f, top + width * .80f)
            lineTo(left + width * .45f, top + width)
            lineTo(left + width * .22f, top + width * .68f)
            close()
        }
        paint.color = 0xFF69718F.toInt()
        canvas.drawPath(rock, paint)
        paint.color = 0xFF57D56D.toInt()
        canvas.drawRoundRect(RectF(left - width * .04f, top, left + width * 1.04f, top + grass), grass, grass, paint)
    }
}
