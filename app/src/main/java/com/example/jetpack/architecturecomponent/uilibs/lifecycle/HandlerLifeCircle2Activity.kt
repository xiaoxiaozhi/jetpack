package com.example.jetpack.architecturecomponent.uilibs.lifecycle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.*
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityHandlerLifeCircle2Binding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *
 */
class HandlerLifeCircle2Activity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityHandlerLifeCircle2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHandlerLifeCircle2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button1.setOnClickListener(this)
        lifecycle.addObserver(LifecycleEventObserver { lifecycleOwner: LifecycleOwner, event: Lifecycle.Event ->
            lifecycle.addObserver(LifecycleEventObserver { lifecycleOwner: LifecycleOwner, event: Lifecycle.Event ->
                println("HandlerLifeCircle2Activity----event-----${event}()----currentState----${lifecycleOwner.lifecycle.currentState}")

            })
        })
        var num = 0
//        lifecycle.coroutineScope.launch {
//            while (true) {
//                delay(1000)
//                println(num++)
//            }
//        } //不用生命周期感知函数， 执行结果对Activity的生命周期不敏感，出发后除非lifecycle销毁，否则都会执行
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    while (true) {
                        delay(1000)
                        println(num++)
                    }
                } finally {// 处于比 STARTED 小的生命周期时候会执行这段代码
                    println("finally---${lifecycle.currentState}")
                }

            }
        }//对组件的生命周期敏感，只有处于STARTED时候才会执行，其它状态取消
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button1 -> startActivity(Intent(this, TestActivity::class.java))
        }

    }
}