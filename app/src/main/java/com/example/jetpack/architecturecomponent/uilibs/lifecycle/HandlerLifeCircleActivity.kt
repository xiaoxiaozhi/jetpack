package com.example.jetpack.architecturecomponent.uilibs.lifecycle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.*
import com.example.jetpack.R
import com.example.jetpack.Util
import com.example.jetpack.databinding.ActivityLifeCircleBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * -----------------[Handling Lifecycles](https://developer.android.google.cn/topic/libraries/architecture/lifecycle)-------------------------------------------------------
 * 1. Lifecycle是一个包含了组件生命周期状态的类，并允许添加观察者，观察组件的生命周期。(继承DefaultLifecycleObserver的方式实现一个观察者具体代码看MyObserver)
 *    生命周期使用两个主要的枚举来跟踪其相关组件的生命周期状态:
 *    1.1 Event:activity 或者 Fragment等 的回调事件
 *    1.2 State:activity 或者 Fragment当前的状态
 *    ![事件和状态图](https://developer.android.google.cn/static/images/topic/libraries/architecture/lifecycle-states.svg)
 *    androidx.lifecycle 包下面的生命周期感知组件有：ViewModel、Flow、Lifecycle、LiveData (Flow怎么感知的？？？)
 *    1.3 如果 Lifecycle 现在未处于良好的状态，则应避免调用某些回调
 *        这个方法最好在DefaultLifecycleObserver 中调用。实验发现 lifecycle.currentState是迟于 Activity的 生命周期回调方法。 具体使用过程查看 MyObserver
 *        lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) 状态 >= STARTED 返回true
 *        看事件和状态图两行状态一致，只需要看第一行从左到右横着看，举个例子，大于 STARTED 只有  RESUMED。大于CREATED 的状态有 STARTED 和 RESUMED
 *    attention:实验发现 LifecycleEventObserver 这个观察者接收不到onDestroy事件，我怀疑是AppCompatActivity没有实现，因为我自定义类继承了Activity和LifecycleOwner就能接收到
 * 2. LifecycleOwner 是单一方法接口，表示类具有 Lifecycle。它具有一种方法（即 getLifecycle()），如果您尝试管理整个应用进程的生命周期，请参阅 ProcessLifecycleOwner:  ProcessLifecycleOwner.get().lifecycle.addObserver(object :DefaultLifecycleObserver{})
 *    Activity和Fragment都实现了LifecycleOwner，任何自定义应用类均可实现 LifecycleOwner 接口。
 *    自定义 LifecycleOwner 查看代码 HandlerLifeCircle1Activity
 * 3. 处理ON_STOP事件 TODO 没看懂
 *
 *-----------------[Use Kotlin coroutines with lifecycle-aware components](https://developer.android.google.cn/topic/libraries/architecture/coroutines)-------------------------
 * 4. 生命周期组件为协程提供支持
 *    生命周期组件(ViewModel、Lifecycle、Flow)的协程作用域
 *    4.1 viewModelScope：定义在ViewModel上的扩展函数。如果清除了 ViewModel，则在此范围内启动的任何协同程序都将自动取消。
 *        vm实例.viewModelScope.launch {} 一般写在ViewModel里面
 *    4.2 LifecycleScope: 定义在LifecycleOwner(AppCompatActivity、Fragment)上的的扩展函数,当组件被销毁时，在此范围内启动的任何协同程序都将被取消。
 *        lifecycleOwner.lifecycleScope 实际调用的是lifecycle.coroutineScope
 *    4.3 repeatOnLifecycle：Lifecycle 和 LifecycleOwner提供了一种方法，在生命周期至少处于某种状态时开始执行代码块，并在处于另一种状态时取消代码块。repeatOnLifecycle本质是coroutineScope{} 是一个挂起点函数
 *        LifecycleOwner.repeatOnLifecycle 实际调用的是 lifecycle.repeatOnLifecycle 。 TODO repeatOnLifecycle执行完成后发现后续的代码不执行了
 *        具体代码查看 HandlerLifeCircle2Activity 类 或者 下面也有 TODO 我自定义一个生命周期观察者，在对应的生命周期中也能实现该功能，所以repeatOnLifecycle是不是对生命周期观察者的简化???
 *    4.4 Flow.flowWithLifecycle(): 在Flow上感知生命周期
 *        flow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect {} 类似于repeatOnLifecycle
 *    4.5 挂起生命周期感知组件的协程
 *        whenX{} 生命周期至少处于某种状态时开始执行代码块，并在处于另一种状态时暂停代码块。与repeatOnLifecycle相比，不是重新执行代码块而是继续执行。
 *        TODO 测试发现whenX{}运行在主线程中而不报错。withContext设计目的就是要切换线程。我不理解为什么仍要运行在主线程。
 *        lifecycleScope.launchWhenX和LifecycleOwner.whenX最后都调用的是 lifecycle.whenX查看源码发现 本质是 withContext，所以它会挂起底层协程，直到代码块执行完毕
 *        attention：repeatOnLifecycle{收集流}和whenX{收集流}。当生命周期被暂停STOPPED，由于后者暂停协程而不是取消协程，所以上游流在后台保持活动，repeatOnLifecycle可能会产生新流并浪费资源。
 */
class HandlerLifeCircleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLifeCircleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLifeCircleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //1. 添加观察者感知Activity的生命周期
        lifecycle.addObserver(MyObserver())
        //1.1 和 1.2 感知组件的事件和状态
        lifecycle.addObserver(LifecycleEventObserver { lifecycleOwner: LifecycleOwner, event: Lifecycle.Event ->
            println("HandlerLifeCircleActivity event-----${event}()----currentState----${lifecycleOwner.lifecycle.currentState}")
        })
        //2. LifecycleOwner
        binding.button1.setOnClickListener {
            startActivity(Intent(this@HandlerLifeCircleActivity, HandlerLifeCircle1Activity::class.java))
        }

        binding.button2.setOnClickListener {
            startActivity(Intent(this, HandlerLifeCircle2Activity::class.java))
        }
        binding.button3.setOnClickListener {
            startActivity(Intent(this, HandlerLifeCircle3Activity::class.java))
        }

        lifecycleScope.launch() {
            //4.3 repeatOnLifecycle
            launch() {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {// 再度处于STARTED状态时计数重新开始
                    try {
                        repeat(10) {
                            delay(1000)
                            Util.log("lifecycle.repeatOnLifecycle------$it")
                        }
                    } catch (e: Exception) {
                        Util.log("lifecycle.repeatOnLifecycle--------------${e.message}")
                    } finally {
                        Util.log("lifecycle.repeatOnLifecycle--------------finally")
                    }

                }
                Util.log("launch--------------dfgsd----------------")
            }

            //4.5
            whenStarted {
                repeat(10) {
                    delay(1000)
                    Util.log("whenStarted------$it")
                }
            }
            lifecycleScope.launchWhenStarted {
                try {
                    // Call some suspend functions.
                } finally {
                    // This line might execute after Lifecycle is DESTROYED.
                    if (lifecycle.currentState >= Lifecycle.State.STARTED) {
                        // Here, since we've checked, it is safe to run any
                        // Fragment transactions.
                    }
                }
            }
            Util.log("lifecycleScope.launch()---------end")
        }

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