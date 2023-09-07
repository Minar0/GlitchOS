package com.whmin.glitchos


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.whmin.glitchos.intentengine.IntentEngine
import com.whmin.glitchos.service.GlitchOSConnection
import com.whmin.glitchos.service.GlitchOSService


class MainActivity : AppCompatActivity() {
    private lateinit var inputBox: EditText
    lateinit var responseBox: TextView
    private var glitchOSConnection = GlitchOSConnection(this)
    private lateinit var intentEngine: IntentEngine
    private var userInput: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_screen)

        inputBox = findViewById(R.id.user_input)
        responseBox = findViewById(R.id.glitchos_response)
        inputBox.setOnEditorActionListener { _, actionId, _ ->
            var handled = false //I'm not sure why handled matters

            //Code only runs when the send key is pressed
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                userInput = inputBox.text.toString()
                glitchOSConnection.glitchOSService?.runCommand(userInput)

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

        //Starts the ZapsOS Service. The connection callback gets a reference to the intentEngine and also sets up the provider
        val zapsOSServiceConnectionCallback: (() -> Unit) = {
            intentEngine = glitchOSConnection.glitchOSService?.intentEngine!!
            intentEngine.music.setupProvider(this)
            Log.d("Main","Created intentEngine")
        }
        glitchOSConnection.onConnectedCallback=zapsOSServiceConnectionCallback
        val glitchOSServiceIntent = Intent(this, GlitchOSService::class.java)
        startForegroundService(glitchOSServiceIntent)
        bindService(glitchOSServiceIntent, glitchOSConnection, Context.BIND_ABOVE_CLIENT)
    }

    override fun onStop() {
        super.onStop()
        glitchOSConnection.unbind()
    }

    fun goToSettings(view: View?) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun test1(view: View?) {
        Log.d("Test1", "Test1 pressed")
        intentEngine.music.providerModule?.togglePause()
    }

    fun test2(view: View?) {
        Log.d("Test2", "Test2 pressed")
        glitchOSConnection.glitchOSService?.listen()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        glitchOSConnection.
        glitchOSService?.
        intentEngine?.
        music?.
        providerModule?.
        onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }
}

