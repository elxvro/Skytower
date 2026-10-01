package com.elxvro.skytower.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.elxvro.skytower.game.Block
import com.elxvro.skytower.game.GameEngine
import com.elxvro.skytower.game.PlacementResult
import com.elxvro.skytower.game.RunMode
import com.elxvro.skytower.platform.GamePreferences
import com.elxvro.skytower.platform.HapticController
import com.elxvro.skytower.platform.SoundController
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class SkyTowerView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {
    private val stateLock = Any()
    private val preferences = GamePreferences(context)
    private val soundController = SoundController()
    private val hapticController = HapticController(context)
    private val presentation = PresentationState()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    @Volatile private var loopRunning = false
    private var loopThread: Thread? = null
    private var engine: GameEngine? = null
    private var newRecord = false
    private var skyTimeSeconds = 0f
    private val fallingPieces = mutableListOf<FallingPiece>()
    private val perfectParticles = mutableListOf<PerfectParticle>()

    init {
        holder.addCallback(this)
        isFocusable = true
        keepScreenOn = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        synchronized(stateLock) { ensureEngine() }
        startLoop()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        synchronized(stateLock) {
            if (engine == null || engine!!.viewportWidth != width.toFloat()) {
                engine = createEngine(width.coerceAtLeast(1).toFloat())
                presentation.startRun(showTutorial = false, cameraTarget = 0f)
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopLoop()
    }

    override fun run() {
        var previousNanos = System.nanoTime()
        while (loopRunning) {
            val now = System.nanoTime()
            val deltaSeconds = ((now - previousNanos) / 1_000_000_000.0).toFloat().coerceAtLeast(0f)
            previousNanos = now

            synchronized(stateLock) {
                engine?.update(deltaSeconds)
                updateEffects(deltaSeconds)
            }

            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) synchronized(stateLock) { drawScene(canvas) }
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
        synchronized(stateLock) { engine?.pause() }
    }

    fun release() {
        stopLoop()
        soundController.release()
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
        loopThread = Thread(this, "SkyTowerLoop").also { it.start() }
    }

    private fun stopLoop() {
        loopRunning = false
        loopThread?.interrupt()
        try {
            loopThread?.join(500L)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        loopThread = null
    }

    private fun ensureEngine() {
        if (engine == null) engine = createEngine(width.coerceAtLeast(1).toFloat())
    }

    private fun createEngine(viewportWidth: Float): GameEngine {
        val blockHeight = max(54f, viewportWidth * 0.067f)
        val margin = max(18f, viewportWidth * 0.035f)
        val perfectTolerance = max(5f, viewportWidth * 0.0075f)
        return GameEngine(viewportWidth, blockHeight, margin, perfectTolerance)
    }

    private fun handleTap(x: Float, y: Float) {
        val activeEngine = engine ?: return
        when (activeEngine.mode) {
            RunMode.MENU -> handleMenuTap(x, y, activeEngine)
            RunMode.RUNNING -> handleRunningTap(x, y, activeEngine)
            RunMode.PAUSED -> handlePausedTap(x, y, activeEngine)
            RunMode.GAME_OVER -> handleGameOverTap(x, y, activeEngine)
        }
    }

    private fun handleMenuTap(x: Float, y: Float, activeEngine: GameEngine) {
        when {
            playRect().contains(x, y) -> {
                soundController.menu(preferences.soundEnabled)
                beginRun(activeEngine, showTutorial = !preferences.tutorialSeen)
            }
            menuSoundRect().contains(x, y) -> {
                preferences.soundEnabled = !preferences.soundEnabled
                soundController.menu(preferences.soundEnabled)
            }
            menuVibrationRect().contains(x, y) -> {
                preferences.vibrationEnabled = !preferences.vibrationEnabled
                hapticController.placed(preferences.vibrationEnabled, false)
            }
            menuThemeRect().contains(x, y) -> {
                preferences.themeId = (preferences.themeId + 1) % ThemePalette.themes.size
                soundController.menu(preferences.soundEnabled)
            }
        }
    }

    private fun handleRunningTap(x: Float, y: Float, activeEngine: GameEngine) {
        if (presentation.tutorialVisible) {
            presentation.dismissTutorial()
            preferences.tutorialSeen = true
        }

        if (pauseRect().contains(x, y)) {
            activeEngine.pause()
            soundController.menu(preferences.soundEnabled)
            return
        }

        val result = activeEngine.drop() ?: return
        soundController.drop(preferences.soundEnabled)
        when (result) {
            PlacementResult.Miss -> {
                newRecord = preferences.updateBestScore(activeEngine.score)
                soundController.gameOver(preferences.soundEnabled)
                hapticController.gameOver(preferences.vibrationEnabled)
            }
            is PlacementResult.Success -> {
                presentation.triggerLanding(result.perfect, activeEngine.combo)
                result.cutFragment?.let { spawnFallingPiece(it) }
                if (result.perfect) spawnPerfectParticles(activeEngine)
                soundController.placed(preferences.soundEnabled, result.perfect, activeEngine.combo)
                hapticController.placed(preferences.vibrationEnabled, result.perfect)
            }
        }
    }

    private fun handlePausedTap(x: Float, y: Float, activeEngine: GameEngine) {
        when {
            pauseResumeRect().contains(x, y) -> {
                soundController.menu(preferences.soundEnabled)
                activeEngine.resume()
            }
            pauseRestartRect().contains(x, y) -> {
                preferences.updateBestScore(activeEngine.score)
                soundController.menu(preferences.soundEnabled)
                restartRun(activeEngine)
            }
            pauseMenuRect().contains(x, y) -> {
                preferences.updateBestScore(activeEngine.score)
                soundController.menu(preferences.soundEnabled)
                returnToMenu(activeEngine)
            }
            pauseSoundRect().contains(x, y) -> preferences.soundEnabled = !preferences.soundEnabled
            pauseVibrationRect().contains(x, y) -> preferences.vibrationEnabled = !preferences.vibrationEnabled
        }
    }

    private fun handleGameOverTap(x: Float, y: Float, activeEngine: GameEngine) {
        if (presentation.gameOverProgress < 0.72f) return
        when {
            gameOverRetryRect().contains(x, y) -> {
                soundController.menu(preferences.soundEnabled)
                restartRun(activeEngine)
            }
            gameOverMenuRect().contains(x, y) -> {
                soundController.menu(preferences.soundEnabled)
                returnToMenu(activeEngine)
            }
        }
    }

    private fun beginRun(activeEngine: GameEngine, showTutorial: Boolean) {
        clearTransientVisuals()
        newRecord = false
        activeEngine.start()
        presentation.startRun(showTutorial = showTutorial, cameraTarget = activeEngine.cameraOffsetY)
    }

    private fun restartRun(activeEngine: GameEngine) {
        clearTransientVisuals()
        newRecord = false
        activeEngine.restart()
        presentation.startRun(showTutorial = false, cameraTarget = activeEngine.cameraOffsetY)
    }

    private fun returnToMenu(activeEngine: GameEngine) {
        clearTransientVisuals()
        presentation.dismissTutorial()
        presentation.startRun(showTutorial = false, cameraTarget = 0f)
        activeEngine.returnToMenu()
    }

    private fun clearTransientVisuals() {
        fallingPieces.clear()
        perfectParticles.clear()
    }

    private fun updateEffects(deltaSeconds: Float) {
        val dt = min(deltaSeconds.coerceAtLeast(0f), 0.05f)
        val activeEngine = engine
        val cameraTarget = activeEngine?.cameraOffsetY ?: 0f
        presentation.update(
            deltaSeconds = dt,
            cameraTarget = cameraTarget,
            gameOver = activeEngine?.mode == RunMode.GAME_OVER,
        )
        skyTimeSeconds += dt

        val fallingIterator = fallingPieces.iterator()
        while (fallingIterator.hasNext()) {
            val piece = fallingIterator.next()
            piece.velocityY += height.coerceAtLeast(1) * 0.72f * dt
            piece.rect.offset(piece.velocityX * dt, piece.velocityY * dt)
            piece.rotation += piece.angularVelocity * dt
            if (piece.rect.top > height * 1.15f) fallingIterator.remove()
        }

        val particleIterator = perfectParticles.iterator()
        while (particleIterator.hasNext()) {
            val particle = particleIterator.next()
            particle.life -= dt
            particle.velocityY += height.coerceAtLeast(1) * 0.22f * dt
            particle.x += particle.velocityX * dt
            particle.y += particle.velocityY * dt
            if (particle.life <= 0f) particleIterator.remove()
        }
    }

    private fun drawScene(canvas: Canvas) {
        val theme = ThemePalette.get(preferences.themeId)
        drawBackground(canvas, theme)
        val activeEngine = engine
        if (activeEngine == null || activeEngine.mode == RunMode.MENU) {
            drawMenu(canvas, theme)
            return
        }

        drawShakenGameplay(canvas, activeEngine, theme)
        drawHud(canvas, activeEngine)
        drawPlacementFeedback(canvas, activeEngine)

        when (activeEngine.mode) {
            RunMode.RUNNING -> if (presentation.tutorialVisible) drawTutorialOverlay(canvas)
            RunMode.PAUSED -> drawPauseOverlay(canvas)
            RunMode.GAME_OVER -> drawGameOverOverlay(canvas, activeEngine)
            RunMode.MENU -> Unit
        }
    }

    private fun drawShakenGameplay(canvas: Canvas, activeEngine: GameEngine, theme: SkyTheme) {
        val shake = presentation.shakeIntensity.coerceIn(0f, 1f)
        canvas.save()
        if (shake > 0f) {
            val x = sin(skyTimeSeconds * 82f) * width * 0.0065f * shake
            val y = cos(skyTimeSeconds * 67f) * height * 0.0032f * shake
            canvas.translate(x, y)
        }
        drawTower(canvas, activeEngine, theme)
        drawFallingPieces(canvas)
        drawPerfectParticles(canvas)
        canvas.restore()
    }

    private fun drawBackground(canvas: Canvas, theme: SkyTheme) {
        paint.shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            theme.skyTop, theme.skyBottom, Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        val drift = sin(skyTimeSeconds * 0.32f) * width * 0.014f
        val reverseDrift = cos(skyTimeSeconds * 0.21f) * width * 0.010f
        val cameraParallax = presentation.cameraOffset * 0.09f

        drawCloud(canvas, width * 0.18f + drift, height * 0.18f + cameraParallax * 0.18f, width * 0.12f, theme.cloud)
        drawCloud(canvas, width * 0.76f - drift * 0.75f, height * 0.28f + cameraParallax * 0.13f, width * 0.09f, theme.cloud)
        drawCloud(canvas, width * 0.45f + reverseDrift, height * 0.45f + cameraParallax * 0.08f, width * 0.07f, theme.cloud)
        drawFloatingIsland(canvas, width * 0.10f + reverseDrift, height * 0.36f + cameraParallax * 0.48f, width * 0.16f, theme)
        drawFloatingIsland(canvas, width * 0.78f - drift * 0.45f, height * 0.50f + cameraParallax * 0.36f, width * 0.14f, theme)
        drawFloatingIsland(canvas, width * 0.64f + drift * 0.30f, height * 0.14f + cameraParallax * 0.58f, width * 0.10f, theme)
    }

    private fun drawCloud(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int) {
        paint.color = color
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx - radius * 0.55f, cy, radius * 0.55f, paint)
        canvas.drawCircle(cx, cy - radius * 0.18f, radius * 0.72f, paint)
        canvas.drawCircle(cx + radius * 0.62f, cy, radius * 0.48f, paint)
        canvas.drawOval(RectF(cx - radius, cy, cx + radius, cy + radius * 0.58f), paint)
    }

    private fun drawFloatingIsland(canvas: Canvas, left: Float, top: Float, islandWidth: Float, theme: SkyTheme) {
        val grassHeight = islandWidth * 0.18f
        val rock = Path().apply {
            moveTo(left, top + grassHeight)
            lineTo(left + islandWidth, top + grassHeight)
            lineTo(left + islandWidth * 0.68f, top + islandWidth * 0.78f)
            lineTo(left + islandWidth * 0.48f, top + islandWidth)
            lineTo(left + islandWidth * 0.22f, top + islandWidth * 0.68f)
            close()
        }
        paint.color = theme.islandRock
        canvas.drawPath(rock, paint)
        paint.color = theme.islandGrass
        canvas.drawRoundRect(
            RectF(left - islandWidth * 0.04f, top, left + islandWidth * 1.04f, top + grassHeight),
            grassHeight, grassHeight, paint,
        )
    }

    private fun drawMenu(canvas: Canvas, theme: SkyTheme) {
        textPaint.setShadowLayer(width * 0.012f, 0f, width * 0.008f, 0x66000000)
        textPaint.textSize = width * 0.16f
        textPaint.color = Color.WHITE
        canvas.drawText("SKY", width / 2f, height * 0.27f, textPaint)
        textPaint.textSize = width * 0.17f
        textPaint.color = 0xFFFFC52D.toInt()
        canvas.drawText("TOWER", width / 2f, height * 0.36f, textPaint)
        textPaint.clearShadowLayer()

        textPaint.textSize = width * 0.047f
        textPaint.color = Color.WHITE
        canvas.drawText("En yükseğe ulaş!", width / 2f, height * 0.415f, textPaint)
        drawButton(canvas, playRect(), "OYNA", 0xFF156FE8.toInt(), 0.062f)

        textPaint.textSize = width * 0.052f
        textPaint.color = Color.WHITE
        canvas.drawText("EN İYİ  ${preferences.bestScore}", width / 2f, height * 0.64f, textPaint)
        drawSmallToggle(canvas, menuSoundRect(), "SES", preferences.soundEnabled)
        drawSmallToggle(canvas, menuVibrationRect(), "TİTREŞİM", preferences.vibrationEnabled)
        drawSmallToggle(canvas, menuThemeRect(), theme.name.uppercase(), true)

        textPaint.textSize = width * 0.027f
        textPaint.color = 0xCCFFFFFF.toInt()
        canvas.drawText("v0.3", width / 2f, height * 0.82f, textPaint)
    }

    private fun drawTower(canvas: Canvas, activeEngine: GameEngine, theme: SkyTheme) {
        val lastIndex = activeEngine.placedBlocks.lastIndex
        activeEngine.placedBlocks.forEachIndexed { index, block ->
            var rect = blockRect(block, activeEngine)
            if (index == lastIndex && presentation.landingScale > 1.001f) {
                rect = landingRect(rect, presentation.landingScale)
            }
            if (rect.bottom >= -rect.height() && rect.top <= height * 1.1f) {
                drawGlossyBlock(canvas, rect, theme.blocks[block.paletteIndex % theme.blocks.size])
            }
        }

        activeEngine.movingBlock?.let { moving ->
            drawGlossyBlock(canvas, blockRect(moving, activeEngine), theme.blocks[moving.paletteIndex % theme.blocks.size])
        }

        if (presentation.landingScale > 1.001f && activeEngine.placedBlocks.isNotEmpty()) {
            val rect = blockRect(activeEngine.placedBlocks.last(), activeEngine)
            val strength = ((presentation.landingScale - 1f) / 0.10f).coerceIn(0f, 1f)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = max(3f, width * 0.008f)
            paint.color = Color.argb((150 * strength).toInt(), 255, 255, 255)
            val grow = width * 0.018f * (1f - strength)
            val pulseRect = RectF(rect.left - grow, rect.top - grow, rect.right + grow, rect.bottom + grow)
            canvas.drawRoundRect(pulseRect, rect.height() * 0.22f, rect.height() * 0.22f, paint)
            paint.style = Paint.Style.FILL
        }
    }

    private fun blockRect(block: Block, activeEngine: GameEngine): RectF {
        val scale = width / activeEngine.viewportWidth
        val baseY = height * 0.82f
        val bottom = baseY - (block.y - presentation.cameraOffset) * scale
        return RectF(block.x * scale, bottom - block.height * scale, (block.x + block.width) * scale, bottom)
    }

    private fun landingRect(rect: RectF, scale: Float): RectF {
        val xScale = scale.coerceIn(1f, 1.10f)
        val yScale = (1f - (xScale - 1f) * 0.58f).coerceAtLeast(0.94f)
        val newWidth = rect.width() * xScale
        val newHeight = rect.height() * yScale
        return RectF(
            rect.centerX() - newWidth / 2f,
            rect.bottom - newHeight,
            rect.centerX() + newWidth / 2f,
            rect.bottom,
        )
    }

    private fun drawGlossyBlock(canvas: Canvas, rect: RectF, color: Int) {
        val radius = rect.height() * 0.20f
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(55, 0, 0, 0)
        val shadow = RectF(rect).apply { offset(0f, rect.height() * 0.10f) }
        canvas.drawRoundRect(shadow, radius, radius, paint)
        paint.color = color
        canvas.drawRoundRect(rect, radius, radius, paint)

        val highlight = RectF(
            rect.left + rect.width() * 0.05f,
            rect.top + rect.height() * 0.08f,
            rect.right - rect.width() * 0.05f,
            rect.top + rect.height() * 0.34f,
        )
        paint.color = Color.argb(92, 255, 255, 255)
        canvas.drawRoundRect(highlight, radius * 0.75f, radius * 0.75f, paint)
    }

    private fun drawHud(canvas: Canvas, activeEngine: GameEngine) {
        drawButton(canvas, pauseRect(), "Ⅱ", 0x66204A80, 0.050f)
        textPaint.textSize = width * 0.038f
        textPaint.color = Color.WHITE
        canvas.drawText("SKOR", width / 2f, height * 0.075f, textPaint)

        val scoreScale = presentation.scorePulseScale.coerceIn(1f, 1.20f)
        textPaint.textSize = width * 0.105f * scoreScale
        textPaint.setShadowLayer(width * 0.008f * (scoreScale - 0.9f), 0f, 0f, 0x55FFFFFF)
        canvas.drawText(activeEngine.score.toString(), width / 2f, height * 0.13f, textPaint)
        textPaint.clearShadowLayer()

        if (activeEngine.combo >= 2) {
            val comboBoost = presentation.perfectIntensity * min(activeEngine.combo, 6) * 0.004f
            textPaint.textSize = width * (0.05f + comboBoost)
            textPaint.color = 0xFFFFE064.toInt()
            canvas.drawText("x${activeEngine.combo} KOMBO", width * 0.78f, height * 0.105f, textPaint)
        }
    }

    private fun drawPlacementFeedback(canvas: Canvas, activeEngine: GameEngine) {
        val intensity = presentation.perfectIntensity.coerceIn(0f, 1f)
        if (intensity <= 0f) return

        val alpha = (255 * intensity).toInt().coerceIn(0, 255)
        val lift = (1f - intensity) * height * 0.035f
        textPaint.textSize = width * (0.070f + intensity * 0.010f)
        textPaint.color = Color.argb(alpha, 255, 236, 96)
        textPaint.setShadowLayer(width * 0.008f, 0f, width * 0.004f, Color.argb(alpha / 2, 55, 30, 0))
        canvas.drawText("PERFECT!", width / 2f, height * 0.30f - lift, textPaint)
        textPaint.clearShadowLayer()

        if (activeEngine.combo >= 2) {
            textPaint.textSize = width * 0.044f
            textPaint.color = Color.argb(alpha, 255, 255, 255)
            canvas.drawText("${activeEngine.combo} KOMBO", width / 2f, height * 0.342f - lift, textPaint)
        }
    }

    private fun spawnFallingPiece(block: Block) {
        val activeEngine = engine ?: return
        val rect = blockRect(block, activeEngine)
        val theme = ThemePalette.get(preferences.themeId)
        val direction = if (rect.centerX() < width / 2f) -1f else 1f
        fallingPieces += FallingPiece(
            rect = rect,
            color = theme.blocks[block.paletteIndex % theme.blocks.size],
            velocityX = width * 0.08f * direction,
            velocityY = height * 0.06f,
            rotation = 0f,
            angularVelocity = 125f * direction,
        )
    }

    private fun drawFallingPieces(canvas: Canvas) {
        fallingPieces.forEach { piece ->
            canvas.save()
            canvas.rotate(piece.rotation, piece.rect.centerX(), piece.rect.centerY())
            drawGlossyBlock(canvas, piece.rect, piece.color)
            canvas.restore()
        }
    }

    private fun spawnPerfectParticles(activeEngine: GameEngine) {
        val placed = activeEngine.placedBlocks.lastOrNull() ?: return
        val rect = blockRect(placed, activeEngine)
        val colors = intArrayOf(0xFFFFFFFF.toInt(), 0xFFFFE36B.toInt(), 0xFF7FE8FF.toInt(), 0xFFFF8DCE.toInt())
        val particleCount = 12 + min(activeEngine.combo, 6)
        for (index in 0 until particleCount) {
            val angle = (index.toDouble() / particleCount.toDouble()) * PI * 2.0
            val speed = width * (0.15f + (index % 4) * 0.025f)
            val life = 0.45f + (index % 5) * 0.055f
            perfectParticles += PerfectParticle(
                x = rect.centerX(),
                y = rect.centerY(),
                velocityX = cos(angle).toFloat() * speed,
                velocityY = sin(angle).toFloat() * speed - height * 0.035f,
                life = life,
                maxLife = life,
                radius = width * (0.006f + (index % 3) * 0.002f),
                color = colors[index % colors.size],
            )
        }
    }

    private fun drawPerfectParticles(canvas: Canvas) {
        perfectParticles.forEach { particle ->
            val ratio = (particle.life / particle.maxLife).coerceIn(0f, 1f)
            val alpha = (255 * ratio).toInt()
            paint.color = Color.argb(alpha, Color.red(particle.color), Color.green(particle.color), Color.blue(particle.color))
            canvas.drawCircle(particle.x, particle.y, particle.radius * (0.65f + ratio * 0.55f), paint)
        }
    }

    private fun drawTutorialOverlay(canvas: Canvas) {
        paint.color = 0x44091B3A
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        val card = RectF(width * 0.12f, height * 0.57f, width * 0.88f, height * 0.76f)
        paint.color = 0xE8213961.toInt()
        canvas.drawRoundRect(card, width * 0.055f, width * 0.055f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = width * 0.004f
        paint.color = 0x77FFFFFF
        canvas.drawRoundRect(card, width * 0.055f, width * 0.055f, paint)
        paint.style = Paint.Style.FILL

        val pulse = (sin(skyTimeSeconds * 6f) * 0.5f + 0.5f).coerceIn(0f, 1f)
        val fingerY = card.top + card.height() * 0.33f + pulse * height * 0.008f
        paint.color = 0xFFF6D4B9.toInt()
        canvas.drawCircle(width / 2f, fingerY, width * 0.032f, paint)
        canvas.drawRoundRect(
            RectF(width * 0.485f, fingerY, width * 0.515f, fingerY + height * 0.035f),
            width * 0.015f, width * 0.015f, paint,
        )

        textPaint.textSize = width * 0.046f
        textPaint.color = Color.WHITE
        canvas.drawText("DOKUN VE BLOĞU BIRAK", width / 2f, card.top + card.height() * 0.66f, textPaint)
        textPaint.textSize = width * 0.032f
        textPaint.color = 0xFFFFE36B.toInt()
        canvas.drawText("Tam hizala → PERFECT!", width / 2f, card.top + card.height() * 0.84f, textPaint)
    }

    private fun drawPauseOverlay(canvas: Canvas) {
        drawDim(canvas)
        val card = overlayCardRect()
        drawPanel(canvas, card)
        drawOverlayTitle(canvas, "DURAKLATILDI", card.top + card.height() * 0.15f)
        drawButton(canvas, pauseResumeRect(), "DEVAM", 0xFF1E88E5.toInt(), 0.048f)
        drawButton(canvas, pauseRestartRect(), "YENİDEN", 0xFF8E61E8.toInt(), 0.044f)
        drawButton(canvas, pauseMenuRect(), "ANA MENÜ", 0xFF374A6D.toInt(), 0.042f)
        drawSmallToggle(canvas, pauseSoundRect(), "SES", preferences.soundEnabled)
        drawSmallToggle(canvas, pauseVibrationRect(), "TİTREŞİM", preferences.vibrationEnabled)
    }

    private fun drawGameOverOverlay(canvas: Canvas, activeEngine: GameEngine) {
        val progress = presentation.gameOverProgress.coerceIn(0f, 1f)
        if (progress <= 0f) return
        drawDim(canvas, progress)
        val card = overlayCardRect()
        val scale = 0.88f + 0.12f * easeOutCubic(progress)
        canvas.save()
        canvas.scale(scale, scale, card.centerX(), card.centerY())
        drawPanel(canvas, card)
        drawOverlayTitle(canvas, "OYUN BİTTİ", card.top + card.height() * 0.14f)

        textPaint.textSize = width * 0.105f
        textPaint.color = Color.WHITE
        canvas.drawText(activeEngine.score.toString(), width / 2f, card.top + card.height() * 0.31f, textPaint)
        textPaint.textSize = width * 0.041f
        canvas.drawText("EN İYİ  ${preferences.bestScore}", width / 2f, card.top + card.height() * 0.40f, textPaint)
        if (newRecord) {
            textPaint.color = 0xFFFFD84D.toInt()
            canvas.drawText("YENİ REKOR!", width / 2f, card.top + card.height() * 0.47f, textPaint)
        }
        drawButton(canvas, gameOverRetryRect(), "TEKRAR OYNA", 0xFF1E88E5.toInt(), 0.043f)
        drawButton(canvas, gameOverMenuRect(), "ANA MENÜ", 0xFF374A6D.toInt(), 0.042f)
        canvas.restore()
    }

    private fun easeOutCubic(value: Float): Float {
        val inverse = 1f - value.coerceIn(0f, 1f)
        return 1f - inverse * inverse * inverse
    }

    private fun drawDim(canvas: Canvas, alphaScale: Float = 1f) {
        val alpha = (153 * alphaScale.coerceIn(0f, 1f)).toInt()
        paint.color = Color.argb(alpha, 11, 21, 48)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun drawPanel(canvas: Canvas, rect: RectF) {
        paint.color = 0xE6203154.toInt()
        canvas.drawRoundRect(rect, width * 0.06f, width * 0.06f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = max(2f, width * 0.005f)
        paint.color = 0x55FFFFFF
        canvas.drawRoundRect(rect, width * 0.06f, width * 0.06f, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawOverlayTitle(canvas: Canvas, text: String, y: Float) {
        textPaint.textSize = width * 0.063f
        textPaint.color = Color.WHITE
        canvas.drawText(text, width / 2f, y, textPaint)
    }

    private fun drawButton(canvas: Canvas, rect: RectF, label: String, color: Int, textScale: Float) {
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawRoundRect(rect, rect.height() * 0.35f, rect.height() * 0.35f, paint)
        paint.color = 0x26FFFFFF
        canvas.drawRoundRect(
            RectF(rect.left + 3f, rect.top + 3f, rect.right - 3f, rect.top + rect.height() * 0.46f),
            rect.height() * 0.28f, rect.height() * 0.28f, paint,
        )
        textPaint.textSize = width * textScale
        textPaint.color = Color.WHITE
        val baseline = rect.centerY() - (textPaint.ascent() + textPaint.descent()) / 2f
        canvas.drawText(label, rect.centerX(), baseline, textPaint)
    }

    private fun drawSmallToggle(canvas: Canvas, rect: RectF, label: String, enabled: Boolean) {
        paint.color = if (enabled) 0xCC1579D7.toInt() else 0x9940526D.toInt()
        canvas.drawRoundRect(rect, rect.height() * 0.28f, rect.height() * 0.28f, paint)
        textPaint.textSize = width * 0.030f
        textPaint.color = Color.WHITE
        val baseline = rect.centerY() - (textPaint.ascent() + textPaint.descent()) / 2f
        canvas.drawText(label, rect.centerX(), baseline, textPaint)
    }

    private fun playRect() = centeredRect(width * 0.64f, height * 0.083f, height * 0.53f)
    private fun menuSoundRect() = RectF(width * 0.08f, height * 0.70f, width * 0.34f, height * 0.76f)
    private fun menuVibrationRect() = RectF(width * 0.37f, height * 0.70f, width * 0.63f, height * 0.76f)
    private fun menuThemeRect() = RectF(width * 0.66f, height * 0.70f, width * 0.92f, height * 0.76f)
    private fun pauseRect() = RectF(width * 0.035f, height * 0.035f, width * 0.16f, height * 0.095f)
    private fun overlayCardRect() = RectF(width * 0.10f, height * 0.18f, width * 0.90f, height * 0.84f)
    private fun pauseResumeRect() = centeredRect(width * 0.58f, height * 0.071f, height * 0.40f)
    private fun pauseRestartRect() = centeredRect(width * 0.58f, height * 0.066f, height * 0.50f)
    private fun pauseMenuRect() = centeredRect(width * 0.58f, height * 0.066f, height * 0.59f)
    private fun pauseSoundRect() = RectF(width * 0.20f, height * 0.68f, width * 0.47f, height * 0.74f)
    private fun pauseVibrationRect() = RectF(width * 0.53f, height * 0.68f, width * 0.80f, height * 0.74f)
    private fun gameOverRetryRect() = centeredRect(width * 0.62f, height * 0.073f, height * 0.61f)
    private fun gameOverMenuRect() = centeredRect(width * 0.62f, height * 0.066f, height * 0.71f)

    private fun centeredRect(rectWidth: Float, rectHeight: Float, centerY: Float) = RectF(
        (width - rectWidth) / 2f,
        centerY - rectHeight / 2f,
        (width + rectWidth) / 2f,
        centerY + rectHeight / 2f,
    )

    private data class FallingPiece(
        val rect: RectF,
        val color: Int,
        val velocityX: Float,
        var velocityY: Float,
        var rotation: Float,
        val angularVelocity: Float,
    )

    private data class PerfectParticle(
        var x: Float,
        var y: Float,
        val velocityX: Float,
        var velocityY: Float,
        var life: Float,
        val maxLife: Float,
        val radius: Float,
        val color: Int,
    )
}
