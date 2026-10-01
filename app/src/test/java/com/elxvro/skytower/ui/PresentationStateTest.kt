package com.elxvro.skytower.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PresentationStateTest {
    @Test
    fun tutorialStartsVisibleOnlyWhenRequestedAndCanBeDismissed() {
        val state = PresentationState()
        state.startRun(showTutorial = true, cameraTarget = 0f)
        assertTrue(state.tutorialVisible)
        state.dismissTutorial()
        assertFalse(state.tutorialVisible)

        state.startRun(showTutorial = false, cameraTarget = 0f)
        assertFalse(state.tutorialVisible)
    }

    @Test
    fun cameraMovesTowardTargetWithoutOvershooting() {
        val state = PresentationState()
        state.startRun(showTutorial = false, cameraTarget = 0f)
        state.update(deltaSeconds = 0.05f, cameraTarget = 120f, gameOver = false)
        assertTrue(state.cameraOffset in 0f..120f)

        repeat(40) { state.update(0.05f, 120f, false) }
        assertEquals(120f, state.cameraOffset, 0.5f)
    }

    @Test
    fun landingAnimationReturnsToRest() {
        val state = PresentationState()
        state.startRun(showTutorial = false, cameraTarget = 0f)
        state.triggerLanding(perfect = false, combo = 0)
        assertTrue(state.landingScale > 1f)

        repeat(20) { state.update(0.05f, 0f, false) }
        assertEquals(1f, state.landingScale, 0.01f)
    }

    @Test
    fun perfectComboRaisesEffectIntensityAndThenFades() {
        val state = PresentationState()
        state.startRun(showTutorial = false, cameraTarget = 0f)
        state.triggerLanding(perfect = true, combo = 4)
        val initial = state.perfectIntensity
        assertTrue(initial > 0.8f)

        repeat(20) { state.update(0.05f, 0f, false) }
        assertEquals(0f, state.perfectIntensity, 0.01f)
    }

    @Test
    fun everyPlacementPulsesScoreThenReturnsToRest() {
        val state = PresentationState()
        state.startRun(showTutorial = false, cameraTarget = 0f)
        state.triggerLanding(perfect = false, combo = 0)
        assertTrue(state.scorePulseScale > 1f)

        repeat(20) { state.update(0.05f, 0f, false) }
        assertEquals(1f, state.scorePulseScale, 0.01f)
    }

    @Test
    fun perfectHighComboCreatesStrongerShakeThanLowCombo() {
        val state = PresentationState()
        state.startRun(showTutorial = false, cameraTarget = 0f)
        state.triggerLanding(perfect = true, combo = 1)
        val lowComboShake = state.shakeIntensity

        state.startRun(showTutorial = false, cameraTarget = 0f)
        state.triggerLanding(perfect = true, combo = 5)
        assertTrue(state.shakeIntensity > lowComboShake)

        repeat(20) { state.update(0.05f, 0f, false) }
        assertEquals(0f, state.shakeIntensity, 0.01f)
    }

    @Test
    fun gameOverOverlayProgressIsClampedBetweenZeroAndOne() {
        val state = PresentationState()
        state.startRun(showTutorial = false, cameraTarget = 0f)
        state.update(0.05f, 0f, true)
        assertTrue(state.gameOverProgress in 0f..1f)

        repeat(30) { state.update(0.05f, 0f, true) }
        assertEquals(1f, state.gameOverProgress, 0.001f)

        state.startRun(showTutorial = false, cameraTarget = 0f)
        assertEquals(0f, state.gameOverProgress, 0.001f)
    }
}
