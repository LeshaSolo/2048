package com.example.noblenumbers.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.noblenumbers.R

class SoundManager(
    private val context: Context,
) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val buttonClickSoundId = soundPool.load(context, R.raw.sfx_button_click, 1)
    private val tileSwipeSoundId = soundPool.load(context, R.raw.sfx_tile_swipe, 1)

    fun play(event: SoundEvent, enabled: Boolean) {
        if (!enabled) return
        val soundId = when (event) {
            SoundEvent.ButtonClick -> buttonClickSoundId
            SoundEvent.Swipe,
            SoundEvent.Merge -> tileSwipeSoundId
            SoundEvent.TargetReached,
            SoundEvent.FrozenMode,
            SoundEvent.FrozenTile,
            SoundEvent.GameOver -> return
        }
        soundPool.play(soundId, DEFAULT_VOLUME, DEFAULT_VOLUME, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }

    private companion object {
        const val MAX_STREAMS = 4
        const val DEFAULT_VOLUME = 0.72f
    }
}

enum class SoundEvent {
    Swipe,
    Merge,
    TargetReached,
    FrozenMode,
    FrozenTile,
    GameOver,
    ButtonClick,
}
