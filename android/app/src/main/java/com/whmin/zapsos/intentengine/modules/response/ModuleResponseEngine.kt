package com.whmin.zapsos.intentengine.modules.response

import com.whmin.zapsos.AppData
import java.util.concurrent.TimeUnit
import kotlin.random.Random

open class ModuleResponseEngine(private val appData: AppData) {
    open var normalTalkCooldown = 5
    private val random = Random(System.currentTimeMillis())
    private val maxRememberedResponses = 100
    open var maxTime = 99999999L
    private var rememberedResponses = ArrayDeque<Response>(maxRememberedResponses)

    //adds the response to the memory. Run in every speech function if you want it to be remembered
    fun addToRememberedResponses(command: String, responseName: String){
        if (rememberedResponses.size >= maxRememberedResponses) {rememberedResponses.removeLast() }
        rememberedResponses.addFirst(Response(command, responseName, getSystemTimeInSec()))
    }

    //region These functions give the ResponseEngine data to pull from with which to make snarky comments
    fun averageTimeBetweenCommand(commandName: String): Long { //TODO: Right now this finds the average time the command was made and NOT the average time between invocations of the command. Fix
        return averageTimeBetweenItem(pullCommands(commandName))
    }
    fun averageTimeBetweenResponse(commandName: String, responseName: String): Long{
        return averageTimeBetweenItem(pullResponses(commandName,responseName))
    }
    fun rememberedResponse(checkedCommandName: String, checkedResponseName: String): Boolean{
        return pullCommands(checkedCommandName)
            .any{ it.responseName == checkedResponseName }//I love how Array-like objects have these useful functions that do this shit for me
    }
    fun timeSinceLastCommand(checkedCommandName: String): Long{
        val rememberedCommands = pullCommands(checkedCommandName)
        if (rememberedCommands.isEmpty()){return maxTime}
        return getSystemTimeInSec() - rememberedCommands[0].timeOccurredInSec
    }
    fun amountOfCommandsInMemory(checkedCommandName: String): Int{
        return pullCommands(checkedCommandName).size
    }
    //endregion

    //region Useful functions used above
    private fun pullCommands(checkedCommandName: String): List<Response>{
        return rememberedResponses.filter{it.commandName == checkedCommandName}
    }
    private fun pullResponses(checkedCommandName: String, checkedResponseName: String): List<Response>{
        return rememberedResponses.filter{it.commandName == checkedCommandName && it.responseName==checkedResponseName}
    }
    private fun getSystemTimeInSec(): Long{
        val timeMillis = System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toSeconds(timeMillis)
    }
    private fun averageTimeBetweenItem(list: List<Response>): Long{
        val listSize = list.size
        if (listSize==0) {return maxTime}

        var totalTimeDiff = 0L
        for (i in 0 until listSize - 1) {
            val difference = list[i].timeOccurredInSec - list[i+1].timeOccurredInSec
            totalTimeDiff += difference
        }
        return totalTimeDiff / listSize
    }
    fun percentageChance(percentage: Double): Boolean{
        return random.nextInt(1, 101) <= percentage
    }
    fun randNum(maxNum: Int, minNum: Int = 1): Int{
        return random.nextInt(minNum, maxNum+1)
    }
    //endregion

    fun speakError() {
        addToRememberedResponses("error","error")
        appData.soundOutput.speak("Ironically a real glitch has occurred. If you hear this, please relay the information to the developer")
    }
    fun successBeep(commandName: String){
        addToRememberedResponses(commandName,"beep")
        appData.soundOutput.playSuccessBeep()
    }
}