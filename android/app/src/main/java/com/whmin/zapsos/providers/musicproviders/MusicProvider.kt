package com.whmin.zapsos.providers.musicproviders

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.activity.result.ActivityResult

//am I using inheritance correctly? I think this is a good way to implement this
abstract class MusicProvider(val metadata: Bundle, val context: Context){
    abstract val logTag: String

    abstract fun authorize(activity: Activity)
    abstract fun getActivityResult(activityResult: ActivityResult)
    abstract fun playSong(song: String, artist: String): Boolean
    abstract fun resume(): Boolean
    abstract fun pause(): Boolean
    abstract fun togglePause(): Boolean
    abstract fun skipForward(): Boolean
    abstract fun skipBack(): Boolean
    abstract fun restartSong(): Boolean
    abstract fun toggleSongRepeat(): Boolean
    abstract fun togglePlaylistRepeat(): Boolean
    abstract fun toggleShuffle(): Boolean
}