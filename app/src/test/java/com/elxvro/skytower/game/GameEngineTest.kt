package com.elxvro.skytower.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {
    private fun engine(): GameEngine = GameEngine(viewportWidth = 1080f)

    @Test fun successfulPerfectDropIncrementsScoreAndCombo() {
        val engine = engine(); engine.start(); engine.movingBlock!!.x = engine.placedBlocks.last().x
        val result = engine.drop()
        assertTrue(result is PlacementResult.Success && result.perfect)
        assertEquals(1, engine.score); assertEquals(1, engine.combo); assertEquals(2, engine.placedBlocks.size)
    }

    @Test fun completeMissEndsRunWithoutIncrementingScore() {
        val engine = engine(); engine.start(); engine.movingBlock!!.x = 2_000f
        val result = engine.drop()
        assertTrue(result is PlacementResult.Miss); assertEquals(0, engine.score); assertEquals(RunMode.GAME_OVER, engine.mode)
    }

    @Test fun consecutivePerfectPlacementsIncreaseCombo() {
        val engine = engine(); engine.start()
        repeat(2) { engine.movingBlock!!.x = engine.placedBlocks.last().x; engine.drop(); engine.update(0.2f) }
        assertEquals(2, engine.score); assertEquals(2, engine.combo)
    }

    @Test fun duplicateDropDuringCooldownIsIgnored() {
        val engine = engine(); engine.start(); engine.movingBlock!!.x = engine.placedBlocks.last().x; engine.drop()
        engine.movingBlock!!.x = engine.placedBlocks.last().x
        assertNull(engine.drop()); assertEquals(1, engine.score)
        engine.update(0.2f); engine.movingBlock!!.x = engine.placedBlocks.last().x; engine.drop(); assertEquals(2, engine.score)
    }

    @Test fun restartResetsTransientRunState() {
        val engine = engine(); engine.start(); engine.movingBlock!!.x = engine.placedBlocks.last().x; engine.drop(); engine.restart()
        assertEquals(RunMode.RUNNING, engine.mode); assertEquals(0, engine.score); assertEquals(0, engine.combo)
        assertEquals(1, engine.placedBlocks.size); assertFalse(engine.inputLocked)
    }

    @Test fun movementBouncesAtHorizontalBounds() {
        val engine = engine(); engine.start(); val moving = engine.movingBlock!!; moving.x = engine.rightBound - 1f
        engine.update(0.05f)
        assertTrue(moving.x <= engine.rightBound); assertEquals(-1f, engine.movementDirection, 0.001f)
    }

    @Test fun lowFpsFrameMatchesEquivalentSmallPhysicsSteps() {
        val lowFps = engine().also { it.start(); it.movingBlock!!.x = 300f }
        val normal = engine().also { it.start(); it.movingBlock!!.x = 300f }
        lowFps.update(0.15f); repeat(3) { normal.update(0.05f) }
        assertEquals(normal.movingBlock!!.x, lowFps.movingBlock!!.x, 0.001f)
        assertEquals(normal.movementDirection, lowFps.movementDirection, 0.001f)
    }

    @Test fun hugeFrameIsCappedAtRecoveryWindow() {
        val hugeFrame = engine().also { it.start(); it.movingBlock!!.x = 300f }
        val capped = engine().also { it.start(); it.movingBlock!!.x = 300f }
        hugeFrame.update(10f); repeat(4) { capped.update(0.05f) }
        assertEquals(capped.movingBlock!!.x, hugeFrame.movingBlock!!.x, 0.001f)
        assertEquals(capped.movementDirection, hugeFrame.movementDirection, 0.001f)
    }

    @Test fun slowTimeMultiplierReducesMovementWithoutChangingBaseCurve() {
        val normal = engine().also { it.start(); it.movingBlock!!.x = 300f }
        val slow = engine().also { it.start(); it.movingBlock!!.x = 300f }
        normal.update(0.05f)
        slow.update(0.05f, speedMultiplier = 0.65f)
        val normalDistance = normal.movingBlock!!.x - 300f
        val slowDistance = slow.movingBlock!!.x - 300f
        assertEquals(normalDistance * 0.65f, slowDistance, 0.01f)
    }

    @Test fun rescueAfterMissRestartsMovingBlockAndAppliesInputLock() {
        val engine = engine(); engine.start(); engine.movingBlock!!.x = 2_000f; engine.drop()
        assertTrue(engine.rescueAfterMiss())
        assertEquals(RunMode.RUNNING, engine.mode)
        assertTrue(engine.inputLocked)
        assertTrue(engine.movingBlock != null)
        assertFalse(engine.rescueAfterMiss())
    }

    @Test fun widerPerfectToleranceChangesOnlyPerfectClassification() {
        val engine = GameEngine(viewportWidth = 1080f, perfectTolerance = 8f)
        engine.start()
        val base = engine.placedBlocks.last()
        engine.movingBlock!!.x = base.x + 12f
        val normal = engine.drop(perfectToleranceMultiplier = 1f)
        assertTrue(normal is PlacementResult.Success && !normal.perfect)

        engine.restart()
        val base2 = engine.placedBlocks.last()
        engine.movingBlock!!.x = base2.x + 12f
        val wide = engine.drop(perfectToleranceMultiplier = 1.75f)
        assertTrue(wide is PlacementResult.Success && wide.perfect)
    }
}
