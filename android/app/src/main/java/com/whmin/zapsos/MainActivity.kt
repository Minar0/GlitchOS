package com.whmin.zapsos

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.whmin.zapsos.modules.ModuleManager


class MainActivity : AppCompatActivity() {
    lateinit var inputBox: EditText
    lateinit var responseBox: TextView
    lateinit var metadata: Bundle
    lateinit var moduleManager: ModuleManager
    var userInput: String = ""
    //val IntentRecognizer = IntentRecogManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_screen)

        inputBox = findViewById(R.id.userInput)
        responseBox = findViewById(R.id.zapsosResponse)

        inputBox.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            //I'm not sure why handled matters
            var handled = false

            //Code only runs when the send key is pressed
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                userInput = inputBox.text.toString()
                responseBox.text = userInput

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
        })

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        metadata = getApplicationMetadata()
        moduleManager = ModuleManager(metadata,sharedPref)
    }

    fun goToSettings(view: View?) {
        val intent = Intent(this, SettingsActivity::class.java)
        val stringy = metadata.getString("SPOTIFY_ID")
        val toast = Toast.makeText(this, stringy, Toast.LENGTH_LONG) // in Activity
        toast.show()
        startActivity(intent)
    }

    fun test(view: View?){
        moduleManager.music
    }

    private fun getApplicationMetadata(): Bundle {
        //I don't care if it's depreciated. It works and I can't spend more time on it
        val appInfo: ApplicationInfo =
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return appInfo.metaData
    }
}
