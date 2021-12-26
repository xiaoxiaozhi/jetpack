package com.example.jetpack.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivitySingleTopBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SingleTopActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySingleTopBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleTopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.addObserver(LifecycleEventObserver { lifecycleOwner: LifecycleOwner, event: Lifecycle.Event ->
            println("SingleTopActivity-----${event.name}")

        })
        binding.button1.setOnClickListener {
            startActivity(
                Intent(this@SingleTopActivity, SingleTop1Activity::class.java)
            )
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("SingleTopActivity-----onNewIntent------")
    }
}