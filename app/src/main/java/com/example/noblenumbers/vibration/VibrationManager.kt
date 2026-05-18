package com.example.noblenumbers.vibration

import android.content.Context
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrationManager(
    context: Context,
) {
    private val vibrator: Vibrator? = context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    private var lastLightHapticAtMillis: Long = 0L

    fun vibrate(event: VibrationEvent, enabled: Boolean) {
        if (!enabled || vibrator?.hasVibrator() != true) return
        if (event.isLightHaptic() && isLightHapticThrottled()) return

        val effect = when (event) {
            VibrationEvent.ButtonClick -> VibrationEffect.createOneShot(12L, 48)
            VibrationEvent.Swipe -> VibrationEffect.createOneShot(16L, 54)
            VibrationEvent.Merge -> VibrationEffect.createOneShot(24L, 82)
            VibrationEvent.Combo -> VibrationEffect.createWaveform(longArrayOf(0, 30, 20, 30), intArrayOf(80, 120, 0, 100), -1)
            VibrationEvent.TargetReached -> VibrationEffect.createOneShot(80L, VibrationEffect.DEFAULT_AMPLITUDE)
            VibrationEvent.FrozenModeStarted -> VibrationEffect.createOneShot(70L, 170)
            VibrationEvent.FrozenTileAppeared -> VibrationEffect.createOneShot(45L, 120)
            VibrationEvent.GameOver -> VibrationEffect.createOneShot(110L, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrator.vibrate(effect)
    }

    private fun isLightHapticThrottled(): Boolean {
        val now = SystemClock.elapsedRealtime()
        val throttled = now - lastLightHapticAtMillis < LIGHT_HAPTIC_INTERVAL_MILLIS
        if (!throttled) lastLightHapticAtMillis = now
        return throttled
    }

    private fun VibrationEvent.isLightHaptic(): Boolean = when (this) {
        VibrationEvent.ButtonClick,
        VibrationEvent.Swipe -> true
        VibrationEvent.Merge,
        VibrationEvent.Combo,
        VibrationEvent.TargetReached,
        VibrationEvent.FrozenModeStarted,
        VibrationEvent.FrozenTileAppeared,
        VibrationEvent.GameOver -> false
    }

    private companion object {
        const val LIGHT_HAPTIC_INTERVAL_MILLIS = 45L
    }
}

enum class VibrationEvent {
    ButtonClick,
    Swipe,
    Merge,
    Combo,
    TargetReached,
    FrozenModeStarted,
    FrozenTileAppeared,
    GameOver,
}
