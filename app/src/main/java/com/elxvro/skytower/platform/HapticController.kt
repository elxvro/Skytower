package com.elxvro.skytower.platform

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

class HapticController(context: Context) {
    private val vibrator: Vibrator? = context.getSystemService(Vibrator::class.java)

    fun placed(enabled: Boolean, perfect: Boolean) {
        val target = vibrator ?: return
        if (!enabled || !target.hasVibrator()) return
        val duration = if (perfect) 38L else 20L
        val amplitude = if (perfect) 125 else 75
        target.vibrate(VibrationEffect.createOneShot(duration, amplitude))
    }

    fun gameOver(enabled: Boolean) {
        val target = vibrator ?: return
        if (!enabled || !target.hasVibrator()) return
        target.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0L, 45L, 45L, 90L),
                intArrayOf(0, 100, 0, 180),
                -1,
            ),
        )
    }
}
