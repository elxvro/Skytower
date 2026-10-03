package com.elxvro.skytower.game

object DifficultyCurve {
    private const val MIN_SPEED = 205f
    private const val MAX_SPEED = 500f
    private const val EARLY_END_SCORE = 12
    private const val MID_END_SCORE = 35
    private const val EARLY_SPEED_PER_POINT = 4.5f
    private const val MID_SPEED_PER_POINT = 6f
    private const val LATE_SPEED_PER_POINT = 3.5f

    fun speedForScore(score: Int): Float {
        val safeScore = score.coerceAtLeast(0)
        val earlyPoints = minOf(safeScore, EARLY_END_SCORE)
        var speed = MIN_SPEED + earlyPoints * EARLY_SPEED_PER_POINT

        if (safeScore > EARLY_END_SCORE) {
            val midPoints = minOf(safeScore, MID_END_SCORE) - EARLY_END_SCORE
            speed += midPoints * MID_SPEED_PER_POINT
        }

        if (safeScore > MID_END_SCORE) {
            speed += (safeScore - MID_END_SCORE) * LATE_SPEED_PER_POINT
        }

        return speed.coerceAtMost(MAX_SPEED)
    }
}
