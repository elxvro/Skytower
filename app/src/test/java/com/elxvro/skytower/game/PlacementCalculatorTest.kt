package com.elxvro.skytower.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlacementCalculatorTest {
    private val base = Block(x = 100f, y = 0f, width = 200f, height = 50f, paletteIndex = 0)

    @Test
    fun exactPlacementPreservesFullWidthAndIsPerfect() {
        val moving = Block(x = 100f, y = 50f, width = 200f, height = 50f, paletteIndex = 1)
        val result = PlacementCalculator.calculate(moving, base, perfectTolerance = 4f)
        assertTrue(result is PlacementResult.Success)
        result as PlacementResult.Success
        assertTrue(result.perfect)
        assertEquals(100f, result.placedBlock.x, 0.001f)
        assertEquals(200f, result.placedBlock.width, 0.001f)
        assertNull(result.cutFragment)
    }

    @Test
    fun partialOverlapReturnsSurvivingWidthAndCutFragment() {
        val moving = Block(x = 150f, y = 50f, width = 200f, height = 50f, paletteIndex = 1)
        val result = PlacementCalculator.calculate(moving, base, perfectTolerance = 4f)
        assertTrue(result is PlacementResult.Success)
        result as PlacementResult.Success
        assertFalse(result.perfect)
        assertEquals(150f, result.placedBlock.x, 0.001f)
        assertEquals(150f, result.placedBlock.width, 0.001f)
        requireNotNull(result.cutFragment)
        assertEquals(300f, result.cutFragment.x, 0.001f)
        assertEquals(50f, result.cutFragment.width, 0.001f)
    }

    @Test
    fun zeroOverlapReturnsMiss() {
        val moving = Block(x = 301f, y = 50f, width = 200f, height = 50f, paletteIndex = 1)
        val result = PlacementCalculator.calculate(moving, base, perfectTolerance = 4f)
        assertTrue(result is PlacementResult.Miss)
    }

    @Test
    fun toleranceBoundarySnapsWithoutShrinkingWidth() {
        val moving = Block(x = 104f, y = 50f, width = 200f, height = 50f, paletteIndex = 1)
        val result = PlacementCalculator.calculate(moving, base, perfectTolerance = 4f)
        assertTrue(result is PlacementResult.Success)
        result as PlacementResult.Success
        assertTrue(result.perfect)
        assertEquals(100f, result.placedBlock.x, 0.001f)
        assertEquals(200f, result.placedBlock.width, 0.001f)
    }
}
