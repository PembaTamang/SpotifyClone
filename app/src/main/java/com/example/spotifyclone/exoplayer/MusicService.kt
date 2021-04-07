package com.example.spotifyclone.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import com.example.spotifyclone.Consts.MEDIA_ROOT_ID
import com.example.spotifyclone.Consts.NETWORK_ERROR
import com.example.spotifyclone.Consts.TAG
import com.example.spotifyclone.exoplayer.callbacks.ExoplayerEventListener
import com.example.spotifyclone.exoplayer.callbacks.MusicNotificationListener
import com.example.spotifyclone.exoplayer.callbacks.MusicPlaybackPreparer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_TAG = "musicService"

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {
    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var firebaseMusicSource : FirebaseMusicSource

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob) // merging jobs

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false

    private lateinit var musicNotificationManager: MusicNotificationManager


    private var currentPlayingSong : MediaMetadataCompat? = null

    private lateinit var musicPlaybackPreparer : MusicPlaybackPreparer

    private var isPlayerInitialized = false

    private lateinit var exoplayerEventListener: ExoplayerEventListener


    companion object{
        var currentSongDuration = 0L
            private set
    }
    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {

            firebaseMusicSource.fetchMediaData()
        }
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        } // intent moves to activity

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        } // media session for android activity
        sessionToken = mediaSession.sessionToken


        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicNotificationListener(this)
        ) {
        currentSongDuration = exoPlayer.duration
        }

        musicPlaybackPreparer= MusicPlaybackPreparer(firebaseMusicSource){
            currentPlayingSong=it
            preparePlayer(firebaseMusicSource.songs,it,true)

        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)
        exoplayerEventListener = ExoplayerEventListener(this)
        exoPlayer.addListener(exoplayerEventListener)
        musicNotificationManager.showNotification(exoPlayer)
    }

    private fun preparePlayer(
        songs : List<MediaMetadataCompat>,
        songToPlay : MediaMetadataCompat?,
        playNow :Boolean
    ){
        val currentSongIndex = if(currentPlayingSong == null) 0 else songs.indexOf(songToPlay)

        exoPlayer.addMediaSource(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(currentSongIndex,0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.removeListener(exoplayerEventListener)
        exoPlayer.release()
    }
    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession){
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            Log.d(TAG, "firebase music source size ${firebaseMusicSource.songs.size} window index $windowIndex")
          return firebaseMusicSource.songs[windowIndex].description
        }

    }
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
       return  BrowserRoot(MEDIA_ROOT_ID,null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Log.d(TAG, "onLoadChildren: music service parentID $parentId" )
        when(parentId){
            MEDIA_ROOT_ID ->{
                val res = firebaseMusicSource.whenReady {
                    isInitialized ->
                    Log.d(TAG, "init is called" )
                    if(isInitialized){
                        result.sendResult(firebaseMusicSource.asAndroidMediaItem())
                        if(!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()){
                            preparePlayer(firebaseMusicSource.songs,firebaseMusicSource.songs[0],false)
                            isPlayerInitialized = true
                        }
                    }else{
                        mediaSession.sendSessionEvent(NETWORK_ERROR,null)
                        result.sendResult(null)
                    }
                }


                if(!res){
                    result.detach()
                }
            }
        }
    }

}