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
import com.example.jetpack.databinding.ActivityFlagNewTaskBinding

class FlagNewTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFlagNewTaskBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlagNewTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(LifecycleEventObserver { lifecycleOwner: LifecycleOwner, event: Lifecycle.Event ->
            println("FlagNewTaskActivity-----$event-----${this@FlagNewTaskActivity.hashCode()}")
        })
        with(getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager) {
            println(
                "FlagNewTaskActivity---id---${getRunningTasks(1).get(0)?.taskId}----top---${
                    getRunningTasks(
                        1
                    ).get(0).topActivity?.className
                }"
            )
            println(
                "FlagNewTaskActivity---id---${getRunningTasks(1).get(0)?.taskId}----base--${
                    getRunningTasks(
                        1
                    ).get(0).baseActivity?.className
                }"
            )
        }
        binding.button1.setOnClickListener {
            startActivity(Intent(this, FlagNewTask1Activity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("FlagNewTaskActivity---onNewIntent")
    }
}