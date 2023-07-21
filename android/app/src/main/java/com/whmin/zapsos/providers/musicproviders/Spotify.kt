package com.whmin.zapsos.providers.musicproviders

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.types.Empty
import com.spotify.protocol.types.PlayerState
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.whmin.zapsos.AppData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


class Spotify(appData: AppData): MusicProvider(appData) {
    override val moduleName: String="Spotify"
    private val speechSynthesizer = appData.speechSynthesizer
    var mSpotifyAppRemote: SpotifyAppRemote? = null
    private val clientID = appData.metadata.getString("SPOTIFY_ID")
    private val redirectURI = "comwhminzapsos://callback"
    private val tokenRequestCode = 1337
    private val sharedPrefEditor = appData.sharedPref.edit()


    private val client = OkHttpClient()
    override fun playSong(song: String?, artist: String?): Boolean {
        Log.d(moduleName,"Playing $song by $artist")
        speechSynthesizer.speak("Playing $song")

        val accessToken = appData.sharedPref.getString("spotify_access_token","")
        if (accessToken == null || accessToken == ""){return false}// If no token is found it returns false
        if (!remoteExists()){return false}//Automatically authorizes mSpotifyAppRemote if not authorized. Currently this makes Zapps forget the current command.

        val encodedString = URLEncoder.encode(song, StandardCharsets.UTF_8.toString())
        Log.d(moduleName,"Encoded string: $encodedString")
        val request = Request.Builder()
            .url("https://api.spotify.com/v1/search?q=$encodedString&type=track&limit=2") //TODO: add searching via artist too
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string().toString() // woooooo two string calls in one line? poggers
                val responseJSON = JSONObject(responseBody)
                if (responseJSON.length() > 0) {
                    val trackURI0 = responseJSON.getJSONObject("tracks").getJSONArray("items").getJSONObject(0).getString("uri")
                    Log.d(moduleName,"First track URI: $trackURI0")
                    mSpotifyAppRemote //TODO: If the song that is 1st is currently playing, take the second result
                        ?.playerApi
                        ?.play(trackURI0)
                        ?.setResultCallback(CallResult.ResultCallback<Empty> {Log.d(moduleName, "Playing song")})
                        ?.setErrorCallback(errorCallback) //this section has got more question marks than me while writing this
                }
                else {Log.d(moduleName,"No tracks found in the response.")}
            }
            override fun onFailure(call: Call, e: IOException) {Log.d(moduleName,e.toString())}
        })
        return true
    }
    override fun resume(): Boolean {
        if (!remoteExists()){return false}
        mSpotifyAppRemote!!.playerApi.resume().setResultCallback {
            Log.d(moduleName, "Playing")}
            .setErrorCallback {errorCallback}
        return true
    }
    override fun pause(): Boolean {
        if (!remoteExists()){return false}
        mSpotifyAppRemote!!.playerApi.pause().setResultCallback {
            Log.d(moduleName, "Paused")}
            .setErrorCallback {errorCallback}
        return true
    }
    override fun togglePause(): Boolean {
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
            Log.d(moduleName, "Skipped forward")}
            .setErrorCallback {errorCallback}
        return true
    }
    override fun skipBack(): Boolean {//This should jump back by a song. Currently acts like restartSong()
        if (!remoteExists()){return false}
        mSpotifyAppRemote!!.playerApi.skipPrevious().setResultCallback {
            Log.d(moduleName, "Skipped backwards")
            mSpotifyAppRemote!!.playerApi.skipPrevious().setResultCallback { Log.d(moduleName, "Skipped backwards again")}
        }
            .setErrorCallback {errorCallback}
        return true
    }
    override fun restartSong(): Boolean {
        if (!remoteExists()){return false}
        mSpotifyAppRemote!!.playerApi.skipPrevious().setResultCallback {
            Log.d(moduleName, "Skipped backwards")}
            .setErrorCallback {errorCallback}
        return true
    }

    private fun setRepeat(repeatState: Int) { //Used in the repeat toggle steps. If used make sure to run remoteExists beforehand. State 0 is no repeat, State 1 is repeat playlist and State 2 is repeat Song
        mSpotifyAppRemote!!.playerApi.setRepeat(repeatState).setResultCallback {
            Log.d(moduleName, "Set repeat to state $repeatState")}
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
        speechSynthesizer.speak("Toggling repeat")
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
    override fun toggleShuffle(): Boolean {  //Doesn't seem to toggle the shuffle anymore? TODO: fix
        if (!remoteExists()){return false}
        speechSynthesizer.speak("Toggling shuffle")
        mSpotifyAppRemote!!.playerApi.toggleShuffle().setResultCallback {
            Log.d(moduleName, "Toggled shuffle")}
            .setErrorCallback {errorCallback}
        return true
    }

    override fun authorize(activity: Activity) {
        if (System.currentTimeMillis() >= appData.sharedPref.getLong("spotify_access_token_expiration",0)){
            openLoginPage(activity)
        }
        remoteExists()
    }
    override fun openLoginPage(activity: Activity){ //TODO: Find a way to refresh token without opening the login page
        val builder = AuthorizationRequest.Builder(clientID, AuthorizationResponse.Type.TOKEN, redirectURI)
        builder.setScopes(arrayOf("app-remote-control"))
        val request = builder.build()

        Log.d(moduleName,"Opening Login")
        AuthorizationClient.openLoginActivity(activity, tokenRequestCode, request)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //gets the Spotify access token from a result and stores it in accessToken and shared preferences. Run inside an overridden onActivityResult() inside an activity
        Log.d(moduleName,"Got result: $requestCode $resultCode ${data.toString()}")
        if (requestCode == tokenRequestCode) {
            val response = AuthorizationClient.getResponse(resultCode, data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    val accessToken = response.accessToken
                    val expirationTime = System.currentTimeMillis()+3600000 //Spotify access tokens expire after an hour. Kind of like me
                    Log.d(moduleName,"Got token $accessToken")

                    sharedPrefEditor.putString("spotify_access_token", accessToken)
                    sharedPrefEditor.putLong("spotify_access_token_expiration", expirationTime)
                    sharedPrefEditor.apply()
                    remoteExists()
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.d(moduleName,"Something went wrong: ${response.error}")
                }
                else -> {
                    Log.d(moduleName,"Something's fucky: ${response.type}")
                }
            }
        }
        //region Code could be used in the Activity Result API?
//        if (activityResult.resultCode == Activity.RESULT_OK) {
//            val data: Intent? = activityResult.data
//            // Check if the result comes from the correct activity
//            if (activityResult.data != null && activityResult.data?.getIntExtra("requestCode",-1) == requestCode) {
//                val response = AuthorizationClient.getResponse(activityResult.resultCode, activityResult.data)
//                when (response.type) {
//                    // Response was successful and contains an auth token
//                    AuthorizationResponse.Type.TOKEN -> {
//                        accessToken=response.code
//                        Log.d(logTag,"Got token $accessToken")
//                    }
//                    AuthorizationResponse.Type.ERROR -> {
//                        // Handle error response
//                    }
//                    // Most likely auth flow was canceled
//                    else -> {
//                    }
//                }
//            }
//       }
        //endregion
    }

    private fun connectRemote(){
        val connectionParams = ConnectionParams.Builder(clientID)
            .setRedirectUri(redirectURI)
            .showAuthView(false)
            .build()

        SpotifyAppRemote.connect(
            super.appData.appContext, connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    mSpotifyAppRemote = spotifyAppRemote
                    Log.d(moduleName, "Connection Success")
                }
                override fun onFailure(error: Throwable) {errorCallback }
            }
        )
    }
    private fun remoteExists(): Boolean {
        if (mSpotifyAppRemote==null) {
            Log.e(moduleName, "Remote doesn't exist, creating...")
            connectRemote()
            return false
        }
        return true
    }

    //Woooo! Finally figured out lambda functions
    private val errorCallback: (Throwable) -> Unit = {error ->
        Log.e(moduleName, error.toString())
    }
}