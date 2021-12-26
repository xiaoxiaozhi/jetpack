package com.example.jetpack.activity

import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivitySingleTop1Binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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