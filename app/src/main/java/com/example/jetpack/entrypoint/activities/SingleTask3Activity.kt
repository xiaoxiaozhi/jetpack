package com.example.jetpack.entrypoint.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.databinding.ActivitySingleTask3Binding

/**
 * TODO startActivity可以用kotlin提供的扩展函数代替 怎么用
 */
class SingleTask3Activity : AppCompatActivity() {
    private lateinit var binding: ActivitySingleTask3Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleTask3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.setOnClickListener {
            startActivity(Intent(this@SingleTask3Activity, SingleTaskActivity::class.java))
        }
    }
}