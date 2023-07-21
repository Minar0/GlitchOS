package com.whmin.zapsos.intentengine.modules

import android.app.Activity
import android.content.SharedPreferences
import android.util.Log
import com.whmin.zapsos.AppData
import com.whmin.zapsos.intentengine.modules.response.ModuleResponseEngine
import com.whmin.zapsos.providers.musicproviders.*
import java.util.regex.Pattern


class MusicModule(private val appData: AppData): Module(){
    override val moduleName = "Music Module"
    var providerModule : MusicProvider? = null

    override val intentRegex: Pattern = Pattern.compile("""play|pause|stop|resume|skip|next|back|repeat|shuffle""")
    private val songArtistRegex = Pattern.compile("""play\s*(.*?)\s*(?:by\s*(.*))?${'$'}""")  //generates a group of the words between play and by. Also generates a group of the words after by. If by doesn't exist it generates a group of everything after play. Damn regex is hard to read
    private val musicSynonymRegex = Pattern.compile("""^(?:song|tune|music|sound)s?${'$'}""")

    private val responder = MusicResponseEngine(appData)


    //region CommandHash generation and handling code
    override fun getFullCommandHash(input: String): HashMap<String, String> {
        val returnHashMap: HashMap<String,String>  = HashMap()
        val mainCommandMatcher = intentRegex.matcher(input)
        mainCommandMatcher.find()

        Log.d(moduleName,"Command:$input   Detected:${mainCommandMatcher.group()}")

        when (mainCommandMatcher.group()) {
            "play" -> returnHashMap.putAll(generatePlayCommandHash(input))
            "pause", "stop", "resume" -> returnHashMap["command"]="toggle_pause"
            "skip", "next" -> returnHashMap["command"]="skip_forward"
            "back" -> returnHashMap["command"]="skip_backward"
            "repeat" -> returnHashMap["command"]="repeat"
            "shuffle" -> returnHashMap["command"]="shuffle"
            else -> {
                Log.e(moduleName,"Command not found despite music being triggered")
                returnHashMap["command"]="error"
            }
        }
        return returnHashMap
    }
    override fun runCommandFromHash(commandMap: HashMap<String, String>): Boolean {
        when (commandMap["command"]){
            "play" -> {
                providerModule?.playSong(commandMap["song"], commandMap["artist"])
                responder.playSong()
            }
            "toggle_pause" -> providerModule?.togglePause()
            "skip_forward" -> providerModule?.skipForward()
            "skip_backward" -> providerModule?.skipBack()
            "repeat" -> providerModule?.toggleSongRepeat()
            "shuffle" -> providerModule?.toggleShuffle()
            else -> {
                Log.e(moduleName,"CommandHash not recognized")
            }
        }
        return true
    }
    private fun generatePlayCommandHash(input: String): HashMap<String,String>{ //Function will determine if the user wants to unpause the music or play a specific song. If a specific song is wanted, it will find the song name and artist (if included)
        val songArtistMatcher = songArtistRegex.matcher(input)
        songArtistMatcher.find()

        var song = songArtistMatcher.group(1)
        if (song == null || musicSynonymRegex.matcher(song).matches()) {song = ""}
        var artist = songArtistMatcher.group(2)
        if (artist == null) {artist = ""}

        val returnHashMap: HashMap<String,String>  = HashMap()
        if(song=="" && artist == ""){
            returnHashMap["command"]="toggle_pause"
        } else{
            returnHashMap["command"] = "play"
            returnHashMap["song"] = song
            returnHashMap["artist"] = artist
        }
        return returnHashMap
    }
    //endregion

    //region Code that handles the musicProvider setup
    fun setupProvider(activity: Activity){ //runs on startup and when user changes preferred provider
        providerModule = when (appData.sharedPref.getString("preferred_music_service","")){//any new music providers will need to be added to this case statement
            "spotify" -> Spotify(appData)
            else -> null
        }
        providerModule?.authorize(activity) // Should setupProvider also run authorize? I think it's efficient but not very readable
    }

    //Code that allows an activity to change the music provider. Only used in the Settings activity
    private lateinit var providerListener: SharedPreferences.OnSharedPreferenceChangeListener
    fun setupProviderListener(activity: Activity){
        providerListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "preferred_music_service") {
                Log.d(moduleName,"Changed provider")
                setupProvider(activity)
            }
        }
        appData.sharedPref.registerOnSharedPreferenceChangeListener(providerListener)
        Log.d(moduleName,"Listener registered")
    }
    fun cleanupProviderListener(){
        if (::providerListener.isInitialized) {
            appData.sharedPref.unregisterOnSharedPreferenceChangeListener(providerListener)
            Log.d(moduleName,"Listener unregistered")
        } else {
            Log.d(moduleName,"Listener is not registered")
        }
    }
    //endregion
}

class MusicResponseEngine(var appData: AppData): ModuleResponseEngine(appData) {
    var speechSynthesis = appData.speechSynthesizer

    fun playSong(): Boolean{
        //super.addToRememberedResponses("play_song")

        return true
    }
}