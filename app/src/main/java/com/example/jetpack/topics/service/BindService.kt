package com.example.jetpack.topics.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class BindService : Service() {

    override fun onCreate() {
        super.onCreate()
    }
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}