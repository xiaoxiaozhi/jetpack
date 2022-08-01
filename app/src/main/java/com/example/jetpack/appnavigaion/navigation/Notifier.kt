/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.jetpack.appnavigaion.navigation

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.jetpack.CHANNEL_ID
import com.example.jetpack.R

/**
 * Utility class for posting notifications.
 * This class creates the notification channel (as necessary) and posts to it when requested.
 */
object Notifier {


    fun postNotification(context: Context, intent: PendingIntent) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        builder.setContentTitle("深层链接")
            .setSmallIcon(R.drawable.diy)
        val notification = builder.setContentText("点击跳转到深层链接")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        val notificationManager = NotificationManagerCompat.from(context)
        // Remove prior notifications; only allow one at a time to edit the latest item
        notificationManager.cancelAll()
        notificationManager.notify(555, notification)
    }
}
