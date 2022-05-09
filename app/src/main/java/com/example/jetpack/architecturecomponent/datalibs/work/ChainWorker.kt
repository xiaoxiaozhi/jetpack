package com.example.jetpack.architecturecomponent.datalibs.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ChainWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        println("ChainWorker----------------doWork()-----id=$id")
        return Result.success()
    }
}