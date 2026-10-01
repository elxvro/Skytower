package com.elxvro.skytower.game

object DifficultyCurve {
    private const val MIN_SPEED = 220f
    private const val MAX_SPEED = 560f
    private const val SPEED_PER_POINT = 7f

    fun speedForScore(score: Int): Float {
        val safeScore = score.coerceAtLeast(0)
        return (MIN_SPEED + safeScore * SPEED_PER_POINT).coerceAtMost(MAX_SPEED)
    }
}
