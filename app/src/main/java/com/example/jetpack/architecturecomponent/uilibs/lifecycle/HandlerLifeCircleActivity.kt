package com.example.jetpack.architecturecomponent.uilibs.lifecycle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityLifeCircleBinding

/**
 * 1. 生命周期感知组件(LiveData、ViewMode)可响应另一个组件（如 Activity 和 Fragment）的生命周期状态的变化。
 *    监控生命周期：类可以通过实现 DefaultLifecycleObserver 并实现相应的方法来监控组件的生命周期状态。
 *    然后，调用 Lifecycle 类的 addObserver(自己实现的DefaultLifecycleObserver) 来添加观察器 具体代码看MyObserver
 * 2. LifecycleOwner 是单一方法接口，表示类具有 Lifecycle。它具有一种方法（即 getLifecycle()），如果您尝试管理整个应用进程的生命周期，请参阅 ProcessLifecycleOwner
 *    Activity和Fragment都实现了LifecycleOwner，回到下面的例子位置服务调用
 *    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {  } 能有效面避免刚才的问题
 *    2.1 自定义 LifecycleOwner 查看代码 MyActivity
 * 3. 处理ON_STOP事件 TODO 没看懂
 * 4. LifecycleScope
 *    Lifecycle生命周期内启动的协程会在 Lifecycle 被销毁时取消
 *    您可以通过 lifecycle.coroutineScope 或 lifecycleOwner.lifecycleScope 属性访问 Lifecycle 的 CoroutineScope。
 *    4.1 可重复生命周期感知型协程
 *        Lifecycle 处于大于等于某个状态时开始执行代码块，并在小于这个状态时取消.本例处于STARTED ，比STARTED大的生命周期是RESUME
 *        ![生命周期顺序图，从左往右越来越大](https://developer.android.google.cn/images/topic/libraries/architecture/lifecycle-states.svg)
 *        具体代码查看 HandlerLifeCircle2Activity 类
 *    4.2 可挂起的生命周期感知协程
 *        至少处于whenLaunchX() 这个状态的时候才会启动协程，只有当 lifecycle 销毁时协程才会取消。在try{}finally{ lifecycle销毁时 finally代码会执行}
 *        具体代码查看 HandlerLifeCircle3Activity 类
 * TODO repeatOnLifecycle 是什么 LifecycleRegistry 是什么 要想知道这个需要自定义LifecycleOwner
 */
class HandlerLifeCircleActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLifeCircleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLifeCircleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button1.setOnClickListener {
            startActivity(
                Intent(
                    this@HandlerLifeCircleActivity, HandlerLifeCircle1Activity::class.java
                )
            )
        }
        binding.button2.setOnClickListener(this)
        binding.button3.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        // 传统的方法在Activity的生命周期回调方法中调用组件的相应方法。但在实际应用中，最终会管理太多组件调用，以响应生命周期的当前状态。
        // 管理多个组件会在生命周期方法（如 onStart() 和 onStop()）中放置大量的代码，这使得它们难以维护。
        // 正常情况下Util 做一些检查之后开启位置服务，极端情况下，还不等检查工作做完，onDestroy()已经被调用位置服务已经关闭
//        Util.checkUserStatus { result ->
//            if (result) {//// what if this callback is invoked AFTER activity is stopped?
//                myLocationListener.start()
//            }
//        }
        //androidx.lifecycle 软件包提供的类和接口可帮助您以弹性和隔离的方式解决这些问题。
    }

    override fun onDestroy() {
        super.onDestroy()
//        myLocationListener.stop()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button2 -> startActivity(Intent(this, HandlerLifeCircle2Activity::class.java))
            R.id.button3 -> startActivity(Intent(this, HandlerLifeCircle3Activity::class.java))
        }
    }
}