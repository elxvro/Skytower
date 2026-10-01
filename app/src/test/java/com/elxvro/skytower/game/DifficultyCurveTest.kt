package com.elxvro.skytower.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyCurveTest {
    @Test
    fun speedStartsForgivingAndIncreasesWithScore() {
        assertEquals(220f, DifficultyCurve.speedForScore(0), 0.001f)
        assertTrue(DifficultyCurve.speedForScore(10) > DifficultyCurve.speedForScore(0))
    }

    @Test
    fun speedNeverExceedsCap() {
        assertEquals(560f, DifficultyCurve.speedForScore(10_000), 0.001f)
    }

    @Test
    fun negativeScoreUsesMinimumSpeed() {
        assertEquals(220f, DifficultyCurve.speedForScore(-5), 0.001f)
    }
}
