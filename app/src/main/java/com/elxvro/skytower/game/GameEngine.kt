package com.elxvro.skytower.game

import kotlin.math.max
import kotlin.math.min

class GameEngine(
    val viewportWidth: Float,
    private val blockHeight: Float = 72f,
    private val horizontalMargin: Float = 24f,
    private val perfectTolerance: Float = 8f,
) {
    companion object {
        const val MAX_DELTA_SECONDS = 0.05f
        private const val MAX_RECOVERY_SECONDS = 0.20f
        private const val DROP_COOLDOWN_SECONDS = 0.12f
    }

    var mode: RunMode = RunMode.MENU
        private set
    var score: Int = 0
        private set
    var combo: Int = 0
        private set
    var cameraOffsetY: Float = 0f
        private set
    var movementDirection: Float = 1f
        private set
    var movingBlock: Block? = null
        private set
    val placedBlocks: MutableList<Block> = mutableListOf()
    var inputLocked: Boolean = false
        private set

    private var inputLockRemaining = 0f

    val leftBound: Float
        get() = horizontalMargin

    val rightBound: Float
        get() = viewportWidth - horizontalMargin - (movingBlock?.width ?: 0f)

    fun start() {
        resetRun()
        mode = RunMode.RUNNING
    }

    fun restart() = start()

    fun returnToMenu() {
        mode = RunMode.MENU
        inputLocked = false
        inputLockRemaining = 0f
    }

    fun pause() {
        if (mode == RunMode.RUNNING) mode = RunMode.PAUSED
    }

    fun resume() {
        if (mode == RunMode.PAUSED) mode = RunMode.RUNNING
    }

    fun update(deltaSeconds: Float) {
        if (mode != RunMode.RUNNING) return

        val elapsed = deltaSeconds.coerceAtLeast(0f)
        if (inputLockRemaining > 0f) {
            inputLockRemaining = max(0f, inputLockRemaining - elapsed)
            inputLocked = inputLockRemaining > 0f
        }

        var remaining = elapsed.coerceAtMost(MAX_RECOVERY_SECONDS)
        while (remaining > 0f) {
            val dt = min(remaining, MAX_DELTA_SECONDS)
            advanceMovement(dt)
            remaining -= dt
        }
    }

    private fun advanceMovement(deltaSeconds: Float) {
        val moving = movingBlock ?: return
        val speed = DifficultyCurve.speedForScore(score)
        var nextX = moving.x + movementDirection * speed * deltaSeconds
        val minX = leftBound
        val maxX = rightBound.coerceAtLeast(minX)

        if (nextX > maxX) {
            val overshoot = nextX - maxX
            nextX = maxX - overshoot
            movementDirection = -1f
        } else if (nextX < minX) {
            val overshoot = minX - nextX
            nextX = minX + overshoot
            movementDirection = 1f
        }
        moving.x = nextX.coerceIn(minX, maxX)
    }

    fun drop(): PlacementResult? {
        if (mode != RunMode.RUNNING || inputLocked) return null
        val moving = movingBlock ?: return null
        val base = placedBlocks.lastOrNull() ?: return null
        val result = PlacementCalculator.calculate(moving, base, perfectTolerance)

        when (result) {
            PlacementResult.Miss -> {
                combo = 0
                mode = RunMode.GAME_OVER
                inputLocked = true
            }

            is PlacementResult.Success -> {
                val placed = result.placedBlock.copy(y = base.y + blockHeight)
                placedBlocks += placed
                score += 1
                combo = if (result.perfect) combo + 1 else 0
                cameraOffsetY = max(0f, placed.y - blockHeight * 5f)
                spawnMovingBlock(placed)
                inputLockRemaining = DROP_COOLDOWN_SECONDS
                inputLocked = true
            }
        }
        return result
    }

    private fun resetRun() {
        score = 0
        combo = 0
        cameraOffsetY = 0f
        movementDirection = 1f
        inputLockRemaining = 0f
        inputLocked = false
        placedBlocks.clear()

        val initialWidth = min(420f, viewportWidth * 0.42f).coerceAtLeast(180f)
        val base = Block(
            x = (viewportWidth - initialWidth) / 2f,
            y = 0f,
            width = initialWidth,
            height = blockHeight,
            paletteIndex = 0,
        )
        placedBlocks += base
        spawnMovingBlock(base)
    }

    private fun spawnMovingBlock(base: Block) {
        movingBlock = Block(
            x = horizontalMargin,
            y = base.y + blockHeight,
            width = base.width,
            height = blockHeight,
            paletteIndex = (score + 1) % 6,
        )
        movementDirection = if (score % 2 == 0) 1f else -1f
        if (movementDirection < 0f) movingBlock!!.x = rightBound
    }
}
