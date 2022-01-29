package com.example.jetpack.entrypoint.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.databinding.ActivitySingleTask1Binding

class SingleTask1Activity : AppCompatActivity() {
    private lateinit var binding: ActivitySingleTask1Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleTask1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setOnClickListener {
            startActivity(Intent(this@SingleTask1Activity, SingleTask2Activity::class.java))
        }
    }
}