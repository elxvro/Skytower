package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.ReferenceDesignTokens
import com.elxvro.skytower.ui.ScreenLayout
import com.elxvro.skytower.ui.SkyVisualKit
import com.elxvro.skytower.ui.UiRect

class SettingsScreen(private val kit: SkyVisualKit) {
    private val hits = mutableListOf<Pair<UiRect, ScreenAction>>()

    fun draw(canvas: Canvas, progress: PlayerProgress) {
        hits.clear()
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val s = l.scale
        kit.drawSky(canvas, canvas.width.toFloat(), canvas.height.toFloat())
        kit.drawLogo(canvas, canvas.width.toFloat(), 135f * s)

        val back = l.referenceRect(36f, 45f, 92f, 82f)
        kit.drawBack(canvas, back.rf())
        hits += back to ScreenAction.Back

        kit.drawRibbon(
            canvas,
            l.referenceRect(215f, 250f, 650f, 112f).rf(),
            "AYARLAR",
            ReferenceDesignTokens.BLUE,
            ReferenceDesignTokens.BLUE_DARK,
        )
        val panel = l.referenceRect(70f, 340f, 940f, 1280f)
        kit.drawPanel(canvas, panel.rf(), 0xFFFFFBF3.toInt())

        val rows = listOf(
            SettingRow("SES", 0xFFFFA91F.toInt(), progress.soundEnabled, ScreenAction.ToggleSound, "sound"),
            SettingRow("TİTREŞİM", 0xFF8B4DE8.toInt(), progress.vibrationEnabled, ScreenAction.ToggleVibration, "vibration"),
            SettingRow("BİLDİRİM", ReferenceDesignTokens.PINK, true, ScreenAction.None, "bell"),
            SettingRow("ÖĞRETİCİ", ReferenceDesignTokens.BLUE, !progress.tutorialSeen, ScreenAction.ToggleTutorial, "tutorial"),
        )

        rows.forEachIndexed { index, row ->
            val top = 455f + index * 155f
            val rect = l.referenceRect(120f, top, 840f, 125f)
            kit.drawLightCard(canvas, rect.rf())
            val icon = l.referenceRect(145f, top + 18f, 92f, 90f)
            kit.drawBlueCard(canvas, icon.rf(), row.iconColor)
            drawIcon(canvas, icon, row.icon, s)
            kit.text.textAlign = android.graphics.Paint.Align.LEFT
            kit.drawTitle(canvas, row.label, rect.left + 145f * s, rect.centerY + 12f * s, 34f * s, ReferenceDesignTokens.TEXT)
            kit.text.textAlign = android.graphics.Paint.Align.CENTER
            val toggle = l.referenceRect(760f, top + 27f, 165f, 72f)
            drawToggle(canvas, toggle.rf(), row.enabled)
            if (row.action != ScreenAction.None) hits += rect to row.action
        }

        drawChoiceRow(canvas, l, 1075f, "KALİTE", "YÜKSEK", 0xFFFFA91F.toInt(), "gear")
        drawChoiceRow(canvas, l, 1230f, "DİL", "TÜRKÇE", ReferenceDesignTokens.CYAN, "globe")

        val reset = l.referenceRect(250f, 1435f, 580f, 105f)
        kit.drawButton(canvas, reset.rf(), "VERİLERİ SIFIRLA", enabled = false, green = false)
        kit.drawTitle(canvas, "Geliştirme sürümünde kilitli", reset.centerX, reset.bottom + 38f * s, 22f * s, ReferenceDesignTokens.TEXT_MUTED)
        kit.drawTitle(canvas, "v0.6 • 2D REFERANS", canvas.width * .5f, 1665f * s, 23f * s, 0xDDFFFFFF.toInt())
    }

    fun actionAt(x: Float, y: Float): ScreenAction =
        hits.lastOrNull { it.first.contains(x, y) }?.second ?: ScreenAction.None

    private fun drawChoiceRow(canvas: Canvas, l: ScreenLayout, top: Float, label: String, value: String, iconColor: Int, iconName: String) {
        val s = l.scale
        val rect = l.referenceRect(120f, top, 840f, 125f)
        kit.drawLightCard(canvas, rect.rf())
        val icon = l.referenceRect(145f, top + 18f, 92f, 90f)
        kit.drawBlueCard(canvas, icon.rf(), iconColor)
        drawIcon(canvas, icon, iconName, s)
        kit.text.textAlign = android.graphics.Paint.Align.LEFT
        kit.drawTitle(canvas, label, rect.left + 145f * s, rect.centerY + 12f * s, 34f * s, ReferenceDesignTokens.TEXT)
        kit.text.textAlign = android.graphics.Paint.Align.CENTER
        val pill = l.referenceRect(655f, top + 22f, 270f, 82f)
        kit.drawPanel(canvas, pill.rf(), 0xFFEAF6FF.toInt())
        kit.drawTitle(canvas, value, pill.centerX - 15f * s, pill.centerY + 12f * s, 30f * s, ReferenceDesignTokens.TEXT)
        kit.paint.color = ReferenceDesignTokens.NAVY
        val tri = android.graphics.Path().apply {
            moveTo(pill.right - 45f * s, pill.centerY - 8f * s)
            lineTo(pill.right - 18f * s, pill.centerY - 8f * s)
            lineTo(pill.right - 31f * s, pill.centerY + 12f * s)
            close()
        }
        canvas.drawPath(tri, kit.paint)
    }

