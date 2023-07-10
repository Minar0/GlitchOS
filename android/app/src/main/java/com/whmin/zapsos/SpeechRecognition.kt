package com.whmin.zapsos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast

class SpeechRecognition(private val appContext: Context) {
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


    //Used in startSpeechRecognition()
    private val speechRecognizerListener = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val speechResults =
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!speechResults.isNullOrEmpty()) {
                val recognizedText = speechResults[0]
                Toast.makeText(appContext, recognizedText, Toast.LENGTH_SHORT).show()
            }
        }
        override fun onPartialResults(p0: Bundle?) {}
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {Log.e(moduleName, "Something went wrong. Error code: $error")}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}