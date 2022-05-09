package com.example.jetpack.architecturecomponent.datalibs.work

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.work.WorkManager
import com.example.jetpack.R

/**
 * WorkManager 配置
 * [使用 ContentProvider 无侵入获取 Context](https://juejin.cn/post/6887980244389593096)
 */
class WorkManagerActivity1 : AppCompatActivity() {
    lateinit var workManager: WorkManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker_manager1)
        workManager = WorkManager.getInstance(this)

    }
}