package com.example.jetpack.architecturecomponent.datalibs.work

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.work.*
import com.example.jetpack.R
import kotlinx.coroutines.*

/**
 * 冲突解决策略 APPEND_OR_REPLACE 不管是启动工作链还是启动一个单独的工作都没达到文档说的效果.测试平台 android 23 和 31
 * (https://stackoverflow.com/questions/67467297/workmanager-existing-work-policy-append-or-replace-doesnt-behave-as-expected)
 */
class WorkerActivity1 : AppCompatActivity() {
    lateinit var workerManager: WorkManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker1)
        println("-------------WorkerActivity1------------------")
        workerManager = WorkManager.getInstance(this)
        val request1 =
            OneTimeWorkRequestBuilder<DelayWorker>().setInputData(workDataOf("data" to 5)).build()
                .apply {
                workerManager.getWorkInfoByIdLiveData(id).observe(
                    this@WorkerActivity1
                ){
                    if(it?.state ==WorkInfo.State.RUNNING ){
                        workerManager.enqueueUniqueWork(
                            "DelayWorker",
                            ExistingWorkPolicy.APPEND_OR_REPLACE,
                            OneTimeWorkRequest.from(DelayWorker::class.java)
                        )
                    }
                }
                    workerManager.enqueueUniqueWork(
                        "DelayWorker",
                        ExistingWorkPolicy.APPEND_OR_REPLACE,
                        this
                    )
                }

//        newSingleThreadContext("context").use {
//            runBlocking(it) {
//                println("sdasd")
//                delay(1000)
//                workerManager.enqueueUniqueWork(
//                    "DelayWorker",
//                    ExistingWorkPolicy.APPEND_OR_REPLACE,
//                    OneTimeWorkRequest.from(ChainWorker::class.java)
//                )
//            }
//        }
//        println("state == ${workerManager.getWorkInfoById(request1.id).get().state}")
//        while (workerManager.getWorkInfoById(request1.id).get().state == WorkInfo.State.RUNNING) {
//        }

    }
}