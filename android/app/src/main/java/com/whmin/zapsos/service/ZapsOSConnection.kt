package com.whmin.zapsos.service

import android.app.Activity
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

class ZapsOSConnection(private val activity: Activity): ServiceConnection {
    val moduleName = "ZapsOS Connector"
    var isBound = false
    var zapsOSService: ZapsOSService? = null
    var onConnectedCallback: (() -> Unit)? = null

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as ZapsOSService.LocalBinder
        zapsOSService = binder.getService()
        isBound = true
        onConnectedCallback?.let {it()}
        Log.d(moduleName, "Activity bound to ZapsOS Service")
    }
    override fun onServiceDisconnected(name: ComponentName?) {
        zapsOSService = null
        isBound = false
    }

    fun unbind(){
        if (isBound) {
            activity.unbindService(this)
            isBound = false
        }
    }
}