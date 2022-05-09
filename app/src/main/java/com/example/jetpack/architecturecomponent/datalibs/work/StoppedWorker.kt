package com.example.jetpack.architecturecomponent.datalibs.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class StoppedWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        println("StoppedWorker-------------doWorker1")
        for (i in 0..Int.MAX_VALUE){

        }
        for (i in 0..Int.MAX_VALUE){

        }
        println("StoppedWorker-------------doWorker2")
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        println("StoppedWorker------onStopped()")
    }

}