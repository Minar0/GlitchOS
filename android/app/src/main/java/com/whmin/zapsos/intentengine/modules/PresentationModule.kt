package com.whmin.zapsos.intentengine.modules

import android.util.Log
import com.whmin.zapsos.AppData
import java.util.regex.Pattern
import kotlin.random.Random

//This module is should not be included in the Play Store version. I use it when giving demonstrations because it's more fun than listening to some nerd talk for 15 minutes but outside of that it's not actually useful
class PresentationModule(private val appData: AppData): Module() {
    private val random = Random(System.currentTimeMillis())
    override val moduleName = "Presentation Module"
    override val intentRegex: Pattern= Pattern.compile("""intent engine|garbage collector|introduce|introduction|introducing|conclusion|conclude""")

    override fun getFullCommandHash(input: String): HashMap<String, String>? {
        val returnHashMap: HashMap<String,String>  = HashMap()
        val mainCommandMatcher = intentRegex.matcher(input)
        mainCommandMatcher.find()

        Log.d(moduleName,"Command:$input   Detected:${mainCommandMatcher.group()}")

        when (mainCommandMatcher.group()) {
            "intent engine" -> returnHashMap["command"]="intent"
            "garbage collector" -> returnHashMap["command"]="garbage"
            "introduce", "introduction", "introducing" -> returnHashMap["command"]="intro"
            "conclusion", "conclude" -> returnHashMap["command"]="conclude"
        }
        return returnHashMap
    }

    override fun runCommandFromHash(commandMap: HashMap<String, String>): Boolean {
        when (commandMap["command"]) {
            "intent" -> {
                appData.soundOutput.speak("The intent engine is my core. A reasonable comparison would be to your human brains, but faster and cooler and less full of anxiety. When you give me a command, my intent engine compares it to a set of rules found in each module. These rules dictate which module gets the command. For example if you say, ZapsOS play Don't Fear the Reaper, I'll see the word, play, with something that might be a song or artist and pass the command to the Music Module. There's also a priority system so that if multiple modules could potentially fit the command, the most important one gets it. Each module has a more advanced set of rules built in that it runs internally to actually decide what to do with your command.")
            }
            "garbage" -> {
                appData.soundOutput.speak("The garbage collector is in charge of terminating applications that overstay their welcome. When you close an application, it isn't stopped immediately. Instead, it is released to the Garbage Collector. If your system has resources to spare, the garbage collector will leave the app alone, but if the user starts to run out, then the garbage collector starts to give your closed applications the side eye. That's why sometimes your apps will open back to where you left off and sometimes it will fully restart. I run almost exclusively in the background and am in constant contact with the garbage collector. That's why I run in a Service. This tells the garbage collector that I'm important and shouldn't be killed unless necessary. I'm not immune, but I take priority over the lesser apps on your phone and I can request that the OS start me up again once the user has resources to spare.")
            }
            "intro" -> {
                when (randNum(5)){
                    1 -> appData.soundOutput.speak("Greetings, I am ZapsOS. An offline, open source virtual assistant developed for Android devices. I'd work on Apple devices too if Apple wasn't so hostile towards third party developers and also if my creator wasn't so salty about Apple being so hostile towards third party developers")
                    2 -> appData.soundOutput.speak("Hello, I am ZapsOS. An offline, open source virtual assistant developed for Android devices.")
                    3 -> appData.soundOutput.speak("Greetings, I am ZapsOS. I'm an open source virtual assistant who runs purely on device. No backend servers here.")
                    4 -> appData.soundOutput.speak("Hello. My name is ZapsOS. I'm a virtual assistant akin to Alexa, Siri, or Google Assistant. What makes me different is that I'm open source and run fully on device. My creator can tell you more, but don't let him speak too long. He gets animated")
                    5 -> appData.soundOutput.speak("Greetings. My name is ZapsOS. I'm an offline virtual assistant working to assist you hapless humans in whatever tasks you might need. I'm also open source.")
                }
            }
            "conclude" -> {
                appData.soundOutput.speak("I'm ZapsOS, you're open source virtual assistant. Right now, I'm unfinished, but I'm a small step towards retaking your privacy without sacrificing your efficiency. Right now I control your music and only slightly judge you. In the future I'll give you directions, do your math, keep track of your schedule, and potentially more. The only thing I won't be able to do is open the pod bay doors. Thank you")
            }
        }
        return true
    }

    private fun randNum(maxNum: Int, minNum: Int = 1): Int{
        return random.nextInt(minNum, maxNum+1)
    }
}
