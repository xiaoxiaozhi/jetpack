package com.example.jetpack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.jetpack.databinding.ActivityMainBinding

/**
 * 1. viewBinding 在module层级gradle添加android{ buildFeatures {viewBinding true}}，如果不想布局文件生成绑定类在布局文件根布局添加 tools:viewBindingIgnore="true"
 * 2.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}