package com.example.jetpack.architecturecomponent.datalibs.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * 重试策略
 */
class BackoffCriteriaWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        println("BackoffCriteriaWorker------doWorker")
        return Result.retry()
    }

}