package com.example.jetpack.architecturecomponent.uilibs.lifecycle.livedata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R

/**
 * 只用做跳转目的地，测试生命周期，没有其它作用
 */
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test2)
    }
}