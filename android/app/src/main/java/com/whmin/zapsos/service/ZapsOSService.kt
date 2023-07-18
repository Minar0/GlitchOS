package com.whmin.zapsos.service

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.preference.PreferenceManager
import com.whmin.zapsos.speech.SpeechRecognition
import com.whmin.zapsos.intentengine.IntentEngine
import com.whmin.zapsos.speech.SpeechSynthesis

class ZapsOSService : Service() {
    private lateinit var sharedPref : SharedPreferences
    private lateinit var metadata : Bundle
    private lateinit var speechRecognizer : SpeechRecognition
    private lateinit var speechSynthesizer: SpeechSynthesis
    val moduleName = "ZapsOS Service"

    fun runCommand(input: String){
        val zapsResponse = IntentEngine.detectAndRunCommand(input)
        speechSynthesizer.speak(zapsResponse)
    }

    fun listen(){
        speechRecognizer.startSpeechRecognition()
    }

    override fun onCreate() {
        super.onCreate()

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        metadata = getApplicationMetadata()

        speechRecognizer = SpeechRecognition(applicationContext,speechRecognizerListener)
        speechSynthesizer = SpeechSynthesis(this,speechSynthInitCallback)

        IntentEngine.initialize(metadata, sharedPref, applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    //These return a local binder to allow activities to connect to the service
    inner class LocalBinder : Binder() {
        fun getService(): ZapsOSService = this@ZapsOSService
    }
    override fun onBind(intent: Intent): IBinder? {
        return LocalBinder()
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        super.onDestroy()
    }

    private fun getApplicationMetadata(): Bundle {
        //I don't care if it's depreciated. It works and I can't spend more time on it
        return packageManager
            .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            .metaData
    }

    private val speechRecognizerListener = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            Log.d(moduleName,"Resulted")
            val speechResults =
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!speechResults.isNullOrEmpty()) {
                val recognizedText = speechResults[0]
                Log.d(moduleName,recognizedText.toString())
                runCommand(speechResults.toString())
            }
        }
        override fun onPartialResults(p0: Bundle?) {}
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
            Log.e(moduleName, "Something went wrong. Error code: $error")}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }


    private val speechSynthInitCallback: () -> Unit = {
        speechSynthesizer.speak("Initialized")
    }
}