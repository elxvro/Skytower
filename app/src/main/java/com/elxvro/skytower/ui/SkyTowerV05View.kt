package com.elxvro.skytower.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.elxvro.skytower.game.Block
import com.elxvro.skytower.game.BlockSkin
import com.elxvro.skytower.game.GameEngine
import com.elxvro.skytower.game.PlacementResult
import com.elxvro.skytower.game.PowerUpType
import com.elxvro.skytower.game.RunMode
import com.elxvro.skytower.game.RunPowerUpState
import com.elxvro.skytower.game.RunStats
import com.elxvro.skytower.platform.GamePreferences
import com.elxvro.skytower.platform.HapticController
import com.elxvro.skytower.platform.PlayerProgress
import com.elxvro.skytower.platform.SoundController
import com.elxvro.skytower.ui.screens.BlockSkinsScreen
import com.elxvro.skytower.ui.screens.DailyMissionsScreen
import com.elxvro.skytower.ui.screens.HomeScreen
import com.elxvro.skytower.ui.screens.LevelScreen
import com.elxvro.skytower.ui.screens.PowerUpsScreen
import com.elxvro.skytower.ui.screens.ScreenAction
import com.elxvro.skytower.ui.screens.ThemeShopScreen
import kotlin.math.max
import kotlin.math.min

