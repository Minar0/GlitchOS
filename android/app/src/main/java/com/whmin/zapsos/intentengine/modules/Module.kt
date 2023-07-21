@file:Suppress("FoldInitializerAndIfToElvis")

package com.whmin.zapsos.intentengine.modules

import android.util.Log
import java.util.regex.Pattern

abstract class Module {
    open val moduleName: String = "Module"
    abstract val intentRegex: Pattern //This string is how IntentEngine knows what module to use

    open fun detectIntent(input: String): Boolean {
        return intentRegex.matcher(input).find()
    }

    fun runCommand(input: String): Boolean{
        val commandHash = getFullCommandHash(input)
        if (commandHash==null){return false}

        val ranCommand = runCommandFromHash(commandHash)
        if (!ranCommand){return false}

        Log.d(moduleName,"Ran command. Command Hash: $commandHash")

        return true
    }


    //function will return a HashMap where each key is a subcommand and its value is used in the subcommand somehow. Value can be empty
    abstract fun getFullCommandHash(input: String): HashMap<String, String>?

    //function will run a command based on the HashMap
    abstract fun runCommandFromHash (commandMap :HashMap<String, String>): Boolean
}