package com.example.jetpack.bestpractice.mypractices

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil.setContentView
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityRootBinding


class RootActivity : AppCompatActivity() {
    lateinit var binding: ActivityRootBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataBinding = setContentView<ActivityRootBinding>(this, R.layout.activity_root)

    }
}