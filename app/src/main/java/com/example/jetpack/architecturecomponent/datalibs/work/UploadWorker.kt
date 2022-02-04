package com.example.jetpack.architecturecomponent.datalibs.work

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.SettableFuture
import com.google.common.util.concurrent.ListenableFuture

class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        // Do the work here--in this case, upload the images.
//        uploadImages()
        println("UploadWorker")
        // Indicate whether the work finished successfully with the Result
        return Result.success()
//        return  Result.failure() //任务执行失败
//        return Result.retry()    //失败后需要重试
    }

//    @SuppressLint("RestrictedApi")
//    override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> {
//        val future = SettableFuture.create<ForegroundInfo>()
//
//        val notificationId = id.hashCode()
//        val fileName = inputData.getString(KEY_OUTPUT_FILE_NAME)
//
//        if (fileName == null) {
//            future.setException(IllegalStateException("Filename is null"))
//            return future
//        }
//
//        val notificationBuilder = getNotificationBuilder(fileName)
//
//        future.set(ForegroundInfo(notificationId, notificationBuilder.build()))
//        return future
//    }
}
