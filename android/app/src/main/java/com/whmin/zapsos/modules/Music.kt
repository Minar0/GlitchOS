package com.whmin.zapsos.modules

import android.content.SharedPreferences
import android.os.Bundle
import com.spotify.android.appremote.api.ConnectionParams


class Music(var metadata: Bundle, private val sharedPref: SharedPreferences){
    var providerModule : MusicProvider? = null

    //any new music providers will need to be added here
    fun setupProvider(){
        providerModule = when (sharedPref.getString("preferred_music_service","")){
            "spotify" -> Spotify()
            else -> null
        }
    }
}


//am I using inheritance correctly? I think this is a good way to implement this
open class MusicProvider(){
    fun play(){

    }
    fun stop(){

    }
    fun pause(){

    }
    fun skip(){

    }
    fun shuffle(){

    }

    //no default behavior, every new provider will have to override this
    open fun authorize(metadata: Bundle) {
    }
}

class Spotify() : MusicProvider() {
    override
    fun authorize(metadata: Bundle){
        val connectionParams = ConnectionParams.Builder(metadata.getString("SPOTIFY_ID"))
            .setRedirectUri("https://github.com/Minar0/ZapsOS")
            .showAuthView(true)
            .build()
    }
}