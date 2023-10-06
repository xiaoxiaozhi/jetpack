package com.example.jetpack.test

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil.setContentView
import com.bumptech.glide.Glide
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityTest3Binding
import com.google.common.util.concurrent.ServiceManager

class TestActivity : AppCompatActivity() {
    lateinit var binding: ActivityTest3Binding
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_test3)
        Thread() {
            println("Thread name1 ${Thread.currentThread().name}")
            runOnUiThread {
                println("Thread name2 ${Thread.currentThread().name}")
                Glide.with(this).load(resources.getDrawable(R.drawable.add_24, null)).into(binding.img)
            }
        }.start()
    }
}