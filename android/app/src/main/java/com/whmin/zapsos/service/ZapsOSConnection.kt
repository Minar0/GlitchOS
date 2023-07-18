package com.whmin.zapsos.service

import android.app.Activity
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class ZapsOSConnection: ServiceConnection {
    var isBound = false
    var zapsOSService: ZapsOSService? = null

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as ZapsOSService.LocalBinder
        zapsOSService = binder.getService()
        isBound = true
    }
    override fun onServiceDisconnected(name: ComponentName?) {
        zapsOSService = null
        isBound = false
    }

    fun unbind(activity: Activity){
        if (isBound) {
            activity.unbindService(this)
            isBound = false
        }
    }
}