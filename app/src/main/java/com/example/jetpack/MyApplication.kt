package com.example.jetpack

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID1,
                    "特殊通知",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "启动特殊通知"
                })

            //注册进度条通知
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_PROGRESS,
                    "显示进度条",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "用通知显示进度"
                })
            //注册全屏通知
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_FULL,
                    "全屏通知",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "显示全屏通知"
                })
            //注册即时通讯通知
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_MESSAGE,
                    "即时通讯",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "显示即时通讯"
                })
            //大图片
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_PICTURE,
                    "大图片",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "显示大图片"
                })
            //收件箱式通知
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_INBOX,
                    "收件箱样式通知",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "显示通知"
                })
            //多媒体通知
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_MEDIA,
                    "多媒体通知",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "显示多媒体通知"
                })

        }
    }
}