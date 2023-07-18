package com.whmin.zapsos.intentengine

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.whmin.zapsos.AppData
import com.whmin.zapsos.intentengine.modules.*
import java.util.Locale
import java.util.regex.Pattern

//Yes, this is a singleton. Am I wrong to do this? Maybe? I'm a perpetual hobbyist so if a professional ha  s some opinions on the matter let me know
//I've recently created a Service to handle a lot of ZapsOS stuff. Maybe I can de-singleton this guy now? Something to consider later. TODO: Consider desinglization
object IntentEngine {
    var isInit = false
    lateinit var appData: AppData
    lateinit var appContext: Context

    lateinit var currentModule: Module
    lateinit var music: MusicModule
    val calendar = CalendarModule()
    lateinit var moduleList: Array<Module>

    fun initialize(metadata: Bundle, sharedPref: SharedPreferences, ct: Context){
        isInit = true
        appData = AppData(metadata,sharedPref)
        appContext = ct

        music = MusicModule(appData)
        moduleList = arrayOf(music, calendar)//Higher priority intents should be closer to the top
    }

    val moduleName="Intent Engine"

    //used for preprocessing
    private val zapsOSNicknames= "za(p|pp)sos|za(p|pp)s|za(p|pp)y"
    private val preprocessPattern: Pattern = Pattern.compile(zapsOSNicknames)


    fun detectAndRunCommand(input: String): String{
        val preprocessedInput = preprocessInput(input)
        if (preprocessedInput == null || preprocessedInput == ""){ //TODO: add a way for Zaps to explain why a command failed
            Log.e(moduleName,"Input string cleared by preprocessor")
            return "Yes?"
        }
        Log.d(moduleName,"Input preprocessed: $preprocessedInput")

        val intendedModule = classifyIntent(preprocessedInput)
        if (intendedModule==null) {
            Log.e(moduleName,"No intended module detected from $preprocessedInput")
            return "Command not detected"
        }
        Log.d(moduleName,"Intent found: ${intendedModule.moduleName}")

        val ranCommand = intendedModule.runCommand(preprocessedInput)
        if (!ranCommand) {
            Log.e(moduleName,"Command failed to run")
            return "Something went wrong while running your request"
        }

        Log.d(moduleName,"Running command")
        return "Running Command"
    }

    private fun preprocessInput(toBeProcessed: String): String?{
        val matcher = preprocessPattern.matcher(toBeProcessed)
        return matcher.replaceAll("").lowercase()
    }

    private fun classifyIntent(input: String): Module? {
        for (module in moduleList){
            val isIntent = module.detectIntent(input)
            if (isIntent){
                currentModule=module
                return currentModule
            }
        }
        return null
    }
}