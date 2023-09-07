package com.whmin.glitchos.service

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.preference.PreferenceManager
import com.whmin.glitchos.AppData
import com.whmin.glitchos.speech.SpeechRecognitionEngine
import com.whmin.glitchos.intentengine.IntentEngine
import com.whmin.glitchos.speech.SoundOutputEngine

class GlitchOSService : Service() {
    private lateinit var speechRecognizer : SpeechRecognitionEngine
    lateinit var intentEngine: IntentEngine
    lateinit var appData: AppData
    val moduleName = "GlitchOS Service"

    fun runCommand(input: String){
        intentEngine.detectAndRunCommand(input)
    }

    fun listen(){
        speechRecognizer.startSpeechRecognition()
    }

    override fun onCreate() {
        super.onCreate()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val metadata = getApplicationMetadata()

        speechRecognizer = SpeechRecognitionEngine(applicationContext,speechRecognizerListener)
        val soundOutput = SoundOutputEngine(this,speechSynthInitCallback)

        appData = AppData(metadata, sharedPref, applicationContext, soundOutput)
        intentEngine = IntentEngine(appData)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    //These return a local binder to allow activities to connect to the service
    inner class LocalBinder : Binder() {
        fun getService(): GlitchOSService = this@GlitchOSService
    }
    override fun onBind(intent: Intent): IBinder {
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
            val speechResults =
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!speechResults.isNullOrEmpty()) {
                val recognizedText = speechResults[0].toString().lowercase()
                Log.d(moduleName, "Detected voice input: $recognizedText")
                runCommand(recognizedText)
            }
        }
        override fun onPartialResults(p0: Bundle?) {}
        override fun onReadyForSpeech(params: Bundle?) {
            appData.soundOutput.playListenBeep()}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
            Log.e(moduleName, "Something went wrong. Error code: $error")
            SpeechRecognizer.ERROR_NETWORK
        }
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }


    private val speechSynthInitCallback: () -> Unit = {
        appData.soundOutput.playSuccessBeep()
    }
}