class SkyTowerV05View(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {
    private val stateLock = Any()
    private val preferences = GamePreferences(context)
    private val sound = SoundController()
    private val haptics = HapticController(context)
    private val kit = SkyVisualKit()
    private val blockRenderer = BlockSkinRenderer()
    private val homeScreen = HomeScreen(kit)
    private val themeScreen = ThemeShopScreen(kit)
    private val dailyScreen = DailyMissionsScreen(kit)
    private val levelScreen = LevelScreen(kit)
    private val skinsScreen = BlockSkinsScreen(kit, blockRenderer)
    private val powerScreen = PowerUpsScreen(kit)
    private val hud = GameplayHudRenderer(kit)
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    @Volatile private var loopRunning = false
    private var loopThread: Thread? = null
    private var engine: GameEngine? = null
    private var screen: AppScreen = AppScreen.HOME
    private var progress: PlayerProgress = preferences.playerProgress()

    private var runPowerUps = RunPowerUpState(emptySet())
    private var runEquipped: Set<PowerUpType> = emptySet()
    private val consumedRunPowerUps = mutableSetOf<PowerUpType>()
    private var runCoinMultiplier = false
    private var runSkin: BlockSkin = BlockSkin.CLASSIC
    private var runPlacements = 0
    private var runPerfects = 0
    private var runSettled = false
    private var perfectFlash = 0f
    private var newRecord = false
    private var lastRunCoins = 0
    private var lastRunXp = 0
    private var tutorialVisible = false

    init {
        holder.addCallback(this)
        isFocusable = true
        keepScreenOn = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        synchronized(stateLock) {
            refreshProgress()
            ensureEngine()
        }
        startLoop()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        synchronized(stateLock) {
            if (engine == null || engine!!.viewportWidth != width.toFloat()) {
                engine = createEngine(width.coerceAtLeast(1).toFloat())
                if (screen == AppScreen.GAMEPLAY) beginRun()
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) = stopLoop()

    override fun run() {
        var previous = System.nanoTime()
        while (loopRunning) {
            val now = System.nanoTime()
            val delta = ((now - previous) / 1_000_000_000.0).toFloat().coerceIn(0f, 0.20f)
            previous = now
            synchronized(stateLock) { update(delta) }

            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) synchronized(stateLock) { drawFrame(canvas) }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }

            try {
                Thread.sleep(8L)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
        }
    }

    fun pauseForLifecycle() {
        synchronized(stateLock) {
            if (screen == AppScreen.GAMEPLAY) engine?.pause()
        }
    }

    fun release() {
        stopLoop()
        sound.release()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            performClick()
            synchronized(stateLock) { handleTap(event.x, event.y) }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun startLoop() {
        if (loopRunning) return
        loopRunning = true
        loopThread = Thread(this, "SkyTowerV05Loop").also { it.start() }
    }

    private fun stopLoop() {
        loopRunning = false
        loopThread?.interrupt()
        try {
            loopThread?.join(600L)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        loopThread = null
    }

    private fun ensureEngine() {
        if (engine == null) engine = createEngine(width.coerceAtLeast(1).toFloat())
    }

    private fun createEngine(viewportWidth: Float): GameEngine = GameEngine(
        viewportWidth = viewportWidth,
        blockHeight = max(54f, viewportWidth * 0.067f),
        horizontalMargin = max(18f, viewportWidth * 0.035f),
        perfectTolerance = max(5f, viewportWidth * 0.0075f),
    )

    private fun update(delta: Float) {
        if (screen != AppScreen.GAMEPLAY) return
        val active = engine ?: return
        val running = active.mode == RunMode.RUNNING
        runPowerUps.update(delta, running)
        active.update(delta, runPowerUps.speedMultiplier)
        perfectFlash = max(0f, perfectFlash - delta * 2.5f)
    }

    private fun drawFrame(canvas: Canvas) {
        when (screen) {
            AppScreen.HOME -> homeScreen.draw(canvas, progress)
            AppScreen.THEME_SHOP -> themeScreen.draw(canvas, progress)
            AppScreen.DAILY_MISSIONS -> dailyScreen.draw(canvas, progress)
            AppScreen.LEVELS -> levelScreen.draw(canvas, progress)
            AppScreen.BLOCK_SKINS -> skinsScreen.draw(canvas, progress)
            AppScreen.POWER_UPS -> powerScreen.draw(canvas, progress)
            AppScreen.GAMEPLAY -> drawGameplay(canvas)
        }
    }

    private fun handleTap(x: Float, y: Float) {
        if (screen == AppScreen.GAMEPLAY) {
            handleGameplayTap(x, y)
            return
        }
        val action = when (screen) {
            AppScreen.HOME -> homeScreen.actionAt(x, y)
            AppScreen.THEME_SHOP -> themeScreen.actionAt(x, y)
            AppScreen.DAILY_MISSIONS -> dailyScreen.actionAt(x, y)
            AppScreen.LEVELS -> levelScreen.actionAt(x, y)
            AppScreen.BLOCK_SKINS -> skinsScreen.actionAt(x, y)
            AppScreen.POWER_UPS -> powerScreen.actionAt(x, y)
            AppScreen.GAMEPLAY -> ScreenAction.None
        }
        handleScreenAction(action)
    }

    private fun handleScreenAction(action: ScreenAction) {
        when (action) {
            ScreenAction.None -> Unit
            ScreenAction.Back -> screen = AppScreen.HOME
            ScreenAction.ToggleSound -> {
                preferences.soundEnabled = !progress.soundEnabled
                refreshProgress()
                sound.menu(progress.soundEnabled)
            }
            ScreenAction.ToggleVibration -> {
                preferences.vibrationEnabled = !progress.vibrationEnabled
                refreshProgress()
                haptics.placed(progress.vibrationEnabled, false)
            }
            is ScreenAction.Open -> {
                if (action.screen == AppScreen.GAMEPLAY) beginRun() else screen = action.screen
                sound.menu(progress.soundEnabled)
            }
            is ScreenAction.Theme -> {
                if (preferences.isThemeUnlocked(action.themeId)) preferences.themeId = action.themeId
                else preferences.tryUnlockTheme(action.themeId)
                refreshProgress()
                sound.menu(progress.soundEnabled)
            }
            is ScreenAction.ClaimDaily -> {
                preferences.claimDailyMission(action.mission)
                refreshProgress()
                sound.menu(progress.soundEnabled)
            }
            ScreenAction.ClaimDailyAll -> {
                preferences.claimDailyAll()
                refreshProgress()
                sound.menu(progress.soundEnabled)
            }
            is ScreenAction.ClaimLevel -> {
                preferences.claimLevelReward(action.level)
                refreshProgress()
                sound.menu(progress.soundEnabled)
            }
            is ScreenAction.Skin -> {
                preferences.purchaseSkin(action.skin)
                refreshProgress()
                sound.menu(progress.soundEnabled)
            }
            is ScreenAction.BuyPowerUp -> {
                preferences.purchasePowerUp(action.type, "${action.type.name}-${System.nanoTime()}")
                refreshProgress()
                sound.menu(progress.soundEnabled)
            }
            is ScreenAction.TogglePowerUp -> {
                preferences.toggleEquipPowerUp(action.type)
                refreshProgress()
                sound.menu(progress.soundEnabled)
            }
        }
    }

    private fun beginRun() {
        ensureEngine()
        refreshProgress()
        val equipped = progress.equippedPowerUps.toMutableSet()
        consumedRunPowerUps.clear()
        runCoinMultiplier = false
        if (PowerUpType.COIN_MULTIPLIER in equipped) {
            runCoinMultiplier = preferences.consumePowerUp(PowerUpType.COIN_MULTIPLIER)
            if (runCoinMultiplier) consumedRunPowerUps += PowerUpType.COIN_MULTIPLIER else equipped -= PowerUpType.COIN_MULTIPLIER
        }
        runEquipped = equipped.toSet()
        runPowerUps = RunPowerUpState(runEquipped)
        runSkin = progress.selectedSkin
        runPlacements = 0
        runPerfects = 0
        runSettled = false
        perfectFlash = 0f
        newRecord = false
        lastRunCoins = 0
        lastRunXp = 0
        tutorialVisible = !progress.tutorialSeen
        engine!!.start()
        screen = AppScreen.GAMEPLAY
        refreshProgress()
    }

    private fun restartRun() = beginRun()

    private fun handleGameplayTap(x: Float, y: Float) {
        val active = engine ?: return
        when (active.mode) {
            RunMode.RUNNING -> handleRunningTap(x, y, active)
            RunMode.PAUSED -> handlePausedTap(x, y, active)
            RunMode.GAME_OVER -> handleGameOverTap(x, y)
            RunMode.MENU -> beginRun()
        }
    }

    private fun handleRunningTap(x: Float, y: Float, active: GameEngine) {
        when (hud.actionAt(x, y)) {
            GameplayHudAction.Pause -> {
                active.pause()
                sound.menu(progress.soundEnabled)
                return
            }
            GameplayHudAction.ActivateSlowTime -> {
                activateTimedPowerUp(PowerUpType.SLOW_TIME)
                return
            }
            GameplayHudAction.ActivateWidePerfect -> {
                activateTimedPowerUp(PowerUpType.WIDE_PERFECT)
                return
            }
            GameplayHudAction.None -> Unit
        }

        if (tutorialVisible) {
            tutorialVisible = false
            preferences.tutorialSeen = true
            refreshProgress()
        }

        val result = active.drop(runPowerUps.perfectToleranceMultiplier) ?: return
        sound.drop(progress.soundEnabled)
        when (result) {
            PlacementResult.Miss -> handleMiss(active)
            is PlacementResult.Success -> {
                runPlacements++
                if (result.perfect) {
                    runPerfects++
                    perfectFlash = 1f
                }
                sound.placed(progress.soundEnabled, result.perfect, active.combo)
                haptics.placed(progress.vibrationEnabled, result.perfect)
            }
        }
    }

    private fun activateTimedPowerUp(type: PowerUpType) {
        if (type in consumedRunPowerUps) return
        if (!preferences.consumePowerUp(type)) return
        if (!runPowerUps.activate(type)) return
        consumedRunPowerUps += type
        refreshProgress()
        sound.menu(progress.soundEnabled)
    }

    private fun handleMiss(active: GameEngine) {
        if (runPowerUps.tryUseSecondChance() && preferences.consumePowerUp(PowerUpType.SECOND_CHANCE) && active.rescueAfterMiss()) {
            consumedRunPowerUps += PowerUpType.SECOND_CHANCE
            refreshProgress()
            sound.menu(progress.soundEnabled)
            return
        }
        settleRun(active)
        sound.gameOver(progress.soundEnabled)
        haptics.gameOver(progress.vibrationEnabled)
    }

    private fun settleRun(active: GameEngine) {
        if (runSettled) return
        newRecord = active.score > progress.bestScore
        val result = preferences.settleRunV05(
            RunStats(score = active.score, placements = runPlacements, perfects = runPerfects),
            coinMultiplier = runCoinMultiplier,
        )
        lastRunCoins = result.runCoinsAwarded
        lastRunXp = result.xpAwarded
        runSettled = true
        refreshProgress()
    }

    private fun handlePausedTap(x: Float, y: Float, active: GameEngine) {
        val l = ScreenLayout(width.toFloat(), height.toFloat())
        when {
            l.referenceRect(210f, 790f, 660f, 125f).contains(x, y) -> active.resume()
            l.referenceRect(210f, 950f, 660f, 125f).contains(x, y) -> restartRun()
            l.referenceRect(210f, 1110f, 660f, 125f).contains(x, y) -> {
                active.returnToMenu()
                screen = AppScreen.HOME
                refreshProgress()
            }
        }
        sound.menu(progress.soundEnabled)
    }

    private fun handleGameOverTap(x: Float, y: Float) {
        val l = ScreenLayout(width.toFloat(), height.toFloat())
        when {
            l.referenceRect(210f, 1180f, 660f, 130f).contains(x, y) -> restartRun()
            l.referenceRect(210f, 1345f, 660f, 120f).contains(x, y) -> {
                engine?.returnToMenu()
                screen = AppScreen.HOME
                refreshProgress()
            }
        }
        sound.menu(progress.soundEnabled)
    }

    private fun drawGameplay(canvas: Canvas) {
        val active = engine ?: return
        val theme = ThemePalette.get(progress.themeId)
        kit.drawSky(canvas, canvas.width.toFloat(), canvas.height.toFloat(), theme.skyTop, theme.skyBottom)
        drawTower(canvas, active, theme)

        val visiblePowerUps = buildSet {
            if (PowerUpType.SLOW_TIME in runEquipped && (PowerUpType.SLOW_TIME !in consumedRunPowerUps || runPowerUps.slowTimeRemaining > 0f)) add(PowerUpType.SLOW_TIME)
            if (PowerUpType.WIDE_PERFECT in runEquipped && (PowerUpType.WIDE_PERFECT !in consumedRunPowerUps || runPowerUps.widePerfectRemaining > 0f)) add(PowerUpType.WIDE_PERFECT)
        }
        hud.draw(
            canvas,
            GameplayHudState(
                score = active.score,
                bestScore = max(progress.bestScore, active.score),
                coins = progress.coins,
                placements = runPlacements,
                combo = active.combo,
                perfectIntensity = perfectFlash,
                runPowerUps = runPowerUps,
                equipped = visiblePowerUps,
            ),
        )

        if (tutorialVisible && active.mode == RunMode.RUNNING) drawTutorial(canvas)
        when (active.mode) {
            RunMode.PAUSED -> drawPauseOverlay(canvas)
            RunMode.GAME_OVER -> drawGameOverOverlay(canvas, active)
            else -> Unit
        }
    }

    private fun drawTower(canvas: Canvas, active: GameEngine, theme: SkyTheme) {
        val groundY = canvas.height * .83f
        val camera = active.cameraOffsetY
        val blocks = active.placedBlocks
        for (block in blocks) drawBlock(canvas, block, groundY, camera, theme)
        active.movingBlock?.let { drawBlock(canvas, it, groundY, camera, theme) }

        overlayPaint.color = 0x55315F83
        canvas.drawOval(
            RectF(canvas.width * .22f, groundY + 12f, canvas.width * .78f, groundY + 54f),
            overlayPaint,
        )
    }

    private fun drawBlock(canvas: Canvas, block: Block, groundY: Float, camera: Float, theme: SkyTheme) {
        val bottom = groundY - (block.y - camera)
        val rect = RectF(block.x, bottom - block.height, block.x + block.width, bottom)
        if (rect.bottom < -block.height || rect.top > canvas.height + block.height) return
        val color = theme.blocks[Math.floorMod(block.paletteIndex, theme.blocks.size)]
        blockRenderer.draw(canvas, rect, color, runSkin)
    }

    private fun drawTutorial(canvas: Canvas) {
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val panel = l.referenceRect(170f, 1450f, 740f, 180f)
        kit.drawBlueCard(canvas, panel.toRectF(), 0xDD183F82.toInt())
        kit.drawTitle(canvas, "DOKUN VE BLOĞU YERLEŞTİR", panel.centerX, panel.top + 72f * l.scale, 31f * l.scale, Color.WHITE)
        kit.drawTitle(canvas, "Taşan kısım kesilir — PERFECT için hizala", panel.centerX, panel.top + 125f * l.scale, 22f * l.scale, 0xFFD9EEFF.toInt())
    }

    private fun drawPauseOverlay(canvas: Canvas) {
        drawDim(canvas)
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val panel = l.referenceRect(140f, 590f, 800f, 760f)
        kit.drawPanel(canvas, panel.toRectF())
        kit.drawRibbon(canvas, l.referenceRect(280f, 630f, 520f, 105f).toRectF(), "DURAKLATILDI")
        kit.drawButton(canvas, l.referenceRect(210f, 790f, 660f, 125f).toRectF(), "DEVAM ET", green = true)
        kit.drawButton(canvas, l.referenceRect(210f, 950f, 660f, 125f).toRectF(), "YENİDEN BAŞLAT", green = false)
        kit.drawButton(canvas, l.referenceRect(210f, 1110f, 660f, 125f).toRectF(), "ANA MENÜ", green = false)
    }

    private fun drawGameOverOverlay(canvas: Canvas, active: GameEngine) {
        if (!runSettled) settleRun(active)
        drawDim(canvas)
        val l = ScreenLayout(canvas.width.toFloat(), canvas.height.toFloat())
        val panel = l.referenceRect(120f, 500f, 840f, 1040f)
        kit.drawPanel(canvas, panel.toRectF())
        kit.drawRibbon(canvas, l.referenceRect(260f, 545f, 560f, 105f).toRectF(), "OYUN BİTTİ")
        kit.drawTitle(canvas, if (newRecord) "YENİ REKOR!" else "SKOR", panel.centerX, 735f * l.scale, 34f * l.scale, if (newRecord) 0xFFFFC928.toInt() else 0xFF123B75.toInt())
        kit.drawTitle(canvas, active.score.toString(), panel.centerX, 865f * l.scale, 104f * l.scale, 0xFF1957A4.toInt())
        kit.drawTitle(canvas, "+$lastRunCoins COIN", panel.centerX - 180f * l.scale, 1010f * l.scale, 30f * l.scale, 0xFFE29A00.toInt())
        kit.drawTitle(canvas, "+$lastRunXp XP", panel.centerX + 180f * l.scale, 1010f * l.scale, 30f * l.scale, 0xFF644BC7.toInt())
        if (runCoinMultiplier) kit.drawTitle(canvas, "COIN ÇARPANI AKTİF ×2", panel.centerX, 1080f * l.scale, 24f * l.scale, 0xFF2E8D43.toInt())
        kit.drawButton(canvas, l.referenceRect(210f, 1180f, 660f, 130f).toRectF(), "TEKRAR OYNA", green = true)
        kit.drawButton(canvas, l.referenceRect(210f, 1345f, 660f, 120f).toRectF(), "ANA MENÜ", green = false)
    }

    private fun drawDim(canvas: Canvas) {
        overlayPaint.color = 0x99071733.toInt()
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), overlayPaint)
    }

    private fun refreshProgress() {
        progress = preferences.playerProgress()
    }

    private fun UiRect.toRectF(): RectF = RectF(left, top, right, bottom)
}
