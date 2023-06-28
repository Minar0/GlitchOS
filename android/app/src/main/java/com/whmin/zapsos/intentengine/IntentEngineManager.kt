package com.whmin.zapsos.intentengine

import android.util.Log
import com.whmin.zapsos.AppData
import com.whmin.zapsos.intentengine.modules.*
import java.util.regex.Pattern
import kotlin.math.log

class IntentEngineManager(appData: AppData) {
    val music = MusicModule(appData)
    val calendar = CalendarModule()
    val moduleList: Array<Module> = arrayOf(music, calendar)//Higher priority intents should be closer to the top
    lateinit var currentModule: Module

    val logTag="Intent Engine"

    //used for preprocessing
    private val zapsOSNicknames= "za(p|pp)sos|za(p|pp)s|za(p|pp)y"
    private val preprocessPattern: Pattern = Pattern.compile(zapsOSNicknames)


    fun detectAndRunCommand(input: String): Boolean{
        val preprocessedInput = preprocessInput(input)
        if (preprocessedInput == null || preprocessedInput == ""){ //TODO: add a way for Zaps to explain why a command failed
            Log.e(logTag,"Input string cleared by preprocessor")
            return false
        }
        Log.d(logTag,"Input preprocessed: $preprocessedInput")

        val intendedModule = classifyIntent(preprocessedInput)
        if (intendedModule==null) {
            Log.e(logTag,"No intended module detected from $preprocessedInput")
            return false
        }
        Log.d(logTag,"Intent found: ${intendedModule.moduleName}")

        val ranCommand = intendedModule.runCommand(preprocessedInput)
        if (!ranCommand) {
            Log.e(logTag,"Command failed to run")
            return false
        }

        Log.d(logTag,"Running command")
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