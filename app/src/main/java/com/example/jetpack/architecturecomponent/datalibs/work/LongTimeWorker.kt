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

/**
 * https://juejin.cn/post/7065505882078969870#comment
 * 这篇文章介绍了，WorkerManager长时间运行的关键 调用  setForeground() 问题是
 */
class LongTimeWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        println("LongTimeWorker--------doWork()")
        setForeground(ForegroundInfo(116, createNotification()))
        var i = 0
        while (true) {
            delay(60 * 1000)
            i += 1
            println("LongTimeWorker-------$i")
        }
        return Result.success()
    }


    private fun createNotification(): Notification {
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setContentTitle("CoroutineWorker title")
            .setContentText("CoroutineWorker content")
            .setSmallIcon(R.drawable.notification_icon)
            .build()

    }
}