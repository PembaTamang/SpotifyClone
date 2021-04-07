package com.example.spotifyclone.remote

import com.example.spotifyclone.model.BKMediaTrack

class MusicDatabase {
    private val url1 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
    private val url2 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
    private val url3 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
    private val url4 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
    private val url5 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
    private val url6 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3"
    private val artist =
        "https://www.freepnglogos.com/uploads/apple-music-logo-circle-png-28.png"

    suspend fun getAllSongs(): List<BKMediaTrack> {
        return listOf(
            BKMediaTrack("0", "Title One", "Artist One", url1, artist),
            BKMediaTrack("1", "Title Two", "Artist Two", url2, artist),
            BKMediaTrack("2", "Title Three", "Artist Three", url3, artist,),
            BKMediaTrack("3", "Title Four", "Artist Four", url4, artist),
            BKMediaTrack("4", "Title Five", "Artist Five", url5, artist),
            BKMediaTrack("5", "Title Six", "Artist Six", url6, artist),
        )
    }
}