package com.whmin.zapsos.modules

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.whmin.zapsos.SettingsActivity

//Class stores and controls the various modules. Hopefully
class ModuleManager(private val metadata: Bundle, private val sharedPref: SharedPreferences, private val context: Context) {
    val music = Music(metadata,sharedPref,context)
}