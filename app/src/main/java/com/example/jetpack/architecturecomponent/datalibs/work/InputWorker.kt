package com.example.jetpack.architecturecomponent.datalibs.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class InputWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        println("InputWorker------doWorker")
        println("inputData = ${inputData.getDouble(DATANAME, 0.0)}")
        return Result.retry()
    }

    companion object {
        const val DATANAME: String = "data1"
    }
}

