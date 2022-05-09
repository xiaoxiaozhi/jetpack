package com.example.jetpack.architecturecomponent.datalibs.work

import android.annotation.SuppressLint
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

class PeriodicWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    val context = appContext

    //doWork() 方法在 WorkManager 提供的后台线程上异步运行。
    override fun doWork(): Result {
        println("PeriodicWorker----imageUriInput")
        return Result.success()
//        return  Result.failure() //任务执行失败
//        return Result.retry()    //失败后需要重试
    }

}
