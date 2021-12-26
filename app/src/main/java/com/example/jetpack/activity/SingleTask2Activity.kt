package com.example.jetpack.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivitySingleTask1Binding
import com.example.jetpack.databinding.ActivitySingleTask2Binding

class SingleTask2Activity : AppCompatActivity() {
    private lateinit var binding: ActivitySingleTask2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleTask2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setOnClickListener {
            startActivity(Intent(this@SingleTask2Activity, SingleTask3Activity::class.java))
        }
    }
}