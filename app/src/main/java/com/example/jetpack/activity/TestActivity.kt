package com.example.jetpack.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R

/**
 * 测试 活动
 * TODO 使用 ActivityScenario
 */
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
    }
}