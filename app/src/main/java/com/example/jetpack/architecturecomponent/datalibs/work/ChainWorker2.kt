package com.example.jetpack.architecturecomponent.datalibs.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

class ChainWorker2(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        println("ChainWorker2----------------doWork()1-----id=$id")
        delay(10*1000)
        println("ChainWorker2----------------doWork()2-----id=$id")
        return Result.success(workDataOf("data" to 2))
    }
}