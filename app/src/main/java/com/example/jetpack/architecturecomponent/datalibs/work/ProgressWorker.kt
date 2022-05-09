package com.example.jetpack.architecturecomponent.datalibs.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

/**
 *  运行之后会发现，打印的是1 ，10 最后一个progress 20 没有打印，可能是因为 Worker已经是成功状态，progress不再打印，工作中收到success状态直接判定为百分之百即可
 */
class ProgressWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    companion object {
        const val Progress = "Progress"
        private const val delayDuration = 1000L
    }

    override suspend fun doWork(): Result {

        setProgress(workDataOf(Progress to 1))// 对于使用 ListenableWorker 或 Worker 的 Java 开发者，setProgressAsync() API 会返
        delay(delayDuration)
        setProgress(workDataOf(Progress to 10))
        delay(delayDuration)
        setProgress(workDataOf(Progress to 20))
        println("ProgressWorker---------doWork()")
        return Result.success()
        // 运行之后会发现，打印的是1 ，10 最后一个progress 20 没有打印，可能是因为 Worker已经是成功状态，progress不再打印，工作中收到success状态直接判定为百分之百即可
    }

}