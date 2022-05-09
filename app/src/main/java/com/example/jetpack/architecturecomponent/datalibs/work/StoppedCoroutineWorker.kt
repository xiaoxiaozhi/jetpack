package com.example.jetpack.architecturecomponent.datalibs.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlin.coroutines.cancellation.CancellationException

/**
 * 发现取消后，doWork仍然执行完毕 （现在只剩一个办法 最后执行玩的时候判断isActivity来确定是否提交任务 ）虽然捕获到了异常但是要等到 doWork执行完了才能捕获
 * [利用协程取消做代码清理](https://stackoverflow.com/questions/57011421/how-can-i-execute-cleanup-code-in-my-coroutineworker-when-onstopped-is-final)
 */
class StoppedCoroutineWorker(context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
//        try {
//            println("StoppedCoroutineWorker------------doWork1")
//            repeat(Int.MAX_VALUE) {}
//            repeat(Int.MAX_VALUE) {}
//            println("StoppedCoroutineWorker------------doWork2")
//            Result.success()
//        } catch (e: CancellationException) {
//            println("StoppedCoroutineWorker------------catch")
//            Result.failure()
//        } finally {
//            println("StoppedCoroutineWorker------------finally")
//        }


        return coroutineScope {
            val job = async {
//--------------------在协程里面try catch 捕获不到取消异常
//                try {
//                    println("StoppedCoroutineWorker------------doWork1")
//                    repeat(Int.MAX_VALUE) {}
//                    repeat(Int.MAX_VALUE) {}
//                    println("StoppedCoroutineWorker------------doWork2")
//                    Result.success()
//                } catch (e: CancellationException) {
//                    println("StoppedCoroutineWorker--------CancellationException")
//                    Result.failure()
//                } finally {
//                    println("StoppedCoroutineWorker--------finally")
//                }
//-----------------------------------------------------------------------------------------
                println("StoppedCoroutineWorker------------doWork1")
                repeat(Int.MAX_VALUE) {}
                repeat(Int.MAX_VALUE) {}
                println("StoppedCoroutineWorker------------doWork2")
                Result.success()
            }

            //用这种方式可以捕获到取消异常
            job.invokeOnCompletion { exception: Throwable? ->
                when (exception) {
                    is CancellationException -> {
//                        Log.e(TAG, "Cleanup on completion", exception)
                        // cleanup on cancellations
                        println("StoppedCoroutineWorker ------- CancellationException")
                    }
                    else -> {
                        println("StoppedCoroutineWorker ------- else")
                    }
                }
            }
            job.await()
        }
    }
}