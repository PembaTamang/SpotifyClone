package com.example.spotifyclone.ui

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spotifyclone.Consts.MEDIA_ROOT_ID
import com.example.spotifyclone.MusicServiceConnection
import com.example.spotifyclone.Resource
import com.example.spotifyclone.exoplayer.isPlayEnabled
import com.example.spotifyclone.exoplayer.isPlaying
import com.example.spotifyclone.exoplayer.isPrepared
import com.example.spotifyclone.model.BKMediaTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
):ViewModel() {
    private val _mediaItems = MutableLiveData<Resource<List<BKMediaTrack>>>()
    val mediaItems: LiveData<Resource<List<BKMediaTrack>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currentlyPlayingSong = musicServiceConnection.currentlyPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        _mediaItems.postValue(Resource.loading(null))


        musicServiceConnection.subsScribe(MEDIA_ROOT_ID,object :MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>) {
                super.onChildrenLoaded(parentId, children)
                Log.d("mTag","children are loaded ${children.size}")
                val items = children.map{
                    BKMediaTrack(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })


    }

    fun skipToNextSong(){
        musicServiceConnection.controls.skipToNext()
    }
    fun skipToPreviousSong(){
        musicServiceConnection.controls.skipToPrevious()
    }
    fun seekTo(pos:Long){
        musicServiceConnection.controls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem:BKMediaTrack, toggle:Boolean = false){
        val isPrepared = playbackState.value?.isPrepared ?: false
        if(isPrepared && mediaItem.mediaID == currentlyPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)){
            playbackState.value?.let {playbackState->
                when{
                    playbackState.isPlaying -> if(toggle) musicServiceConnection.controls.pause() //pause song
                    playbackState.isPlayEnabled -> musicServiceConnection.controls.play() //resume song
                    else -> Unit
                }
            }
        }else{
            musicServiceConnection.controls.playFromMediaId(mediaItem.mediaID,null)
        }
    }
    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unSubsScribe(MEDIA_ROOT_ID,object :MediaBrowserCompat.SubscriptionCallback(){

        })
    }
}