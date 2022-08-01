package com.example.jetpack.topics.userinterface.appbar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R

class ToolBar1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tool_bar1)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}