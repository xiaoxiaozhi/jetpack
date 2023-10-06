package com.example.jetpack.topics.service

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.example.jetpack.R
import com.google.android.material.internal.ContextUtils.getActivity

class ForegroundActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foreground)
    }
}