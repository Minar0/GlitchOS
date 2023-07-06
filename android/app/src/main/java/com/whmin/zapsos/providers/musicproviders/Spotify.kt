package com.whmin.zapsos.providers.musicproviders

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import com.spotify.sdk.android.auth.AccountsQueryParameters.CLIENT_ID
import com.spotify.sdk.android.auth.AccountsQueryParameters.REDIRECT_URI
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse


class Spotify(metadata: Bundle, context: Context) : MusicProvider(metadata,context) {
    override val logTag: String="Spotify"
    var mSpotifyAppRemote: SpotifyAppRemote? = null
    private val clientID = super.metadata.getString("SPOTIFY_ID")
    private val redirectURI = "comwhminzapsos://callback"
    private var accessToken: String = ""
    private val requestCode = 1337

    override fun authorize(activity: Activity) {
        val builder = AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
        builder.setScopes(arrayOf("app-remote-control"))
        val request = builder.build()

        AuthorizationClient.openLoginActivity(activity, requestCode, request)
    }

    override fun getActivityResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val data: Intent? = activityResult.data
            // Check if the result comes from the correct activity
            if (activityResult.data != null && activityResult.data?.getIntExtra("requestCode",-1) == requestCode) {
                val response = AuthorizationClient.getResponse(activityResult.resultCode, activityResult.data)
                when (response.getType()) {
                    // Response was successful and contains an auth token
                    AuthorizationResponse.Type.TOKEN -> {
                        accessToken=response.code
                        Log.d(logTag,"Got token $accessToken")
                    }
                    AuthorizationResponse.Type.ERROR -> {
                        // Handle error response
                    }
                    // Most likely auth flow was canceled
                    else -> {
                    }
                }
            }
        }
    }

    fun disconnect(){
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)
    }

    fun setAccessToken(token: String){
        accessToken=token
    }

    private fun connectRemote(){
        val connectionParams = ConnectionParams.Builder(clientID)
            .setRedirectUri(redirectURI)
            .showAuthView(false)
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
        if (!remoteExists()){return false}//Automatically authorizes mSpotifyAppRemote if not authorized. Currently this makes Zapps forget the current command.
        val searchedItems = mSpotifyAppRemote!!.contentApi.getRecommendedContentItems(ContentApi.ContentType.DEFAULT)
        searchedItems.setResultCallback {
            val firstItem = it.items[0]
            Log.d(logTag, "Found $firstItem")
        }
        return true
    }

    override fun resume(): Boolean {
        if (!remoteExists()){return false}
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
            connectRemote()
            return false
        }
        return true
    }

    fun getAccessToken(): String{

        return ""
    }

    //Woooo! Finally figured out lambda functions
    private val errorCallback: (Throwable) -> Unit = {
        Log.e("Spotify", it.toString())
    }
}