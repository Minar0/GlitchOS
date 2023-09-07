package com.whmin.glitchos.speech

import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import com.whmin.glitchos.R
import java.util.Locale

class SoundOutputEngine (context: Context, private val onInitCallback: () -> Unit): OnInitListener {
    val moduleName = "Sound Output Engine"
    private var speechSynthesizer = TextToSpeech(context, this)
    private var listenBeep = MediaPlayer.create(context, R.raw.listening_beep)
    private var successBeep = MediaPlayer.create(context, R.raw.success_beep)

    fun speak(text: String) {
        speechSynthesizer.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun playListenBeep(){
        listenBeep.start()
    }

    fun playSuccessBeep(){
        successBeep.start()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(moduleName, "Synthesizer initialized")

            val availableVoices = speechSynthesizer.voices
            for (voice in availableVoices) {
                val locale = voice.locale
                val name = voice.name
                val quality = voice.quality
                Log.d(moduleName,"Voice name: $name,  Quality: $quality,  Locale:$locale")
                if (name=="en-AU-default"){speechSynthesizer.voice = voice}//I don't think this line works. TODO: Fix
            }

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
