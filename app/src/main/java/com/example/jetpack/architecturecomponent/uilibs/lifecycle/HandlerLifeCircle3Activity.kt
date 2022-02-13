package com.example.jetpack.architecturecomponent.uilibs.lifecycle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityHandlerLifeCircle2Binding
import com.example.jetpack.databinding.ActivityHandlerLifeCircle3Binding
import kotlinx.coroutines.delay

/**
 *
 */
class HandlerLifeCircle3Activity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityHandlerLifeCircle3Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHandlerLifeCircle3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button1.setOnClickListener(this)
        var num = 0
        lifecycleScope.launchWhenCreated {
            try {
                while (true) {
                    delay(1000)
                    println("num = ${++num}")
                }
            } finally {// lifecycle 销毁时会执这段段代码
                println("finally---${lifecycle.currentState}")
            }

        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button1 -> startActivity(Intent(this, TestActivity::class.java))
        }
    }
}