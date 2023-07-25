package com.whmin.zapsos.speech

import android.content.Context
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class SpeechRecognitionEngine(private val appContext: Context, private val speechRecognizerListener: RecognitionListener) {
    val moduleName = "Speech Recognition"
    private lateinit var speechRecognizer: SpeechRecognizer
    private val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

    init{
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Start Speaking")
        speechIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
    }



    fun startSpeechRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
        speechRecognizer.setRecognitionListener(speechRecognizerListener)
        speechRecognizer.startListening(speechIntent)
    }

    fun destroy(){
        speechRecognizer.destroy()
    }
}