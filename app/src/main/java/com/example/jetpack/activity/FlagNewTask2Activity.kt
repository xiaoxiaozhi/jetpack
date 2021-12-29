package com.example.jetpack.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityFlagNewTask2Binding
import com.example.jetpack.databinding.ActivitySingleTask2Binding

class FlagNewTask2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityFlagNewTask2Binding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlagNewTask2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(LifecycleEventObserver { lifecycleOwner: LifecycleOwner, event: Lifecycle.Event ->
            println("FlagNewTask2Activity----$event")
        })
        with(getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager) {
            println(
                "FlagNewTask2Activity---id---${getRunningTasks(1).get(0)?.taskId}----top---${
                    getRunningTasks(
                        1
                    ).get(0).topActivity?.className
                }"
            )
            println(
                "FlagNewTask2Activity---id---${getRunningTasks(1).get(0)?.taskId}----base--${
                    getRunningTasks(
                        1
                    ).get(0).baseActivity?.className
                }"
            )
        }
        binding.button1.setOnClickListener {
            startActivity(Intent().apply {
                println("FlagNewTask2Activity---button1---click")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setClass(this@FlagNewTask2Activity, FlagNewTask1Activity::class.java)
            })
        }
        binding.button2.setOnClickListener {
            startActivity(Intent().apply {
                println("FlagNewTask2Activity---button2---click")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setClass(this@FlagNewTask2Activity, FlagNewTaskActivity::class.java)
            })
        }
        binding.button3.setOnClickListener {
            startActivity(Intent().apply {
                println("FlagNewTask2Activity---button3---click")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setClass(this@FlagNewTask2Activity, FlagNewTask3Activity::class.java)
            })
        }
    }
}