package com.example.jetpack.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityFlagNewTask1Binding

class FlagNewTask1Activity : AppCompatActivity() {
    private lateinit var binding: ActivityFlagNewTask1Binding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlagNewTask1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        with(getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager) {
            println(
                "FlagNewTask1Activity---id---${getRunningTasks(1).get(0)?.taskId}----top---${
                    getRunningTasks(
                        1
                    ).get(0).topActivity?.className
                }"
            )
            println(
                "FlagNewTask1Activity---id---${getRunningTasks(1).get(0)?.taskId}----base--${
                    getRunningTasks(
                        1
                    ).get(0).baseActivity?.className
                }"
            )
        }
        binding.button1.setOnClickListener {
            startActivity(Intent().apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                addFlags()
                setClass(this@FlagNewTask1Activity, FlagNewTaskActivity::class.java)
            })
        }
    }
}