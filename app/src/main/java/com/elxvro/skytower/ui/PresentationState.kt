package com.elxvro.skytower.ui

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class PresentationState {
    var tutorialVisible: Boolean = false
        private set
    var cameraOffset: Float = 0f
        private set
    var landingScale: Float = 1f
        private set
    var perfectIntensity: Float = 0f
        private set
    var scorePulseScale: Float = 1f
        private set
    var shakeIntensity: Float = 0f
        private set
    var gameOverProgress: Float = 0f
        private set

    private var landingRemaining = 0f
    private var perfectRemaining = 0f
    private var scorePulseRemaining = 0f
    private var shakeRemaining = 0f
    private var shakeDuration = 0f

    fun startRun(showTutorial: Boolean, cameraTarget: Float) {
        tutorialVisible = showTutorial
        cameraOffset = cameraTarget
        landingScale = 1f
        perfectIntensity = 0f
        scorePulseScale = 1f
        shakeIntensity = 0f
        gameOverProgress = 0f
        landingRemaining = 0f
        perfectRemaining = 0f
        scorePulseRemaining = 0f
        shakeRemaining = 0f
        shakeDuration = 0f
    }

    fun dismissTutorial() {
        tutorialVisible = false
    }

    fun triggerLanding(perfect: Boolean, combo: Int) {
        val safeCombo = combo.coerceAtLeast(0)
        landingRemaining = LANDING_DURATION
        landingScale = if (perfect) 1.10f else 1.065f

        scorePulseRemaining = SCORE_PULSE_DURATION
        scorePulseScale = if (perfect) {
            min(1.20f, 1.13f + safeCombo * 0.012f)
        } else {
            1.08f
        }

        if (perfect) {
            perfectRemaining = PERFECT_DURATION + min(safeCombo, 5) * 0.035f
            perfectIntensity = min(1f, 0.76f + safeCombo * 0.07f)
            shakeDuration = SHAKE_DURATION + min(safeCombo, 5) * 0.012f
            shakeRemaining = shakeDuration
            shakeIntensity = min(1f, 0.24f + safeCombo * 0.12f)
        }
    }

    fun update(deltaSeconds: Float, cameraTarget: Float, gameOver: Boolean) {
        val dt = deltaSeconds.coerceIn(0f, MAX_DELTA)
        val cameraBlend = (dt * CAMERA_RESPONSE).coerceIn(0f, 1f)
        cameraOffset += (cameraTarget - cameraOffset) * cameraBlend
        if (abs(cameraTarget - cameraOffset) < 0.25f) cameraOffset = cameraTarget

        landingRemaining = max(0f, landingRemaining - dt)
        landingScale = if (landingRemaining > 0f) {
            1f + 0.10f * (landingRemaining / LANDING_DURATION)
        } else {
            1f
        }

        perfectRemaining = max(0f, perfectRemaining - dt)
        perfectIntensity = if (perfectRemaining > 0f) {
            (perfectIntensity - dt * PERFECT_FADE_RATE).coerceAtLeast(0f)
        } else {
            0f
        }

        scorePulseRemaining = max(0f, scorePulseRemaining - dt)
        scorePulseScale = if (scorePulseRemaining > 0f) {
            1f + 0.14f * (scorePulseRemaining / SCORE_PULSE_DURATION)
        } else {
            1f
        }

        shakeRemaining = max(0f, shakeRemaining - dt)
        shakeIntensity = if (shakeRemaining > 0f && shakeDuration > 0f) {
            shakeIntensity * (shakeRemaining / shakeDuration).coerceIn(0f, 1f)
        } else {
            0f
        }

        gameOverProgress = if (gameOver) {
            (gameOverProgress + dt / GAME_OVER_DURATION).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    companion object {
        private const val MAX_DELTA = 0.05f
        private const val CAMERA_RESPONSE = 8f
        private const val LANDING_DURATION = 0.18f
        private const val PERFECT_DURATION = 0.48f
        private const val PERFECT_FADE_RATE = 1.7f
        private const val SCORE_PULSE_DURATION = 0.22f
        private const val SHAKE_DURATION = 0.16f
        private const val GAME_OVER_DURATION = 0.28f
    }
}
