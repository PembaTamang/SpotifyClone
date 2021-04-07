package com.example.spotifyclone.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.util.Log
import androidx.core.net.toUri
import com.example.spotifyclone.remote.MusicDatabase
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.example.spotifyclone.Consts.TAG
import javax.inject.Inject

class FirebaseMusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase
) {

    var songs =  emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() {
        Log.d(TAG, "fetchMediaData: called")
    //set state to initializing
        state = State.STATE_INITIALIZING
        val allSongs = musicDatabase.getAllSongs()

        //mapping song to mediaMetaData
        songs = allSongs.map {
            Builder()
                .putString(METADATA_KEY_ARTIST,it.artist)
                .putString(METADATA_KEY_MEDIA_ID,it.mediaID)
                .putString(METADATA_KEY_TITLE,it.title)
                .putString(METADATA_KEY_DISPLAY_TITLE,it.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI,it.imageUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI,it.imageUrl)
                .putString(METADATA_KEY_MEDIA_URI,it.songUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE,"dummy subtitle")
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION,"dummy description")
                .build()
        }
        state = State.STATE_INITIALIZED
        Log.d(TAG,"songs size ${songs.size}")
    }

    fun asMediaSource(dataSourceFactory:DefaultDataSourceFactory) : ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach{ song->
          val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(song.getString(METADATA_KEY_MEDIA_URI).toUri()))
            concatenatingMediaSource.addMediaSource(audioSource)
       }
        return concatenatingMediaSource
    }

    fun asAndroidMediaItem() = songs.map {
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(it.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(it.description.title)
            .setSubtitle(it.description.subtitle)
            .setMediaId(it.description.mediaId)
            .setIconUri(it.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc,FLAG_PLAYABLE)
    }.toMutableList()

    private val dataStateListener = mutableListOf<(Boolean) -> Unit>()  //enums will be false or true returned as a lambda f(n)


    private var state : State = State.STATE_CREATED // initial state
    set(value) {
        if(value == State.STATE_INITIALIZED || value == State.STATE_ERROR){
            synchronized(dataStateListener){
                field = value
                dataStateListener.forEach { listener->
                    listener(state ==State.STATE_INITIALIZED) // true == init , false == error
                }
            }
        }else{
            field = value
        }
    }

    fun whenReady(action : (Boolean)->Unit) : Boolean{
        return if(state == State.STATE_CREATED || state == State.STATE_INITIALIZING ){
            dataStateListener + action
            false
        }else{
            action(state == State.STATE_INITIALIZED)
            true
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}