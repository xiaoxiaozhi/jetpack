package com.example.jetpack.architecturecomponent.datalibs.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

class ChainWorker4(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        println("ChainWorker4----------------doWork()-----id=$id")
        println("ChainWorker4 key:data value:$inputData")
        return Result.failure(workDataOf("data" to "失败"))
    }
}