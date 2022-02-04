package com.example.jetpack.topics.service

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityServicePracticeBinding

class ServicePracticeActivity : AppCompatActivity() {
    lateinit var binding: ActivityServicePracticeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicePracticeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //1. 启动前台服务
        binding.button1.setOnClickListener {
            val intent: Intent =
                Intent(this@ServicePracticeActivity, ForegroundService::class.java).apply {
                    putExtra("opt", 0)
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //android 8+ 功能类类似startService 仍然要在 service的startCommand()方法内调用startForeground()
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
        //2. 移除前台服务
        binding.button2.setOnClickListener {
            val intent: Intent =
                Intent(this@ServicePracticeActivity, ForegroundService::class.java).apply {
                    putExtra("opt", 1)
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //android 8+ 功能类类似startService 仍然要在 service的startCommand()方法内调用startForeground()
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

    }
}