    private fun drawToggle(canvas: Canvas, rect: RectF, enabled: Boolean) {
        kit.paint.color = if (enabled) ReferenceDesignTokens.ACTION_GREEN else ReferenceDesignTokens.DISABLED
        canvas.drawRoundRect(rect, rect.height() / 2f, rect.height() / 2f, kit.paint)
        kit.paint.style = android.graphics.Paint.Style.STROKE
        kit.paint.strokeWidth = rect.height() * .08f
        kit.paint.color = if (enabled) ReferenceDesignTokens.ACTION_GREEN_DARK else 0xFF71869E.toInt()
        canvas.drawRoundRect(rect, rect.height() / 2f, rect.height() / 2f, kit.paint)
        kit.paint.style = android.graphics.Paint.Style.FILL
        val r = rect.height() * .37f
        val cx = if (enabled) rect.right - rect.height() * .50f else rect.left + rect.height() * .50f
        kit.paint.color = Color.WHITE
        canvas.drawCircle(cx, rect.centerY(), r, kit.paint)
    }

    private fun drawIcon(canvas: Canvas, rect: UiRect, type: String, s: Float) {
        val cx = rect.centerX
        val cy = rect.centerY
        when (type) {
            "gear" -> kit.drawGear(canvas, cx, cy, 26f * s)
            "tutorial" -> {
                kit.paint.color = Color.WHITE
                val p = android.graphics.Path().apply {
                    moveTo(cx - 28f*s, cy - 6f*s); lineTo(cx, cy - 22f*s); lineTo(cx + 28f*s, cy - 6f*s); lineTo(cx, cy + 10f*s); close()
                }
                canvas.drawPath(p, kit.paint)
                canvas.drawRect(cx - 7f*s, cy + 10f*s, cx + 7f*s, cy + 30f*s, kit.paint)
            }
            "sound" -> {
                kit.paint.color = Color.WHITE
                canvas.drawRect(cx - 25f*s, cy - 12f*s, cx - 8f*s, cy + 12f*s, kit.paint)
                val p = android.graphics.Path().apply {
                    moveTo(cx - 8f*s, cy - 12f*s); lineTo(cx + 12f*s, cy - 28f*s); lineTo(cx + 12f*s, cy + 28f*s); lineTo(cx - 8f*s, cy + 12f*s); close()
                }
                canvas.drawPath(p, kit.paint)
                kit.paint.style = android.graphics.Paint.Style.STROKE
                kit.paint.strokeWidth = 5f*s
                canvas.drawArc(RectF(cx + 2f*s, cy - 25f*s, cx + 40f*s, cy + 25f*s), -55f, 110f, false, kit.paint)
                kit.paint.style = android.graphics.Paint.Style.FILL
            }
            "vibration" -> {
                kit.paint.color = Color.WHITE
                canvas.drawRoundRect(RectF(cx - 15f*s, cy - 27f*s, cx + 15f*s, cy + 27f*s), 6f*s, 6f*s, kit.paint)
                kit.paint.style = android.graphics.Paint.Style.STROKE
                kit.paint.strokeWidth = 4f*s
                canvas.drawArc(RectF(cx - 37f*s, cy - 24f*s, cx - 17f*s, cy + 24f*s), 95f, 170f, false, kit.paint)
                canvas.drawArc(RectF(cx + 17f*s, cy - 24f*s, cx + 37f*s, cy + 24f*s), -85f, 170f, false, kit.paint)
                kit.paint.style = android.graphics.Paint.Style.FILL
            }
            "bell" -> {
                kit.paint.color = Color.WHITE
                canvas.drawCircle(cx, cy - 8f*s, 18f*s, kit.paint)
                canvas.drawRect(cx - 21f*s, cy - 8f*s, cx + 21f*s, cy + 18f*s, kit.paint)
                canvas.drawCircle(cx, cy + 24f*s, 6f*s, kit.paint)
            }
            "globe" -> {
                kit.paint.style = android.graphics.Paint.Style.STROKE
                kit.paint.strokeWidth = 4f*s
                kit.paint.color = Color.WHITE
                canvas.drawCircle(cx, cy, 27f*s, kit.paint)
                canvas.drawOval(RectF(cx - 12f*s, cy - 27f*s, cx + 12f*s, cy + 27f*s), kit.paint)
                canvas.drawLine(cx - 26f*s, cy, cx + 26f*s, cy, kit.paint)
                kit.paint.style = android.graphics.Paint.Style.FILL
            }
        }
    }

    private data class SettingRow(val label: String, val iconColor: Int, val enabled: Boolean, val action: ScreenAction, val icon: String)
    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
