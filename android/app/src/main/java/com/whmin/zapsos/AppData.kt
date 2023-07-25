package com.whmin.zapsos

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.whmin.zapsos.speech.SoundOutputEngine

//This stores a variety of useful stuff app information that I can pass to ZapsOS's various bits and bobs
//the urge to make this a singleton is rising
data class AppData(var metadata: Bundle, var sharedPref: SharedPreferences, var appContext: Context, var soundOutput: SoundOutputEngine)