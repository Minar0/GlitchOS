package com.whmin.zapsos


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import com.whmin.zapsos.intentengine.IntentEngine
import com.whmin.zapsos.service.ZapsOSConnection
import com.whmin.zapsos.service.ZapsOSService


class MainActivity : AppCompatActivity() {
    private var zapsOSConnection = ZapsOSConnection()
    private lateinit var inputBox: EditText
    lateinit var responseBox: TextView
    var userInput: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_screen)

        inputBox = findViewById(R.id.user_input)
        responseBox = findViewById(R.id.zapsos_response)
        inputBox.setOnEditorActionListener { _, actionId, _ ->
            var handled = false //I'm not sure why handled matters

            //Code only runs when the send key is pressed
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                userInput = inputBox.text.toString()
                zapsOSConnection.zapsOSService?.runCommand(userInput)

                //This will close the soft keyboard from the current view, if it is focused
                val view = this.currentFocus
                if (view != null) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
                //Clears the text in the input box
                inputBox.setText("")
                handled = true
            }
            handled
        }

        //Starts the ZapsOS Service
        val zapsOSServiceIntent = Intent(this, ZapsOSService::class.java)
        startForegroundService(zapsOSServiceIntent)
        bindService(zapsOSServiceIntent, zapsOSConnection, Context.BIND_ABOVE_CLIENT)
    }

    override fun onStart() {
        if (IntentEngine.isInit) {IntentEngine.music.setupProvider(this)}
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        zapsOSConnection.unbind(this)
    }

    fun goToSettings(view: View?) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun test1(view: View?){
        Log.d("Test1", "Test pressed")
        val musicManager = IntentEngine.music
        musicManager.providerModule?.togglePause()
    }

    fun test2(view: View?){
        Log.d("Test2", "Test2 pressed")
        zapsOSConnection.zapsOSService?.listen()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        IntentEngine.music.providerModule?.onActivityResult(requestCode,resultCode,data)
    }
}
