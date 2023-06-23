package com.whmin.zapsos.modules

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState


const val LOG_TAG="Music"

class Music(private val metadata: Bundle, private val sharedPref: SharedPreferences, private val context: Context){
    var providerModule : MusicProvider? = null

    //any new music providers will need to be added here
    fun setupProvider(){
        providerModule = when (sharedPref.getString("preferred_music_service","")){
            "spotify" -> Spotify(metadata, context)
            else -> null
        }
        providerModule?.authorize()
    }
}


//am I using inheritance correctly? I think this is a good way to implement this
abstract class MusicProvider(val metadata: Bundle, val context: Context){
    abstract fun authorize()
    abstract fun resume(): Int
    abstract fun pause(): Int
    abstract fun togglePause(): Int
    abstract fun skipForward(): Int
    abstract fun skipBack(): Int
    abstract fun restartSong(): Int
    abstract fun toggleSongRepeat(): Int
    abstract fun togglePlaylistRepeat(): Int
    abstract fun toggleShuffle(): Int
}

class Spotify(metadata: Bundle, context: Context) : MusicProvider(metadata,context) {
    var mSpotifyAppRemote: SpotifyAppRemote? = null

    override
    fun authorize(){
        val connectionParams = ConnectionParams.Builder(super.metadata.getString("SPOTIFY_ID"))
            .setRedirectUri("comwhminzapsos://callback")
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(
            super.context, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    Log.d(LOG_TAG, "Connection Success")
                }
                override fun onFailure(error: Throwable) {errorCallback }
            }
        )
    }

    fun disconnect(){
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }

    override fun resume(): Int {
        if (!remoteExists()){return 0}//Automatically authorizes mSpotifyAppRemote if not authorized. Currently this makes Zapps forget the current command.
        mSpotifyAppRemote!!.playerApi.resume().setResultCallback {
            Log.d(LOG_TAG, "Playing")}
            .setErrorCallback {errorCallback}
        return 1
    }

    override fun pause(): Int {
        if (!remoteExists()){return 0}
        mSpotifyAppRemote!!.playerApi.pause().setResultCallback {
            Log.d(LOG_TAG, "Paused")}
            .setErrorCallback {errorCallback}
        return 1
    }


    override
    fun togglePause(): Int {
        if (!remoteExists()){return 0}

        mSpotifyAppRemote!!.playerApi.playerState.setResultCallback { playerState: PlayerState ->
            if (playerState.isPaused) {
                resume()
            }
            else {
                pause()
            }
        }
        return 1
    }

    override fun skipForward(): Int {
        if (!remoteExists()){return 0}
        mSpotifyAppRemote!!.playerApi.skipNext().setResultCallback {
            Log.d(LOG_TAG, "Skipped forward")}
            .setErrorCallback {errorCallback}
        return 1
    }

    override fun skipBack(): Int {//This should jump back by a song. Currently acts like restartSong()
        if (!remoteExists()){return 0}
        mSpotifyAppRemote!!.playerApi.skipPrevious().setResultCallback {
            Log.d(LOG_TAG, "Skipped backwards")
            mSpotifyAppRemote!!.playerApi.skipPrevious().setResultCallback {Log.d(LOG_TAG, "Skipped backwards again")}
        }
            .setErrorCallback {errorCallback}
        return 1
    }

    override fun restartSong(): Int {
        if (!remoteExists()){return 0}
        mSpotifyAppRemote!!.playerApi.skipPrevious().setResultCallback {
            Log.d(LOG_TAG, "Toggled shuffle")}
            .setErrorCallback {errorCallback}
        return 1
    }

    //Used in the repeat toggle steps. If used make sure to run remoteExists beforehand. State 0 is no repeat, State 1 is repeat playlist and State 2 is repeat Song
    private fun setRepeat(repeatState: Int) {
        mSpotifyAppRemote!!.playerApi.setRepeat(repeatState).setResultCallback {
            Log.d(LOG_TAG, "Set shuffle to state $repeatState")}
            .setErrorCallback {errorCallback}
    }

    override fun togglePlaylistRepeat(): Int {
        if (!remoteExists()){return 0}
        mSpotifyAppRemote!!.playerApi.playerState.setResultCallback { playerState: PlayerState ->
            if(playerState.playbackOptions.repeatMode!=2){
                setRepeat(2)
            }
            else{
                setRepeat(0)
            }
        }
        return 1
    }

    override fun toggleSongRepeat(): Int {
        if (!remoteExists()){return 0}
        mSpotifyAppRemote!!.playerApi.playerState.setResultCallback { playerState: PlayerState ->
            if(playerState.playbackOptions.repeatMode!=1){
                setRepeat(1)
            }
            else{
                setRepeat(0)
            }
        }
        return 1
    }

    override fun toggleShuffle(): Int {
        if (!remoteExists()){return 0}
        mSpotifyAppRemote!!.playerApi.toggleShuffle().setResultCallback {
            Log.d(LOG_TAG, "Toggled shuffle")}
            .setErrorCallback {errorCallback}
        return 1
    }



    private fun remoteExists(): Boolean {
        if (mSpotifyAppRemote==null) {
            Log.e(LOG_TAG, "Remote doesn't exist, authenticating")
            authorize()
            return false
        }
        return true
    }

    //Woooo! Finally figured out lambda functions
    private val errorCallback: (Throwable) -> Unit = {
        Log.d("Spotify", it.toString())
    }
}
