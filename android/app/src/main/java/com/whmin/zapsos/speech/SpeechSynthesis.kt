package com.whmin.zapsos.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import java.util.Locale

class SpeechSynthesis (private val context: Context, private val onInitCallback: () -> Unit): OnInitListener {
    val moduleName = "Speech Synthesizer"
    private var speechSynthesizer = TextToSpeech(context, this)

    fun speak(text: String) {
        speechSynthesizer.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }


    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(moduleName, "Synthesizer initialized")

            val result = speechSynthesizer.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(moduleName, "ENG-US is not supported by device")
            } else {
                Log.d(moduleName, "Language initialized")
                onInitCallback()
            }
        } else {
            Log.e(moduleName, "Synthesizer failed to initialize")
        }
    }
}
