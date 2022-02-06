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

class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    val context = appContext
    override fun doWork(): Result {
        val imageUriInput = inputData.getString("IMAGE_URI") ?: return Result.failure()

        // Do the work here--in this case, upload the images.
//        uploadImages()
        println("---UploadWorker----imageUriInput")
        runBlocking {
            delay(5 * 1000)
        }
        // Indicate whether the work finished successfully with the Result
        return Result.success()
//        return  Result.failure() //任务执行失败
//        return Result.retry()    //失败后需要重试
    }

}
