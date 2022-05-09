package com.example.jetpack.architecturecomponent.datalibs.work

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.example.jetpack.CHANNEL_ID
import com.example.jetpack.R
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * 查询 CoroutineWorker的代码，getForegroundInfoAsync 被标记会删除的API
 */
class ExpeditedWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        println("ExpeditedWorker-------------doWorker")
        return Result.success()
    }

    /**
     * 代码来自 [leakcanary-android-core](https://github.com/square/leakcanary/search?q=heapAnalysisForegroundInfoAsync)
     * android 12 之前不实现getForegroundInfoAsync会报错要求实现一个
     * android 12 之后即时实现了，也不会调用
     */
    @SuppressLint("RestrictedApi")
    override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> {
        println("ExpeditedWorker-------getForegroundInfoAsync")
        val infoFuture = SettableFuture.create<ForegroundInfo>()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("ExpeditedWorker 通知")
            .setContentText("LeakCanary is working.")
            .setSmallIcon(R.drawable.notification_icon)
            .setAutoCancel(true)
        infoFuture.set(
            ForegroundInfo(
                115,
                builder.build()
            )
        )
        return infoFuture
//        return ListenableFuture<>()ForegroundInfo(115,createNotification())
//        return super.getForegroundInfoAsync();
    }

    override fun onStopped() {
        super.onStopped()
        println("ExpeditedWorker---------onStopped()")
    }
}