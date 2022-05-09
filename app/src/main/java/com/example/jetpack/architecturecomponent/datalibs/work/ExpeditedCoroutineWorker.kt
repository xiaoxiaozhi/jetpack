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
 * 一个提供与 Kotlin Coroutines 互操作的 ListenableWorker 实现。默认情况下，CoroutineWorker 在[ Dispatchers. Default ]上运行; 这可以通过重写[ coroutineContext ]进行修改。
 * CoroutineWorker 最多有10分钟的时间来完成它的执行并返回一个[ listableworker。结果]。在这个时间过期之后，将发出停止工作的信号。
 */
class ExpeditedCoroutineWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    val context = appContext

    /**
     * android 12 之前不实现getForegroundInfoAsync会报错要求实现一个
     * android 12 之后可以不必实现，即时实现了也不会调用
     */
    override suspend fun getForegroundInfo(): ForegroundInfo {
        println("ExpeditedCoroutineWorker--------getForegroundInfo()")
        return ForegroundInfo(114, createNotification())
    }

    override suspend fun doWork(): Result {
        println("ExpeditedCoroutineWorker-----doWork-------$id")
//        setForeground(getForegroundInfo()) 官方文档上说，为兼容android 12 之前的版本，要在这里调用以显示通知，实际测试调用了之后通知会显示两遍
        delay(5 * 1000)
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