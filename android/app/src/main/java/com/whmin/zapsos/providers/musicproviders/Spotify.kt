package com.whmin.zapsos.providers.musicproviders

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ListItems
import com.spotify.protocol.types.PlayerState
import kotlin.math.log

class Spotify(metadata: Bundle, context: Context) : MusicProvider(metadata,context) {
    override val logTag: String="Spotify"
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
                    Log.d(logTag, "Connection Success")
                }
                override fun onFailure(error: Throwable) {errorCallback }
            }
        )
    }

    override fun playSong(song: String, artist: String): Boolean {
        val searchedItems = mSpotifyAppRemote!!.contentApi.getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
        searchedItems.setResultCallback {
            val firstItem = it.items[0]
            Log.d(logTag, "Found $firstItem")
        }
        return false
    }

    fun disconnect(){
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }

    override fun resume(): Boolean {
        if (!remoteExists()){return true}//Automatically authorizes mSpotifyAppRemote if not authorized. Currently this makes Zapps forget the current command.
        mSpotifyAppRemote!!.playerApi.resume().setResultCallback {
            Log.d(logTag, "Playing")}
            .setErrorCallback {errorCallback}
        return true
    }

    override fun pause(): Boolean {
        if (!remoteExists()){return false}
        mSpotifyAppRemote!!.playerApi.pause().setResultCallback {
            Log.d(logTag, "Paused")}
            .setErrorCallback {errorCallback}
        return true
    }


    override
    fun togglePause(): Boolean {
        if (!remoteExists()){return false}

        mSpotifyAppRemote!!.playerApi.playerState.setResultCallback { playerState: PlayerState ->
            if (playerState.isPaused) {
                resume()
            }
            else {
                pause()
            }
        }
        return true
    }

    override fun skipForward(): Boolean {
        if (!remoteExists()){return false}
        mSpotifyAppRemote!!.playerApi.skipNext().setResultCallback {
            Log.d(logTag, "Skipped forward")}
            .setErrorCallback {errorCallback}
        return true
    }

    override fun skipBack(): Boolean {//This should jump back by a song. Currently acts like restartSong()
        if (!remoteExists()){return false}
        mSpotifyAppRemote!!.playerApi.skipPrevious().setResultCallback {
            Log.d(logTag, "Skipped backwards")
            mSpotifyAppRemote!!.playerApi.skipPrevious().setResultCallback { Log.d(logTag, "Skipped backwards again")}
        }
            .setErrorCallback {errorCallback}
        return true
    }

    override fun restartSong(): Boolean {
        if (!remoteExists()){return false}
        mSpotifyAppRemote!!.playerApi.skipPrevious().setResultCallback {
            Log.d(logTag, "Toggled shuffle")}
            .setErrorCallback {errorCallback}
        return true
    }

    //Used in the repeat toggle steps. If used make sure to run remoteExists beforehand. State 0 is no repeat, State 1 is repeat playlist and State 2 is repeat Song
    private fun setRepeat(repeatState: Int) {
        mSpotifyAppRemote!!.playerApi.setRepeat(repeatState).setResultCallback {
            Log.d(logTag, "Set shuffle to state $repeatState")}
            .setErrorCallback {errorCallback}
    }

    override fun togglePlaylistRepeat(): Boolean {
        if (!remoteExists()){return false}
        mSpotifyAppRemote!!.playerApi.playerState.setResultCallback { playerState: PlayerState ->
            if(playerState.playbackOptions.repeatMode!=2){
                setRepeat(2)
            }
            else{
                setRepeat(0)
            }
        }
        return true
    }

    override fun toggleSongRepeat(): Boolean {
        if (!remoteExists()){return false}
        mSpotifyAppRemote!!.playerApi.playerState.setResultCallback { playerState: PlayerState ->
            if(playerState.playbackOptions.repeatMode!=1){
                setRepeat(1)
            }
            else{
                setRepeat(0)
            }
        }
        return true
    }

    override fun toggleShuffle(): Boolean {
        if (!remoteExists()){return false}
        mSpotifyAppRemote!!.playerApi.toggleShuffle().setResultCallback {
            Log.d(logTag, "Toggled shuffle")}
            .setErrorCallback {errorCallback}
        return true
    }



    private fun remoteExists(): Boolean {
        if (mSpotifyAppRemote==null) {
            Log.e(logTag, "Remote doesn't exist, authenticating")
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