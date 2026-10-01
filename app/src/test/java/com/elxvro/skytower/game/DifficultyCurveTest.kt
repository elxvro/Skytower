package com.elxvro.skytower.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyCurveTest {
    @Test
    fun earlyGameStartsMoreForgiving() {
        assertEquals(205f, DifficultyCurve.speedForScore(0), 0.001f)
        assertEquals(259f, DifficultyCurve.speedForScore(12), 0.001f)
    }

    @Test
    fun midGameRampsUpWithoutAJump() {
        val early = DifficultyCurve.speedForScore(12)
        val mid = DifficultyCurve.speedForScore(35)
        assertEquals(397f, mid, 0.001f)
        assertTrue(mid > early)
    }

    @Test
    fun lateGameUsesControlledCap() {
        assertEquals(500f, DifficultyCurve.speedForScore(10_000), 0.001f)
    }

    @Test
    fun negativeScoreUsesMinimumSpeed() {
        assertEquals(205f, DifficultyCurve.speedForScore(-5), 0.001f)
    }
}
