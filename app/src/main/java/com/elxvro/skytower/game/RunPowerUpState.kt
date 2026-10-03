package com.elxvro.skytower.game

class RunPowerUpState(
    private val equipped: Set<PowerUpType>,
) {
    var slowTimeRemaining: Float = 0f
        private set
    var widePerfectRemaining: Float = 0f
        private set
    var secondChanceUsed: Boolean = false
        private set

    val coinMultiplierActive: Boolean get() = PowerUpType.COIN_MULTIPLIER in equipped
    val speedMultiplier: Float get() = if (slowTimeRemaining > 0f) SLOW_MULTIPLIER else 1f
    val perfectToleranceMultiplier: Float get() = if (widePerfectRemaining > 0f) WIDE_MULTIPLIER else 1f

    fun activate(type: PowerUpType): Boolean = when (type) {
        PowerUpType.SLOW_TIME -> if (type in equipped) {
            slowTimeRemaining = DURATION_SECONDS
            true
        } else false
        PowerUpType.WIDE_PERFECT -> if (type in equipped) {
            widePerfectRemaining = DURATION_SECONDS
            true
        } else false
        else -> false
    }

    fun tryUseSecondChance(): Boolean {
        if (PowerUpType.SECOND_CHANCE !in equipped || secondChanceUsed) return false
        secondChanceUsed = true
        return true
    }

    fun update(deltaSeconds: Float, running: Boolean) {
        if (!running) return
        val dt = deltaSeconds.coerceAtLeast(0f)
        slowTimeRemaining = (slowTimeRemaining - dt).coerceAtLeast(0f)
        widePerfectRemaining = (widePerfectRemaining - dt).coerceAtLeast(0f)
    }

    companion object {
        const val DURATION_SECONDS = 8f
        const val SLOW_MULTIPLIER = 0.65f
        const val WIDE_MULTIPLIER = 1.75f
    }
}
