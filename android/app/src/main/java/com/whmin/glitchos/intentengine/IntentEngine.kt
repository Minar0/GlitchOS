package com.whmin.glitchos.intentengine

import android.util.Log
import com.whmin.glitchos.AppData
import com.whmin.glitchos.intentengine.modules.*
import com.whmin.glitchos.intentengine.modules.MusicModule
import java.util.regex.Pattern


class IntentEngine(private val appData: AppData) {
    val moduleName="Intent Engine"
    private lateinit var currentModule: Module
    var music: MusicModule = MusicModule(appData)
    private val calendar = CalendarModule()
    private var moduleList: Array<Module> = arrayOf(PresentationModule(appData),music , calendar) //Higher priority intents should be closer to the top


    //used for preprocessing
    private val glitchOSNicknames= "glitch"
    private val preprocessPattern: Pattern = Pattern.compile(glitchOSNicknames)


    fun detectAndRunCommand(input: String): Boolean{
        val preprocessedInput = preprocessInput(input)
        if (preprocessedInput == null || preprocessedInput == ""){ //TODO: add a way for Glitch to explain why a command failed
            Log.e(moduleName,"Input string cleared by preprocessor")
            appData.soundOutput.speak("Yes?")
            return false
        }
        Log.d(moduleName,"Input preprocessed: $preprocessedInput")

        val intendedModule = classifyIntent(preprocessedInput)
        if (intendedModule==null) {
            Log.e(moduleName,"No intended module detected from $preprocessedInput")
            appData.soundOutput.speak("I'm not sure what you want me to do.")
            return false
        }
        Log.d(moduleName,"Intent found: ${intendedModule.moduleName}")

        val ranCommand = intendedModule.runCommand(preprocessedInput)
        if (!ranCommand) {
            Log.e(moduleName,"Command failed to run")
            return false
        }

        Log.d(moduleName,"Running command")
        return true
    }

    private fun preprocessInput(toBeProcessed: String): String?{
        val matcher = preprocessPattern.matcher(toBeProcessed)
        return matcher.replaceAll("")
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