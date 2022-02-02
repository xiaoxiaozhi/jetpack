package com.example.jetpack.topics.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class BindService : Service() {

    override fun onCreate() {
        super.onCreate()
    }

    //如果您并不希望允许绑定，则应返回 null。
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}