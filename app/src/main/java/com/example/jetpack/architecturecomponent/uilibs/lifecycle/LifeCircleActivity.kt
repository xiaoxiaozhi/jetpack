package com.example.jetpack.architecturecomponent.uilibs.lifecycle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R

/**
 * 1. 生命周期感知组件(LiveData、ViewMode)可响应另一个组件（如 Activity 和 Fragment）的生命周期状态的变化。
 *    监控生命周期：类可以通过实现 DefaultLifecycleObserver 并实现相应的方法来监控组件的生命周期状态。
 *    然后，调用 Lifecycle 类的 addObserver(自己实现的DefaultLifecycleObserver) 来添加观察器 具体代码看MyObserver
 * 2. LifecycleOwner 是单一方法接口，表示类具有 Lifecycle。它具有一种方法（即 getLifecycle()），如果您尝试管理整个应用进程的生命周期，请参阅 ProcessLifecycleOwner
 *    Activity和Fragment都实现了LifecycleOwner，回到下面的例子位置服务调用
 *    if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) { // connect if not connected } 能有效面壁面刚才的问题
 *    2.1 自定义 LifecycleOwner 查看代码 MyActivity
 * 3. 处理ON_STOP事件 TODO 没看懂
 *
 *
 */
class LifeCircleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_life_circle)

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
}