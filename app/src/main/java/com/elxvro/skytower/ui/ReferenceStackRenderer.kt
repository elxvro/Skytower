package com.elxvro.skytower.ui

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.elxvro.skytower.game.BlockSkin

class ReferenceStackRenderer(
    private val blockRenderer: BlockSkinRenderer,
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun drawHome(canvas: Canvas, area: RectF) {
        val spec = ReferenceStackLayout.home()
        drawPlatform(canvas, area, spec)
        val stackArea = RectF(
            area.left + area.width() * .20f,
            area.top + area.height() * .04f,
            area.right - area.width() * .20f,
            area.bottom - area.height() * .26f,
        )
        drawStack(canvas, stackArea, spec, BlockSkin.CLASSIC)
    }

    fun drawPreview(
        canvas: Canvas,
        area: RectF,
        palette: IntArray,
        skin: BlockSkin = BlockSkin.CLASSIC,
        showPlatform: Boolean = false,
    ) {
        val spec = ReferenceStackLayout.preview(palette)
        val stackArea = if (showPlatform) {
            drawPlatform(canvas, area, spec)
            RectF(
                area.left + area.width() * .18f,
                area.top + area.height() * .03f,
                area.right - area.width() * .18f,
                area.bottom - area.height() * .27f,
            )
        } else {
            RectF(
                area.left + area.width() * .07f,
                area.top + area.height() * .03f,
                area.right - area.width() * .07f,
                area.bottom - area.height() * .03f,
            )
        }
        drawStack(canvas, stackArea, spec, skin)
    }

    private fun drawStack(canvas: Canvas, area: RectF, spec: ReferenceStackSpec, skin: BlockSkin) {
        val gap = area.height() * .012f
        val blockH = (area.height() - gap * (spec.blocks.size - 1)) / spec.blocks.size
        spec.blocks.forEachIndexed { index, block ->
            val bottom = area.bottom - index * (blockH + gap)
            val width = area.width() * block.width
            val rect = RectF(
                area.centerX() - width / 2f,
                bottom - blockH,
                area.centerX() + width / 2f,
                bottom,
            )
            blockRenderer.draw(canvas, rect, block.color, skin)
        }
    }

    private fun drawPlatform(canvas: Canvas, area: RectF, spec: ReferenceStackSpec) {
        val grassHeight = area.height() * spec.platformGrassHeight
        val rockDepth = area.height() * spec.platformRockDepth
        val platformTop = area.bottom - rockDepth
        val grass = RectF(
            area.left + area.width() * .10f,
            platformTop,
            area.right - area.width() * .10f,
            platformTop + grassHeight,
        )
        paint.style = Paint.Style.FILL
        paint.color = 0xFF58D36A.toInt()
        canvas.drawRoundRect(grass, grassHeight * .42f, grassHeight * .42f, paint)

        val rock = Path().apply {
            moveTo(grass.left + grass.width() * .07f, grass.bottom - grassHeight * .12f)
            lineTo(grass.right - grass.width() * .07f, grass.bottom - grassHeight * .12f)
            quadTo(
                area.centerX() + area.width() * .20f,
                area.bottom - rockDepth * .05f,
                area.centerX(),
                area.bottom - rockDepth * .01f,
            )
            quadTo(
                area.centerX() - area.width() * .20f,
                area.bottom - rockDepth * .05f,
                grass.left + grass.width() * .07f,
                grass.bottom - grassHeight * .12f,
            )
            close()
        }
        paint.color = 0xFF737B91.toInt()
        canvas.drawPath(rock, paint)
    }
}
