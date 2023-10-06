package com.example.jetpack

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.hardware.camera2.CameraMetadata.LENS_FACING_FRONT
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.camera2.internal.Camera2CameraFactory
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraXConfig
import androidx.camera.core.impl.CameraFactory
import androidx.camera.core.impl.CameraThreadConfig
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.jetpack.topics.dependencyinjection.mydagger2.MyComponent
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApplication : Application()
//    , CameraXConfig.Provider
{
//    lateinit var component: MyComponent
    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
//        component = DaggerMyComponent.create()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            println("Build.VERSION.SDK_INT = ${Build.VERSION.SDK_INT}")
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }// importance 此参数确定出现任何属于此渠道的通知时如何打断用户
            // 注册通知
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(NotificationChannel(
                CHANNEL_ID1, "特殊通知", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "启动特殊通知"
            })

            //注册进度条通知
            notificationManager.createNotificationChannel(NotificationChannel(
                CHANNEL_PROGRESS, "显示进度条", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "用通知显示进度"
            })
            //注册全屏通知
            notificationManager.createNotificationChannel(NotificationChannel(
                CHANNEL_FULL, "全屏通知", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "显示全屏通知"
            })
            //注册即时通讯通知
            notificationManager.createNotificationChannel(NotificationChannel(
                CHANNEL_MESSAGE, "即时通讯", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "显示即时通讯"
            })
            //大图片
            notificationManager.createNotificationChannel(NotificationChannel(
                CHANNEL_PICTURE, "大图片", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "显示大图片"
            })
            //收件箱式通知
            notificationManager.createNotificationChannel(NotificationChannel(
                CHANNEL_INBOX, "收件箱样式通知", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "显示通知"
            })
            //多媒体通知
            notificationManager.createNotificationChannel(NotificationChannel(
                CHANNEL_MEDIA, "多媒体通知", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "显示多媒体通知"
            })

        } else {
            println("--------sdk小于android O 26----------")
        }
    }

//    override fun getCameraXConfig(): CameraXConfig {
//        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())//查看defaultConfig()源码会发现，那三个Provider是必须的，否则会报错。配置主要在下面几行代码体现
//            .setAvailableCamerasLimiter(CameraSelector.DEFAULT_FRONT_CAMERA).setCameraExecutor(mainExecutor)
//            .setMinimumLoggingLevel(Log.INFO).setSchedulerHandler(Handler(Looper.getMainLooper()))
//            .build()//从 Camera2Config 获取配置
//
//    }
}