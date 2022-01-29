package com.example.jetpack.topics.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * 前台服务执行用户可以注意到的操作
 * 前台服务必须要显示通知，除非服务被停止或从前台删除，
 * Android 12 (API level 31) 及以上设备显示前台通知会等待10秒显示通知。有两种情况例外
 * 1. 在前台服务中播放音乐的音乐播放器应用程序
 * 2. 健身应用程序，它记录用户在前台服务中的运行情况，并获得用户的许可。通知可能会显示用户在当前健身会话期间所走过的距离。
 * Android 9 (API level 28)及以上系统必须在配置文件中添加android:name="android.permission.FOREGROUND_SERVICE 权限请求，由于这个是普通权限系统会自动授予
 *
 */
class ForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
//        startForeground()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}