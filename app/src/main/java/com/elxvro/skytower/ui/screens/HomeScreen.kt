package com.elxvro.skytower.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import com.elxvro.skytower.game.LevelRules
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.ui.AppScreen
import com.elxvro.skytower.ui.BlockSkinRenderer
import com.elxvro.skytower.ui.ReferenceDesignTokens
import com.elxvro.skytower.ui.ReferenceStackRenderer
import com.elxvro.skytower.ui.ScreenLayout
import com.elxvro.skytower.ui.SkyVisualKit
import com.elxvro.skytower.ui.UiRect
import com.elxvro.skytower.ui.assets.UiAssetCatalog
import com.elxvro.skytower.ui.assets.UiAssetLoader
import com.elxvro.skytower.ui.assets.UiBitmapRenderer

class HomeScreen(
    private val kit: SkyVisualKit,
    private val assets: UiAssetLoader? = null,
    private val bitmapRenderer: UiBitmapRenderer = UiBitmapRenderer(),
    private val stackRenderer: ReferenceStackRenderer = ReferenceStackRenderer(BlockSkinRenderer()),
) {
    private val hits = mutableListOf<Pair<UiRect, ScreenAction>>()

    fun draw(canvas: Canvas, progress: PlayerProgress) {
        hits.clear()
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val s = l.scale

        val background = assets?.bitmap(UiAssetCatalog.HOME_BACKGROUND)
        if (background != null) {
            bitmapRenderer.drawCenterCrop(canvas, background, RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat()))
        } else {
            kit.drawSky(canvas, canvas.width.toFloat(), canvas.height.toFloat())
        }

        val level = LevelRules.levelForXp(progress.totalXp)
        val coinRect = l.referenceRect(32f, 36f, 285f, 88f)
        val levelRect = l.referenceRect(760f, 36f, 288f, 88f)
        kit.drawCoinCapsule(canvas, coinRect.rf(), progress.coins)
        kit.drawLevelCapsule(canvas, levelRect.rf(), level.level, level.xpIntoLevel.toFloat() / level.xpRequired.toFloat())

        val logoRect = l.referenceRect(195f, 105f, 690f, 290f)
        val logo = assets?.bitmap(UiAssetCatalog.HOME_LOGO)
        if (logo != null) bitmapRenderer.drawAspectFit(canvas, logo, logoRect.rf())
        else kit.drawLogo(canvas, canvas.width.toFloat(), 205f * s)

        val daily = l.referenceRect(30f, 325f, 300f, 200f)
        val best = l.referenceRect(750f, 325f, 300f, 200f)
        drawUtilityCard(canvas, daily, UiAssetCatalog.HOME_CARD_DAILY, UtilityFallback.DAILY, progress)
        drawUtilityCard(canvas, best, UiAssetCatalog.HOME_CARD_BEST_SCORE, UtilityFallback.BEST, progress)
        hits += daily to ScreenAction.Open(AppScreen.DAILY_MISSIONS)

        val tower = l.referenceRect(210f, 505f, 660f, 500f)
        val towerBitmap = assets?.bitmap(UiAssetCatalog.HOME_TOWER_PLATFORM)
        if (towerBitmap != null) bitmapRenderer.drawAspectFit(canvas, towerBitmap, tower.rf())
        else stackRenderer.drawHome(canvas, tower.rf())

        val play = l.referenceRect(180f, 1040f, 720f, 240f)
        val playBitmap = assets?.bitmap(UiAssetCatalog.HOME_BUTTON_PLAY)
        if (playBitmap != null) bitmapRenderer.drawAspectFit(canvas, playBitmap, play.rf())
        else {
            kit.drawButton(canvas, play.rf(), "OYNA", green = true)
            kit.drawPlay(canvas, play.left + 100f*s, play.centerY, 58f*s)
        }
        hits += play to ScreenAction.Open(AppScreen.GAMEPLAY)

        val tiles = listOf(
            MenuTile("TEMALAR", AppScreen.THEME_SHOP, ReferenceDesignTokens.PURPLE, "blocks", UiAssetCatalog.HOME_CARD_THEMES),
            MenuTile("GÖREVLER", AppScreen.DAILY_MISSIONS, ReferenceDesignTokens.ORANGE, "tasks", UiAssetCatalog.HOME_CARD_TASKS),
            MenuTile("SEVİYE", AppScreen.LEVELS, ReferenceDesignTokens.BLUE, "level", UiAssetCatalog.HOME_CARD_LEVEL),
            MenuTile("SKİNLER", AppScreen.BLOCK_SKINS, ReferenceDesignTokens.PINK, "shirt", UiAssetCatalog.HOME_CARD_SKINS),
            MenuTile("GÜÇLENDİRME", AppScreen.POWER_UPS, ReferenceDesignTokens.CYAN, "bolt", UiAssetCatalog.HOME_CARD_POWERUPS),
            MenuTile("AYARLAR", AppScreen.SETTINGS, 0xFF7682A6.toInt(), "gear", UiAssetCatalog.HOME_CARD_SETTINGS),
        )

        tiles.forEachIndexed { index, tile ->
            val col = index % 3
            val row = index / 3
            val x = 35f + col * 347.5f
            val y = 1320f + row * 225f
            val rect = l.referenceRect(x, y, 315f, 207f)
            val bitmap = assets?.bitmap(tile.assetPath)
            if (bitmap != null) {
                bitmapRenderer.drawAspectFit(canvas, bitmap, rect.rf())
            } else {
                kit.drawBlueCard(canvas, rect.rf(), tile.color)
                drawMenuIcon(canvas, rect, tile.icon, s)
                kit.drawOutlinedTitle(canvas, tile.label, rect.centerX, rect.bottom - 20f*s, 25f*s, Color.WHITE)
            }
            hits += rect to ScreenAction.Open(tile.screen)
        }

        kit.drawTitle(canvas, "v0.7 • ASSET UI", canvas.width*.5f, 1815f*s, 22f*s, 0xDDFFFFFF.toInt())
    }

    fun actionAt(x: Float, y: Float): ScreenAction =
        hits.lastOrNull { it.first.contains(x, y) }?.second ?: ScreenAction.None

    private fun drawUtilityCard(
        canvas: Canvas,
        rect: UiRect,
        path: String,
        fallback: UtilityFallback,
        progress: PlayerProgress,
    ) {
        val bitmap = assets?.bitmap(path)
        if (bitmap != null) {
            bitmapRenderer.drawAspectFit(canvas, bitmap, rect.rf())
            if (fallback == UtilityFallback.BEST) {
                kit.drawOutlinedTitle(
                    canvas,
                    progress.bestScore.toString(),
                    rect.centerX,
                    rect.bottom - 20f * (rect.width / 300f),
                    28f * (rect.width / 300f),
                    ReferenceDesignTokens.GOLD,
                )
            }
            return
        }

        kit.drawBlueCard(canvas, rect.rf(), 0xFF1773C7.toInt())
        val scale = rect.width / 300f
        if (fallback == UtilityFallback.DAILY) {
            kit.drawGift(canvas, RectF(rect.left + 90f*scale, rect.top + 16f*scale, rect.right - 90f*scale, rect.top + 112f*scale))
            kit.drawTitle(canvas, "GÜNLÜK GÖREV", rect.centerX, rect.bottom - 24f*scale, 25f*scale, Color.WHITE)
        } else {
            kit.drawTrophy(canvas, rect.centerX, rect.top + 75f*scale, 64f*scale)
            kit.drawTitle(canvas, "EN İYİ SKOR", rect.centerX, rect.bottom - 52f*scale, 24f*scale, Color.WHITE)
            kit.drawTitle(canvas, progress.bestScore.toString(), rect.centerX, rect.bottom - 16f*scale, 30f*scale, ReferenceDesignTokens.GOLD)
        }
    }

    private fun drawMenuIcon(canvas: Canvas, rect: UiRect, icon: String, s: Float) {
        val cx = rect.centerX
        val cy = rect.top + 76f*s
        when (icon) {
            "gear" -> kit.drawGear(canvas, cx, cy, 34f*s)
            "bolt" -> kit.drawBolt(canvas, cx, cy, 58f*s)
            "level" -> kit.drawTrophy(canvas, cx, cy, 52f*s)
            "tasks" -> {
                kit.paint.color = Color.WHITE
                canvas.drawRoundRect(RectF(cx-34f*s, cy-42f*s, cx+34f*s, cy+42f*s), 9f*s, 9f*s, kit.paint)
                kit.drawCheck(canvas, cx, cy+3f*s, 36f*s, ReferenceDesignTokens.TEXT)
            }
            "shirt" -> {
                val p = android.graphics.Path().apply {
                    moveTo(cx-48f*s, cy-30f*s); lineTo(cx-22f*s, cy-45f*s); lineTo(cx-10f*s, cy-25f*s)
                    lineTo(cx+10f*s, cy-25f*s); lineTo(cx+22f*s, cy-45f*s); lineTo(cx+48f*s, cy-30f*s)
                    lineTo(cx+34f*s, cy-5f*s); lineTo(cx+22f*s, cy-12f*s); lineTo(cx+22f*s, cy+42f*s)
                    lineTo(cx-22f*s, cy+42f*s); lineTo(cx-22f*s, cy-12f*s); lineTo(cx-34f*s, cy-5f*s); close()
                }
                kit.paint.color = Color.WHITE
                canvas.drawPath(p, kit.paint)
            }
            else -> {
                val colors = intArrayOf(0xFF22B8FF.toInt(), 0xFFFFD136.toInt(), 0xFFF2498D.toInt())
                for (i in 0..2) {
                    kit.paint.color = colors[i]
                    val dx = (i-1)*30f*s
                    canvas.drawRoundRect(RectF(cx+dx-20f*s, cy+(i%2)*16f*s-20f*s, cx+dx+20f*s, cy+(i%2)*16f*s+20f*s), 7f*s, 7f*s, kit.paint)
                }
            }
        }
    }

    private enum class UtilityFallback { DAILY, BEST }
    private data class MenuTile(
        val label: String,
        val screen: AppScreen,
        val color: Int,
        val icon: String,
        val assetPath: String,
    )

    private fun UiRect.rf(): RectF = RectF(left, top, right, bottom)
}
