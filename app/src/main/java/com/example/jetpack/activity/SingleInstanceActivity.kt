package com.example.jetpack.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.jetpack.R

/**
 *
 */
class SingleInstanceActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_instance)
        with(getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager) {
            println("SingleInstanceActivity---id---${getRunningTasks(1).get(0)?.taskId}----top---${getRunningTasks(1).get(0).topActivity?.className}")
            println("SingleInstanceActivity---id---${getRunningTasks(1).get(0)?.taskId}----base--${getRunningTasks(1).get(0).baseActivity?.className}")
        }
        println("")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("SingleInstanceActivity------onNewIntent")//没执行
    }
}