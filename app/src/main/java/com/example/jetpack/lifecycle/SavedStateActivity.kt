package com.example.jetpack.lifecycle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivitySavedStateBinding

/**
 * 保存状态在自定义类中
 * 1. Activity 和 Fragment 提供，SavedStateRegistry帮助保存UI状态
 * 2. 在活动和碎片的onCreate() 方法中注册 保存类：SavedStateRegistry.SavedStateProvider，或者传入生命周期在ON_CREATE后注册
 *
 * 3.
 */
class SavedStateActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySavedStateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedStateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SearchManager(this)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }
}