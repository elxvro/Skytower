package com.elxvro.skytower.platform

import android.media.AudioManager
import android.media.ToneGenerator

class SoundController {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)

    fun menu(enabled: Boolean) = play(enabled, ToneGenerator.TONE_PROP_BEEP, 45)

    fun drop(enabled: Boolean) = play(enabled, ToneGenerator.TONE_PROP_ACK, 35)

    fun placed(enabled: Boolean, perfect: Boolean, combo: Int) {
        val tone = when {
            combo >= 3 -> ToneGenerator.TONE_DTMF_9
            perfect -> ToneGenerator.TONE_DTMF_6
            else -> ToneGenerator.TONE_PROP_BEEP2
        }
        play(enabled, tone, if (perfect) 90 else 55)
    }

    fun gameOver(enabled: Boolean) = play(enabled, ToneGenerator.TONE_PROP_NACK, 180)

    fun release() {
        toneGenerator.release()
    }

    private fun play(enabled: Boolean, tone: Int, durationMs: Int) {
        if (enabled) toneGenerator.startTone(tone, durationMs)
    }
}
