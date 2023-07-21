package com.whmin.zapsos.intentengine.modules.response

import com.whmin.zapsos.AppData

open class ModuleResponseEngine(private val appData: AppData) {
    private var maxRememberedResponses = 30
    var rememberedResponses = ArrayDeque<Response>(maxRememberedResponses)


    fun addToRememberedResponses(command: String, responseName: String){
        if (rememberedResponses.size >= maxRememberedResponses) {rememberedResponses.removeLast() }
        rememberedResponses.addFirst(Response(command, responseName, System.currentTimeMillis()))
    }

    fun averageTimeBetweenCommand(command: String): Long{
        //for (response)

        val totalResponseTime = rememberedResponses.sumOf {it.time}
        val amountOfResponses = rememberedResponses.size

        return if (amountOfResponses > 0) {totalResponseTime / amountOfResponses} else {0}
    }

    fun averageTimeBetweenResponseName(responseName: String){

    }
}