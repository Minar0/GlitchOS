package com.whmin.glitchos.providers.musicproviders

import android.app.Activity
import android.content.Intent
import com.whmin.glitchos.AppData

//am I using inheritance correctly? I think this is a good way to implement this
abstract class MusicProvider(val appData: AppData){
    abstract val moduleName: String

    abstract fun playSong(song: String?, artist: String?): Boolean
    abstract fun resume(): Boolean
    abstract fun pause(): Boolean
    abstract fun togglePause(): Boolean
    abstract fun skipForward(): Boolean
    abstract fun skipBack(): Boolean
    abstract fun restartSong(): Boolean
    abstract fun toggleSongRepeat(): Boolean
    abstract fun togglePlaylistRepeat(): Boolean
    abstract fun toggleShuffle(): Boolean
    abstract fun authorize(activity: Activity)
    abstract fun openLoginPage(activity: Activity)
    abstract fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}