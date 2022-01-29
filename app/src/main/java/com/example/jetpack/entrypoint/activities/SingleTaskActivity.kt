package com.example.jetpack.entrypoint.activities

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.jetpack.databinding.ActivitySingleTaskBinding

class SingleTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySingleTaskBinding
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(LifecycleEventObserver { lifecycleOwner: LifecycleOwner, event: Lifecycle.Event ->
            println("SingleTaskActivity-----${event.name}")
        })
        binding.button1.setOnClickListener {
            startActivity(
                Intent().setClass(
                    this@SingleTaskActivity,
                    SingleTask1Activity::class.java
                )
            )
        }
        binding.button2.setOnClickListener {
            startActivity(Intent().apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                component = ComponentName.createRelative(
                    "com.example.myapplication",
                    "com.example.myapplication.ThirdSingleTaskActivity"
                )
            })
        }
        with(getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager) {
            println("SingleTaskActivity---id---${getRunningTasks(1).get(0)?.taskId}----top---${getRunningTasks(1).get(0).topActivity?.className}")
            println("SingleTaskActivity---id---${getRunningTasks(1).get(0)?.taskId}----base--${getRunningTasks(1).get(0).baseActivity?.className}")
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("SingleTaskActivity-----onNewIntent------")
    }
}