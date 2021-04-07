package com.example.spotifyclone

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.spotifyclone.Consts.NETWORK_ERROR
import com.example.spotifyclone.Consts.TAG
import com.example.spotifyclone.exoplayer.MusicService

class MusicServiceConnection(context: Context) {

    //setting mutable live data as private and exposing live data to other classes

    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    private val _currentlyPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val currentlyPlayingSong: LiveData<MediaMetadataCompat?> = _currentlyPlayingSong

    lateinit var mediaController: MediaControllerCompat

    val controls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls




    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowserCompat = MediaBrowserCompat(
        context, ComponentName(context,MusicService::class.java),mediaBrowserConnectionCallback,null
    ).apply {
        connect() //trigger onConnected
    }



    fun subsScribe(parentID:String, callback:MediaBrowserCompat.SubscriptionCallback){
        mediaBrowserCompat.subscribe(parentID,callback)
        Log.d(TAG, "subsScribe: parentid $parentID ")
    }
    fun unSubsScribe(parentID:String, callback:MediaBrowserCompat.SubscriptionCallback){
        mediaBrowserCompat.unsubscribe(parentID,callback)
    }


    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            Log.d("mTag", "onConnected:from callback ")
            mediaController = MediaControllerCompat(context,mediaBrowserCompat.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isConnected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            Log.d(TAG, "onConnectionSuspended: ")
           _isConnected.postValue(Event(Resource.error("The connection was suspended",false)))
        }

        override fun onConnectionFailed() {
            Log.d(TAG, "onConnectionFailed: ")
            _isConnected.postValue(Event(Resource.error("Couldn't create media browser",false)))
        }
    }




    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            _currentlyPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when (event) {
                NETWORK_ERROR -> {
                    _networkError.postValue(Event(Resource.error("Connection error", null)))
                }
            }
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}