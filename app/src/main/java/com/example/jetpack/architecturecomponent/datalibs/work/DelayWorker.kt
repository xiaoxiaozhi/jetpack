package com.example.jetpack.architecturecomponent.datalibs.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class DelayWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
//        println("DelayWorker-----------doWorker1-----$id")
//        delay(20 * 1000)
//        println("DelayWorker-----------doWorker2-----$id")
        val breakAt = inputData.getInt("data", -1)
        repeat(10) {
            if (it == breakAt) return Result.failure()
            println("$it")
            delay(1000L)
        }
        return Result.success()
        return Result.failure()
    }
}