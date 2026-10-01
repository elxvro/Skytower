package com.elxvro.skytower.game

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object PlacementCalculator {
    fun calculate(
        moving: Block,
        base: Block,
        perfectTolerance: Float,
    ): PlacementResult {
        if (abs(moving.x - base.x) <= perfectTolerance) {
            return PlacementResult.Success(
                placedBlock = moving.copy(x = base.x),
                cutFragment = null,
                perfect = true,
            )
        }

        val overlapLeft = max(moving.x, base.x)
        val overlapRight = min(moving.x + moving.width, base.x + base.width)
        val overlapWidth = overlapRight - overlapLeft
        if (overlapWidth <= 0f) return PlacementResult.Miss

        val placed = moving.copy(x = overlapLeft, width = overlapWidth)
        val movingRight = moving.x + moving.width
        val fragment = when {
            moving.x < overlapLeft -> moving.copy(width = overlapLeft - moving.x)
            movingRight > overlapRight -> moving.copy(x = overlapRight, width = movingRight - overlapRight)
            else -> null
        }

        return PlacementResult.Success(
            placedBlock = placed,
            cutFragment = fragment?.takeIf { it.width > 0f },
            perfect = false,
        )
    }
}
