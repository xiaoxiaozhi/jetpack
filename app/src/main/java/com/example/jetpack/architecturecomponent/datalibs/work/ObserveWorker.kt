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

class ObserveWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        for (i in 1..10) {
            delay(10 * 1000)
            println("ObserveWorker-----$i")
        }
//        println("ObserveWorker-----")
        return Result.success()
    }

}