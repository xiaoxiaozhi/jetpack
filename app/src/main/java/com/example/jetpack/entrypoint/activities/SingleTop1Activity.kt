package com.example.jetpack.entrypoint.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.databinding.ActivitySingleTop1Binding

class SingleTop1Activity : AppCompatActivity() {
    private lateinit var binding: ActivitySingleTop1Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleTop1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button1.setOnClickListener {
            startActivity(
                Intent().setClass(
                    this@SingleTop1Activity,
                    SingleTopActivity::class.java
                )
            )
        }
    }
}