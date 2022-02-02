package com.example.jetpack.topics.service

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
            startService(Intent(this@ServicePracticeActivity, ForegroundService::class.java))
        }

    }
}