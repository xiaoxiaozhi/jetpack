package com.example.jetpack.entrypoint.activities

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.jetpack.databinding.ActivityFlagNewTask3Binding

class FlagNewTask3Activity : AppCompatActivity() {
    private lateinit var binding: ActivityFlagNewTask3Binding
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlagNewTask3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        with(getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager) {
            println(
                "FlagNewTask3Activity---id---${getRunningTasks(1).get(0)?.taskId}----top---${
                    getRunningTasks(
                        1
                    ).get(0).topActivity?.className
                }"
            )
            println(
                "FlagNewTask3Activity---id---${getRunningTasks(1).get(0)?.taskId}----base--${
                    getRunningTasks(
                        1
                    ).get(0).baseActivity?.className
                }"
            )
        }
        binding.button1.setOnClickListener {
            startActivity(Intent().apply {
                println("FlagNewTask3Activity---button1---click")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setClass(this@FlagNewTask3Activity, FlagNewTask2Activity::class.java)
            })
        }
    }
}