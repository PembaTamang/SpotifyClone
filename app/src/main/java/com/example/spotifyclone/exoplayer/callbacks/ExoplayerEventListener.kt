package com.example.spotifyclone.exoplayer.callbacks

import android.widget.Toast
import com.example.spotifyclone.exoplayer.MusicService
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM

class ExoplayerEventListener(
    private val musicService: MusicService
): Player.EventListener {
    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        if(reason == Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST || reason == PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM  ){
            musicService.stopForeground(false)
        }

    }

    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService, "Error occurred", Toast.LENGTH_SHORT).show()
    }
}