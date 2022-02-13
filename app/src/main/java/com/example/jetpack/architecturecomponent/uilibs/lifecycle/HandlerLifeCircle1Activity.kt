package com.example.jetpack.architecturecomponent.uilibs.lifecycle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.jetpack.R

/**
 * ![事件和状态图](https://developer.android.google.cn/images/topic/libraries/architecture/lifecycle-states.svg)
 * 两行状态一致，只需要看第一行从左到右横着看，举个例子，大于 STARTED 只有  RESUMED。大于CREATED 的状态有 STARTED 和 RESUMED
 */
class HandlerLifeCircle1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_handler_life_circle1)
        lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        lifecycle.addObserver(LifecycleEventObserver { lifecycleOwner: LifecycleOwner, event: Lifecycle.Event ->
            println("event-----${event}()----currentState----${lifecycleOwner.lifecycle.currentState}")

        })
        println("onCreate---${lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)}")
    }

    override fun onStart() {
        super.onStart()
        println("onStart---${lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)}")
    }

    override fun onPause() {
        super.onPause()
        println("onPause---${lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)}")
    }

    override fun onStop() {
        super.onStop()
        println("onStop---${lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)}")
    }
}