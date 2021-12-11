package com.example.jetpack.lifecycle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.replace
import androidx.lifecycle.LifecycleOwner
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityLifeCycleBinding

/**
 * https://juejin.cn/post/6893870636733890574#heading-4 系列文章
 * 生命周期组件
 * ----------------Activity、Fragment添加生命周期组件------------------------------
 * 1. 一种常见的模式是在活动和片段的生命周期方法中实现依赖组件的操作。
 *    然而，这种模式会导致代码的组织不良和错误的扩散。通过使用具有生命周期感知的组件，
 *    您可以将依赖组件的代码从生命周期方法中移出并转移到组件本身中。
 * 2. 如果我们需要执行长期运行的操作，例如onStart()中执行一些配置的检查由于时间过长此时活动或片段已经停止，再继续执行就会报错
 * 3. 继承LifecycleOwner 获得生命周期感知，也可以设置对某一周期的监听
 * ---------------普通Class、自定义组建 类添加生命周期组件------------------------------------------------------
 * 1. 继承DefaultLifecycleObserver 。
 *    class MyObserver : DefaultLifecycleObserver
 *    myLifecycleOwner.getLifecycle().addObserver(MyObserver())
 * 2.
 * ---------------APP进程生命周期ProcessLifecycleOwner-------------------------------------------------------
 * ---------------getViewLifecycleOwner Fragment界面的生命周期？？？？----------------------------------------
 */
class LifeCycleActivity : AppCompatActivity(), LifecycleOwner {
    private lateinit var binding: ActivityLifeCycleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLifeCycleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifeCycleButton.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter,
                    R.anim.exit,
                    R.anim.pop_enter,
                    R.anim.pop_exit
                )
                .replace<LifeCycleFragment>(R.id.lifeCycle_container)
                .addToBackStack(null)
                .setReorderingAllowed(true)
                .commit()
        }
    }

    public override fun onStart() {
        super.onStart()
//        Util.checkUserStatus { result ->
//            // what if this callback is invoked AFTER activity is stopped?
//            if (result) {
//                myLocationListener.start()
//            }
//        }
    }

}