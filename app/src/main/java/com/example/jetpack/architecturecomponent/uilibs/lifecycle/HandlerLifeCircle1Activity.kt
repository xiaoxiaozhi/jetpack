package com.example.jetpack.architecturecomponent.uilibs.lifecycle

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.*
import com.example.jetpack.R

/**
 *
 */
class HandlerLifeCircle1Activity : Activity(), LifecycleOwner {
    private lateinit var lifecycleRegistry: LifecycleRegistry
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_handler_life_circle1)
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycle.addObserver(LifecycleEventObserver { lifecycleOwner: LifecycleOwner, event: Lifecycle.Event ->
            println("HandlerLifeCircle1Activity event-----${event}()----currentState----${lifecycleOwner.lifecycle.currentState}")
        })
    }

    public override fun onStart() {
        super.onStart()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun onResume() {
        super.onResume()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onPause() {
        super.onPause()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun onStop() {
        super.onStop()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}