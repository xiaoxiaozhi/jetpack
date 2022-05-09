package com.example.jetpack.architecturecomponent.datalibs.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

class ChainWorker5(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        println("ChainWorker5----------------doWork()-----id=$id")
        println("ChainWorker5 key:data value:$inputData")
        return Result.success()
    }
}