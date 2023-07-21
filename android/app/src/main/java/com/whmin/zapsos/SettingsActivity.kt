package com.whmin.zapsos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.whmin.zapsos.intentengine.IntentEngine
import com.whmin.zapsos.service.ZapsOSConnection
import com.whmin.zapsos.service.ZapsOSService


private const val TITLE_TAG = "settingsActivityTitle"

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback { //Most of this stuff is boilerplate code made by Android Studio. I'm slowly figuring out how it all works. Never worked with Fragments before
    private var zapsOSConnection = ZapsOSConnection(this)
    lateinit var intentEngine: IntentEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, HeaderFragment())
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_settings)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val zapsOSServiceConnectionCallback: (() -> Unit) = {
            intentEngine = zapsOSConnection.zapsOSService?.intentEngine!!
            intentEngine.music.setupProviderListener(this)
        }
        zapsOSConnection.onConnectedCallback=zapsOSServiceConnectionCallback
        val zapsOSServiceIntent = Intent(this, ZapsOSService::class.java)
        bindService(zapsOSServiceIntent, zapsOSConnection, Context.BIND_ABOVE_CLIENT)
    }

    override fun onPause() {
        super.onPause()
        intentEngine.music.cleanupProviderListener()//Do I really need to deregister the listener? It seems like Android will do it for me
        zapsOSConnection.unbind()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment.toString()
        ).apply {
            arguments = args
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        title = pref.title
        return true
    }

    @Deprecated("Deprecated in Java")// I know, I know, bad practice. I don't know how to get the activity result from the Spotify Auth Lib using the activity result API. TODO: learn how to use activity result api
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //IntentEngine.music.providerModule?.onActivityResult(requestCode, resultCode, data)
    }

    class HeaderFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey)
        }
    }

    class MusicFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.music_preferences, rootKey)
        }
    }

    class SyncFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.sync_preferences, rootKey)
        }
    }
}