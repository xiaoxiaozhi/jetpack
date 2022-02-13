package com.example.jetpack.architecturecomponent.uilibs.lifecycle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R

/**
 * 跳转目的地测试生命周期用的
 */
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test1)
    }
}