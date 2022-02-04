package com.example.jetpack.topics.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.jetpack.CHANNEL_ID
import com.example.jetpack.R

/**
 * 前台服务执行用户可以注意到的操作
 * 前台服务必须要显示通知，除非服务被停止或从前台删除，
 *
 * 1. Android 12 (API level 31) 及以上设备显示前台通知会等待10秒显示通知。有两种情况例外
 *    1.1.在前台服务中播放音乐的音乐播放器应用程序
 *    1.2.健身应用程序，它记录用户在前台服务中的运行情况，并获得用户的许可。通知可能会显示用户在当前健身会话期间所走过的距离。
 *    1.3.关联的通知带有操作按钮
 *    1.4.foregroundServiceType 类型是  mediaPlayback, mediaProjection, or phoneCall 中的一种
 *    1.5.设置 setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
 *
 * 2. Android 9 (API level 28)及以上系统必须在配置文件中添加android:name="android.permission.FOREGROUND_SERVICE 权限请求
 *    由于这个是普通权限系统会自动授予，如果android 9+设备不请求该权限会报错 SecurityException.
 *
 * 3. 启动前台服务
 *    3.1. android12+ 设备不能在后台启动前台服务，否则会报错。作为替换请使用WorkManager
 *    3.2. 这种情况下https://developer.android.google.cn/guide/components/foreground-services#background-start-restriction-exemptions
 *         android12+ 的设备可以启动前台服务
 * 4. 删除前台服务
 *     stopForeground()从前台状态移除，并不会终止service
 * 5. 设置前台服务类型
 *    当你的设备是Android10+的时候前台服务访问位置需要在manifest.xml 中添加 <service android:foregroundServiceType="location" />，访问相机和麦克风不用添加
 *    当你的设备是Android11+的时候前台服务访问相机或者麦克风，需要添加  <service ... android:foregroundServiceType="microphone|camera" />
 *    前台服务如果要访问的类型只是配置清单的子集，使用以下代码做限制
 *    Service.startForeground(notification, FOREGROUND_SERVICE_TYPE_LOCATION or FOREGROUND_SERVICE_TYPE_CAMERA)
 * 6. 约束前台服务访问 位置、相机、麦克风
 *    6.1 Android11+ 当应用程序在后台运行时启动前台服务，除非用户已经向程序授权, ACCESS_BACKGROUND_LOCATION否则无法访问 位置
 *    6.2 Android11+ 当应用程序在后台运行时启动前台服务无法访问 相机、麦克风
 *    6.3 豁免条件 https://developer.android.google.cn/guide/components/foreground-services#bg-access-restriction-exemptions
 *    6.4 启动的前台服务被限制访问 位置、相机、麦克风 logcat会打印Foreground service started from background can not have \
          location/camera/microphone access: service SERVICE_NAME
 *
 *
 *
 */
class ForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.apply {
            when (getIntExtra("opt", -1)) {
                0 -> startF()//启动前台服务，怎么验证启动了呢？？？
                1 -> stopForeground(true)//从前台状态移除，并不会终止service
                else -> println("没找到")
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    fun startF() {
        val pendingIntent: PendingIntent =
            Intent(this, ForegroundActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }
        val notification: Notification =
            NotificationCompat.Builder(this@ForegroundService, CHANNEL_ID)
                .setContentTitle("前台服务")
                .setContentText("显示前台服务")
                .setSmallIcon(R.drawable.foreground)
//                .setContentIntent(pendingIntent)
                .setTicker("什么是ticker")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(107, notification, FOREGROUND_SERVICE_TYPE_LOCATION)//配置清单，拥有 相机、位置、麦克风。在这里只访问位置
        } else {
            startForeground(107, notification)
        }


    }
}