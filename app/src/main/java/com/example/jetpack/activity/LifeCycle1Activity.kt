package com.example.jetpack.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R

class LifeCycle1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_life_cycle1)
    }

}