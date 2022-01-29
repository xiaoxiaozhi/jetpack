package com.example.jetpack.topics.service

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R

class ServicePracticeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_practice)
        startService(Intent(this@ServicePracticeActivity, StartService::class.java))
    }
}