package com.whmin.zapsos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    lateinit var inputBox: EditText
    lateinit var responseBox: TextView
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
    }
    fun goToSettings(view: View?) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}