package com.example.jetpack.architecturecomponent.datalibs.work

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.jetpack.CHANNEL_ID
import com.example.jetpack.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class ExpeditedCoroutineWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    val context = appContext
    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(114, createNotification())
    }

    override suspend fun doWork(): Result {
        println("CoroutineWorker-----doWork")
        runBlocking {
            delay(5 * 1000)
        }
        return Result.success()
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("CoroutineWorker title")
            .setContentText("CoroutineWorker content")
            .setSmallIcon(R.drawable.notification_icon)
            .build()

    }
}