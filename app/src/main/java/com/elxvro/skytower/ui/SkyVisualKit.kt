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
import kotlin.math.min

class SkyVisualKit {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    fun drawSky(
        canvas: Canvas,
        width: Float,
        height: Float,
        top: Int = ReferenceDesignTokens.SKY_TOP,
        bottom: Int = ReferenceDesignTokens.SKY_BOTTOM,
    ) {
        paint.shader = LinearGradient(0f, 0f, 0f, height, top, bottom, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.shader = null
        drawCloud(canvas, width * .13f, height * .17f, width * .105f)
        drawCloud(canvas, width * .84f, height * .23f, width * .085f)
        drawCloud(canvas, width * .52f, height * .44f, width * .06f)
        drawIsland(canvas, width * .015f, height * .58f, width * .19f)
        drawIsland(canvas, width * .81f, height * .62f, width * .18f)
        drawIsland(canvas, width * .72f, height * .20f, width * .115f)
    }

    fun drawLogo(canvas: Canvas, width: Float, y: Float) {
        val outline = Paint(text).apply {
            style = Paint.Style.STROKE
            strokeWidth = width * .014f
            color = ReferenceDesignTokens.NAVY_DARK
        }
        text.textSize = width * .112f
        outline.textSize = text.textSize
        canvas.drawText("SKY", width * .5f, y, outline)
        text.color = ReferenceDesignTokens.WHITE
        text.setShadowLayer(width * .012f, 0f, width * .010f, ReferenceDesignTokens.SHADOW)
        canvas.drawText("SKY", width * .5f, y, text)

        text.textSize = width * .126f
        outline.textSize = text.textSize
        canvas.drawText("TOWER", width * .5f, y + width * .108f, outline)
        text.color = ReferenceDesignTokens.GOLD
        canvas.drawText("TOWER", width * .5f, y + width * .108f, text)
        text.clearShadowLayer()
    }

    fun drawBack(canvas: Canvas, rect: RectF) {
        roundedShadow(canvas, rect, ReferenceDesignTokens.BLUE, rect.height() * .28f, ReferenceDesignTokens.BLUE_DARK)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = rect.height() * .15f
        paint.color = Color.WHITE
        val cx = rect.centerX()
        val cy = rect.centerY()
        val dx = rect.width() * .20f
        canvas.drawLine(cx + dx * .45f, cy - dx * .75f, cx - dx * .55f, cy, paint)
        canvas.drawLine(cx - dx * .55f, cy, cx + dx * .45f, cy + dx * .75f, paint)
        canvas.drawLine(cx - dx * .38f, cy, cx + dx * .75f, cy, paint)
        paint.style = Paint.Style.FILL
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawCoinCapsule(canvas: Canvas, rect: RectF, coins: Int) {
        roundedShadow(canvas, rect, ReferenceDesignTokens.NAVY, rect.height() / 2f, ReferenceDesignTokens.NAVY_DARK)
        drawCoin(canvas, rect.left + rect.height() * .52f, rect.centerY(), rect.height() * .31f)
        text.textSize = rect.height() * .40f
        text.color = Color.WHITE
        canvas.drawText(coins.coerceAtLeast(0).toString(), rect.left + rect.width() * .64f, rect.centerY() + text.textSize * .34f, text)
    }

    fun drawLevelCapsule(canvas: Canvas, rect: RectF, level: Int, progress: Float = 0f) {
        roundedShadow(canvas, rect, ReferenceDesignTokens.NAVY, rect.height() * .30f, ReferenceDesignTokens.NAVY_DARK)
        drawCrown(canvas, rect.left + rect.height() * .55f, rect.centerY() - rect.height() * .04f, rect.height() * .30f)
        text.textSize = rect.height() * .30f
        text.color = Color.WHITE
        canvas.drawText("Sv. ${level.coerceAtLeast(1)}", rect.left + rect.width() * .68f, rect.top + rect.height() * .43f, text)
        val bar = RectF(rect.left + rect.width() * .48f, rect.bottom - rect.height() * .22f, rect.right - rect.width() * .08f, rect.bottom - rect.height() * .10f)
        drawProgress(canvas, bar, progress, ReferenceDesignTokens.GOLD)
    }

    fun drawRibbon(canvas: Canvas, rect: RectF, title: String) =
        drawRibbon(canvas, rect, title, ReferenceDesignTokens.RIBBON_RED, ReferenceDesignTokens.RIBBON_RED_DARK)

    fun drawRibbon(canvas: Canvas, rect: RectF, title: String, fill: Int, dark: Int) {
        val tail = rect.height() * .34f
        val path = Path().apply {
            moveTo(rect.left - tail, rect.top + rect.height() * .25f)
            lineTo(rect.left, rect.top + rect.height() * .12f)
            quadTo(rect.centerX(), rect.top - rect.height() * .08f, rect.right, rect.top + rect.height() * .12f)
            lineTo(rect.right + tail, rect.top + rect.height() * .25f)
            lineTo(rect.right, rect.bottom)
            quadTo(rect.centerX(), rect.bottom + rect.height() * .08f, rect.left, rect.bottom)
            close()
        }
        paint.color = fill
        paint.setShadowLayer(rect.height() * .10f, 0f, rect.height() * .07f, ReferenceDesignTokens.SHADOW)
        canvas.drawPath(path, paint)
        paint.clearShadowLayer()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(3f, rect.height() * .055f)
        paint.color = dark
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
        text.textSize = rect.height() * .49f
        text.color = Color.WHITE
        text.setShadowLayer(rect.height() * .035f, 0f, rect.height() * .028f, dark)
        canvas.drawText(title, rect.centerX(), rect.centerY() + text.textSize * .34f, text)
        text.clearShadowLayer()
    }

    fun drawPanel(canvas: Canvas, rect: RectF, color: Int = ReferenceDesignTokens.PANEL) {
        roundedShadow(canvas, rect, color, min(rect.width(), rect.height()) * .055f, ReferenceDesignTokens.BORDER, max(3f, rect.width() * .0055f))
    }

    fun drawBlueCard(canvas: Canvas, rect: RectF, color: Int = ReferenceDesignTokens.BLUE_CARD) {
        roundedShadow(canvas, rect, color, min(rect.width(), rect.height()) * .13f, ReferenceDesignTokens.BLUE_DARK, max(2f, rect.width() * .005f))
    }

    fun drawLightCard(canvas: Canvas, rect: RectF, highlighted: Boolean = false) {
        val fill = if (highlighted) 0xFFFFF1A8.toInt() else ReferenceDesignTokens.PANEL
        val stroke = if (highlighted) ReferenceDesignTokens.GOLD else 0xFFAED7F3.toInt()
        roundedShadow(canvas, rect, fill, min(rect.width(), rect.height()) * .16f, stroke, max(2f, rect.width() * .004f))
    }

    fun drawButton(canvas: Canvas, rect: RectF, label: String, enabled: Boolean = true, green: Boolean = true) {
        val fill = when {
            !enabled -> ReferenceDesignTokens.DISABLED
            green -> ReferenceDesignTokens.ACTION_GREEN
            else -> ReferenceDesignTokens.BLUE
        }
        val stroke = when {
            !enabled -> 0xFF7388A4.toInt()
            green -> ReferenceDesignTokens.ACTION_GREEN_DARK
            else -> ReferenceDesignTokens.BLUE_DARK
        }
        roundedShadow(canvas, rect, fill, rect.height() * .28f, stroke, max(2f, rect.height() * .055f))
        text.textSize = rect.height() * .38f
        text.color = Color.WHITE
        text.setShadowLayer(rect.height() * .035f, 0f, rect.height() * .025f, stroke)
        canvas.drawText(label, rect.centerX(), rect.centerY() + text.textSize * .34f, text)
        text.clearShadowLayer()
    }

    fun drawPrice(canvas: Canvas, rect: RectF, price: Int, locked: Boolean = true) {
        roundedShadow(canvas, rect, ReferenceDesignTokens.NAVY, rect.height() / 2f, ReferenceDesignTokens.NAVY_DARK)
        drawCoin(canvas, rect.left + rect.height() * .43f, rect.centerY(), rect.height() * .23f)
        if (locked) drawLock(canvas, rect.left + rect.height() * .89f, rect.centerY(), rect.height() * .23f)
        text.textSize = rect.height() * .34f
        text.color = Color.WHITE
        canvas.drawText(price.coerceAtLeast(0).toString(), rect.left + rect.width() * .67f, rect.centerY() + text.textSize * .34f, text)
    }

    fun drawProgress(canvas: Canvas, rect: RectF, fraction: Float, fill: Int = ReferenceDesignTokens.ACTION_GREEN) {
        rounded(canvas, rect, 0xFFB8CBE1.toInt(), rect.height() / 2f, 0xFF7694B5.toInt(), max(1f, rect.height() * .07f))
        val safe = fraction.coerceIn(0f, 1f)
        if (safe > 0f) {
            val inner = RectF(rect.left + rect.height() * .04f, rect.top + rect.height() * .08f, max(rect.left + rect.height() * .13f, rect.left + rect.width() * safe), rect.bottom - rect.height() * .08f)
            rounded(canvas, inner, fill, inner.height() / 2f, fill)
        }
    }

    fun drawTitle(canvas: Canvas, value: String, x: Float, y: Float, size: Float, color: Int = ReferenceDesignTokens.TEXT) {
        text.textSize = size
        text.color = color
        canvas.drawText(value, x, y, text)
    }

    fun drawOutlinedTitle(canvas: Canvas, value: String, x: Float, y: Float, size: Float, fill: Int, outline: Int = ReferenceDesignTokens.NAVY_DARK) {
        val stroke = Paint(text).apply {
            textSize = size
            style = Paint.Style.STROKE
            strokeWidth = max(3f, size * .09f)
            color = outline
        }
        canvas.drawText(value, x, y, stroke)
        text.textSize = size
        text.color = fill
        canvas.drawText(value, x, y, text)
    }

    fun drawLockBadge(canvas: Canvas, rect: RectF) {
        roundedShadow(canvas, rect, ReferenceDesignTokens.GOLD, rect.height() * .25f, ReferenceDesignTokens.GOLD_DARK)
        drawLock(canvas, rect.centerX(), rect.centerY(), rect.height() * .32f)
    }

    fun drawCoin(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        paint.style = Paint.Style.FILL
        paint.color = ReferenceDesignTokens.GOLD
        canvas.drawCircle(cx, cy, radius, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(2f, radius * .16f)
        paint.color = ReferenceDesignTokens.GOLD_DARK
        canvas.drawCircle(cx, cy, radius * .88f, paint)
        paint.strokeWidth = max(1.5f, radius * .10f)
        paint.color = 0xFFFFF1A2.toInt()
        canvas.drawCircle(cx, cy, radius * .61f, paint)
        paint.style = Paint.Style.FILL
        drawCrown(canvas, cx, cy, radius * .36f)
    }

    fun drawCrown(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val path = Path().apply {
            moveTo(cx - radius, cy + radius * .55f)
            lineTo(cx - radius * .78f, cy - radius * .55f)
            lineTo(cx - radius * .20f, cy - radius * .05f)
            lineTo(cx, cy - radius * .80f)
            lineTo(cx + radius * .22f, cy - radius * .05f)
            lineTo(cx + radius * .80f, cy - radius * .58f)
            lineTo(cx + radius, cy + radius * .55f)
            close()
        }
        paint.color = ReferenceDesignTokens.GOLD
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(1.5f, radius * .14f)
        paint.color = ReferenceDesignTokens.GOLD_DARK
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
    }

    fun drawCheck(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int = Color.WHITE) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = max(3f, size * .16f)
        paint.color = color
        canvas.drawLine(cx - size * .45f, cy, cx - size * .10f, cy + size * .32f, paint)
        canvas.drawLine(cx - size * .10f, cy + size * .32f, cx + size * .48f, cy - size * .36f, paint)
        paint.style = Paint.Style.FILL
        paint.strokeCap = Paint.Cap.BUTT
    }

    fun drawPlay(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int = Color.WHITE) {
        val path = Path().apply {
            moveTo(cx - size * .34f, cy - size * .48f)
            lineTo(cx + size * .50f, cy)
            lineTo(cx - size * .34f, cy + size * .48f)
            close()
        }
        paint.color = color
        canvas.drawPath(path, paint)
    }

    fun drawGear(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int = Color.WHITE) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(4f, radius * .24f)
        paint.color = color
        canvas.drawCircle(cx, cy, radius * .52f, paint)
        repeat(8) { i ->
            val a = Math.toRadians((i * 45.0))
            val sx = cx + kotlin.math.cos(a).toFloat() * radius * .70f
            val sy = cy + kotlin.math.sin(a).toFloat() * radius * .70f
            val ex = cx + kotlin.math.cos(a).toFloat() * radius
            val ey = cy + kotlin.math.sin(a).toFloat() * radius
            canvas.drawLine(sx, sy, ex, ey, paint)
        }
        paint.style = Paint.Style.FILL
    }

    fun drawGift(canvas: Canvas, rect: RectF) {
        val bowY = rect.top + rect.height() * .26f
        paint.color = 0xFFFF3E76.toInt()
        canvas.drawRoundRect(RectF(rect.left + rect.width()*.15f, bowY, rect.right - rect.width()*.15f, rect.bottom), rect.width()*.08f, rect.width()*.08f, paint)
        paint.color = ReferenceDesignTokens.GOLD
        canvas.drawRect(rect.centerX() - rect.width()*.07f, bowY, rect.centerX() + rect.width()*.07f, rect.bottom, paint)
        canvas.drawRect(rect.left + rect.width()*.10f, bowY - rect.height()*.08f, rect.right - rect.width()*.10f, bowY + rect.height()*.07f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = rect.width()*.08f
        paint.color = ReferenceDesignTokens.GOLD
        canvas.drawOval(RectF(rect.centerX()-rect.width()*.31f, rect.top, rect.centerX()-rect.width()*.02f, bowY+rect.height()*.02f), paint)
        canvas.drawOval(RectF(rect.centerX()+rect.width()*.02f, rect.top, rect.centerX()+rect.width()*.31f, bowY+rect.height()*.02f), paint)
        paint.style = Paint.Style.FILL
    }

    fun drawTrophy(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        paint.color = ReferenceDesignTokens.GOLD
        val cup = RectF(cx-size*.30f, cy-size*.42f, cx+size*.30f, cy+size*.10f)
        canvas.drawRoundRect(cup, size*.12f, size*.12f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size*.12f
        canvas.drawArc(RectF(cx-size*.58f, cy-size*.34f, cx-size*.12f, cy+size*.12f), 80f, 200f, false, paint)
        canvas.drawArc(RectF(cx+size*.12f, cy-size*.34f, cx+size*.58f, cy+size*.12f), -100f, 200f, false, paint)
        paint.style = Paint.Style.FILL
        canvas.drawRect(cx-size*.07f, cy+size*.08f, cx+size*.07f, cy+size*.42f, paint)
        canvas.drawRoundRect(RectF(cx-size*.28f, cy+size*.36f, cx+size*.28f, cy+size*.50f), size*.06f, size*.06f, paint)
    }

    fun drawBolt(canvas: Canvas, cx: Float, cy: Float, size: Float, color: Int = ReferenceDesignTokens.GOLD) {
        val p = Path().apply {
            moveTo(cx + size*.08f, cy-size*.50f)
            lineTo(cx-size*.30f, cy+size*.02f)
            lineTo(cx-size*.02f, cy+size*.02f)
            lineTo(cx-size*.12f, cy+size*.52f)
            lineTo(cx+size*.34f, cy-size*.10f)
            lineTo(cx+size*.06f, cy-size*.10f)
            close()
        }
        paint.color = color
        canvas.drawPath(p, paint)
    }

    fun drawLock(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(2f, size*.14f)
        paint.color = ReferenceDesignTokens.NAVY_DARK
        canvas.drawArc(RectF(cx-size*.40f, cy-size*.48f, cx+size*.40f, cy+size*.12f), 180f, -180f, false, paint)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(cx-size*.48f, cy-size*.02f, cx+size*.48f, cy+size*.52f), size*.14f, size*.14f, paint)
    }

    private fun roundedShadow(canvas: Canvas, rect: RectF, fill: Int, radius: Float, stroke: Int, strokeWidth: Float = 3f) {
        paint.style = Paint.Style.FILL
        paint.setShadowLayer(max(5f, radius * .28f), 0f, max(4f, radius * .20f), ReferenceDesignTokens.SHADOW)
        paint.color = fill
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.clearShadowLayer()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = stroke
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.style = Paint.Style.FILL
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
        paint.color = 0xF2FFFFFF.toInt()
        canvas.drawCircle(cx-radius*.56f, cy, radius*.55f, paint)
        canvas.drawCircle(cx, cy-radius*.18f, radius*.72f, paint)
        canvas.drawCircle(cx+radius*.62f, cy, radius*.48f, paint)
        canvas.drawOval(RectF(cx-radius, cy, cx+radius, cy+radius*.55f), paint)
    }

    private fun drawIsland(canvas: Canvas, left: Float, top: Float, width: Float) {
        val grass = width*.16f
        val rock = Path().apply {
            moveTo(left, top+grass)
            lineTo(left+width, top+grass)
            lineTo(left+width*.70f, top+width*.77f)
            lineTo(left+width*.48f, top+width)
            lineTo(left+width*.22f, top+width*.68f)
            close()
        }
        paint.color = 0xFF6E748A.toInt()
        canvas.drawPath(rock, paint)
        paint.color = 0xFF55D468.toInt()
        canvas.drawRoundRect(RectF(left-width*.04f, top, left+width*1.04f, top+grass), grass, grass, paint)
        paint.color = 0xFF2FA44C.toInt()
        canvas.drawCircle(left+width*.20f, top+grass*.25f, grass*.32f, paint)
        canvas.drawCircle(left+width*.78f, top+grass*.20f, grass*.28f, paint)
    }
}
