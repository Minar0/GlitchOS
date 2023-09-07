package com.whmin.glitchos.intentengine.modules

import android.app.Activity
import android.content.SharedPreferences
import android.util.Log
import com.whmin.glitchos.AppData
import com.whmin.glitchos.intentengine.modules.response.ModuleResponseEngine
import com.whmin.glitchos.providers.musicproviders.*
import java.util.regex.Pattern

//TODO: Allow for searching of both Song and Artist
//TODO: Allow for playing of user playlists. Here's how you do that with the Spotify providerModule https://developer.spotify.com/documentation/web-api/reference/get-users-saved-tracks
class MusicModule(private val appData: AppData): Module(){
    override val moduleName = "Music Module"
    var providerModule : MusicProvider? = null
    private val responder = MusicResponseEngine(appData)

    override val intentRegex: Pattern = Pattern.compile("""play|pause|stop|resume|skip|next|back|repeat|shuffle""")
    private val songArtistRegex = Pattern.compile("""play\s*(.*?)\s*(?:by\s*(.*))?${'$'}""")  //generates a group of the words between play and by. Also generates a group of the words after by. If by doesn't exist it generates a group of everything after play. Damn regex is hard to read
    private val musicSynonymRegex = Pattern.compile("""^(?:song|tune|music|sound)s?${'$'}""")


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
                responder.playSong(commandMap["song"]) //Consider passing this to the providerModule so that it can run the command after pulling the song
            }
            "toggle_pause" -> {//TODO: a saved pause state can't be relied upon. It either must pull the pause state from the API or this command must be split into two: pause and unpause
                providerModule?.togglePause()
                responder.togglePause()
            }
            "skip_forward" -> {
                providerModule?.skipForward()
                responder.skipForward()
            }
            "skip_backward" -> {
                providerModule?.skipBack()
                responder.skipBack()
            }
            "repeat" -> {
                providerModule?.toggleSongRepeat()
                responder.toggleSongRepeat()
            }
            "shuffle" -> {  //TODO: If pause is split into pause and unpause, shuffle should too
                providerModule?.toggleShuffle()
                responder.toggleShuffle()
            }
            else -> {
                responder.speakError()
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


//Handles the responses to the music module
class MusicResponseEngine(var appData: AppData): ModuleResponseEngine(appData) {
    fun playSong(song: String?){
        val commandName = "play_song"

        if(!rememberedResponse(commandName,"rick_roll") && percentageChance(0.1)) {
            super.addToRememberedResponses(commandName,"rick_roll")
            appData.soundOutput.speak("Now playing Never Gonna Give You Up by Rick Astley") //I'm very sorry
        }
        else if (!rememberedResponse(commandName,"hope_check") && percentageChance(10.0)) {
            super.addToRememberedResponses(commandName,"hope_check")
            appData.soundOutput.speak("Playing $song. Hopefully that's the one you asked for")
        }
        else if (!rememberedResponse(commandName,"interesting") && percentageChance(10.0)){
            super.addToRememberedResponses(commandName,"interesting")
            appData.soundOutput.speak("Interesting choice")
        }
        else if(timeSinceLastCommand(commandName)  >= normalTalkCooldown){
            super.addToRememberedResponses(commandName,"normal_response")
            when (randNum(100)){
                in 1..50 -> appData.soundOutput.speak("Playing $song")
                in 51..60 -> appData.soundOutput.speak("On it, playing $song")
                in 61..70 -> appData.soundOutput.speak("$song. Playing now")
                in 71..80 -> appData.soundOutput.speak("$song")
                in 81..90 -> appData.soundOutput.speak("Fetching $song now")
                in 91..100 -> appData.soundOutput.speak("Here's $song")
            }
        }
        else{successBeep(commandName)}
    }

    fun togglePause() {
        val commandName = "toggle_pause"
        if(timeSinceLastCommand(commandName)  >= normalTalkCooldown){
            super.addToRememberedResponses(commandName,"normal_response")
            appData.soundOutput.speak("Toggled the pause state")
        }
        else{successBeep(commandName)}
    }

    fun skipForward() {
        val commandName = "skip_forward"
        if(!rememberedResponse(commandName,"fast_skip_sass") && averageTimeBetweenCommand(commandName) <= 20 && amountOfCommandsInMemory(commandName) >= 3 && percentageChance(50.0)) {
            super.addToRememberedResponses(commandName,"fast_skip_sass")
            appData.soundOutput.speak("Sounds like you're unsatisfied with your music")
        }
        else if (!rememberedResponse(commandName,"faster_skip_sass") && averageTimeBetweenCommand(commandName) <= 10 && amountOfCommandsInMemory(commandName) >= 3 && percentageChance(50.0)) {
            super.addToRememberedResponses(commandName,"faster_skip_sass")
            appData.soundOutput.speak("Are you aware you are supposed to listen to songs and not skip through them?")
        }
        else if (!rememberedResponse(commandName,"fastest_skip_sass") && averageTimeBetweenCommand(commandName) <= 5 && amountOfCommandsInMemory(commandName) >= 3 && percentageChance(50.0)) {
            super.addToRememberedResponses(commandName,"fastest_skip_sass")
            appData.soundOutput.speak("How do you even have time to hear the songs before you skip them?")
        }
        else if (timeSinceLastCommand("play_song") <= 10 && percentageChance(75.0)){
            super.addToRememberedResponses(commandName,"skip_play_song")
            appData.soundOutput.speak("Why ask me to play a song if you're just going to skip it?")
        }
        else if(timeSinceLastCommand(commandName)  >= normalTalkCooldown){
            super.addToRememberedResponses(commandName,"normal_response")
            when (randNum(80)){//we have fun here
                in 1..30 -> appData.soundOutput.speak("Skipping song")
                in 31..60 -> appData.soundOutput.speak("Song skipped")
                in 61..70 -> appData.soundOutput.speak("Playing next song")
                in 71..80 -> appData.soundOutput.speak("Here's the next song")
            }
        }
        else{successBeep(commandName)}
    }

    fun skipBack() { //TODO: Implement a function that track sequential command amount. Sass users that use this too much without turning on repeat
        val commandName = "skip_back"

        if(timeSinceLastCommand(commandName)  >= normalTalkCooldown){
            super.addToRememberedResponses(commandName,"normal_response")
            when (randNum(70)){//we have fun here
                in 1..30 -> appData.soundOutput.speak("Repeating song")
                in 31..60 -> appData.soundOutput.speak("Restarting song")
                in 61..70 -> appData.soundOutput.speak("Restarting song from the beginning")
            }
        }
        else{successBeep(commandName)}
    }

    fun toggleSongRepeat() {
        val commandName = "song_repeat"

        if (!rememberedResponse(commandName, "explanation") && percentageChance(5.0)){
            addToRememberedResponses(commandName,"explanation")
            appData.soundOutput.speak("Song repeat will be turned off if it was on and off if it was on")
        }
        else if(timeSinceLastCommand(commandName) >= normalTalkCooldown){
            addToRememberedResponses(commandName,"normal_response")
            when (randNum(70)){//we have fun here
                in 1..40 -> appData.soundOutput.speak("Repeat song toggled")
                in 41..70 -> appData.soundOutput.speak("Song repeat has been toggled")
            }
        }
        else{successBeep(commandName)}
    }

    fun toggleShuffle() {
        val commandName = "toggle_shuffle"

        if (!rememberedResponse(commandName, "explanation") && percentageChance(5.0)){
            addToRememberedResponses(commandName,"explanation")
            appData.soundOutput.speak("Song shuffle will be turned off if it was on and off if it was on")
        }
        else if(timeSinceLastCommand(commandName)  >= 60){
            addToRememberedResponses(commandName,"normal_response")
            appData.soundOutput.speak("Shuffle has been toggled")
        }
        else{successBeep(commandName)}
    }
}