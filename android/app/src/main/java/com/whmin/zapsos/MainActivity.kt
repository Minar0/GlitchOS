package com.whmin.zapsos

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    lateinit var inputBox: EditText
    lateinit var responseBox: TextView
    lateinit var metadata: Bundle
    var userInput: String = ""
    //val IntentRecognizer = IntentRecogManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.message_screen)

        inputBox=findViewById(R.id.userInput)
        responseBox=findViewById(R.id.zapsosResponse)

        inputBox.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            //I'm not sure why handled matters
            var handled = false

            //Code only runs when the send key is pressed
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                userInput=inputBox.text.toString()
                responseBox.text=userInput

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

        //This is a really fun way to do the metadata initialization using Kotlin's weird if statement variable stuff. metadata is equal to an if statement. if the build version is past a certain amount, it'll set it equal to the true block. If not, it'll use the old way of doing stuff
        metadata = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            this.packageManager.getApplicationInfo(this.packageName,PackageManager.ApplicationInfoFlags.of(0))
                .metaData
        }
        else
        {
            this.packageManager
                .getApplicationInfo(this.packageName, PackageManager.GET_META_DATA)
                .metaData
        }
    }
    fun goToSettings(view: View?) {
        val intent = Intent(this, SettingsActivity::class.java)
        val stringy = metadata.getString("SPOTIFY_ID")
        val toast = Toast.makeText(this, stringy, Toast.LENGTH_LONG) // in Activity
        toast.show()
        //startActivity(intent)
    }
}