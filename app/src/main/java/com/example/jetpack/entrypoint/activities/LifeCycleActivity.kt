package com.example.jetpack.entrypoint.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.jetpack.databinding.LifcycleBinding

/**
 * 1. 介绍各个生命周期回调
 * 2. 横竖屏切换、进程放置在后台Activity被杀死后系统会记住它的状态，并在导航回Activity的时候恢复
 * 3. 活动A被部分覆盖，市区焦点将调用onPause方法，完全覆盖将调用onPause 和 onStop 回调
 * */
class LifeCycleActivity : AppCompatActivity() {
    private lateinit var binding: LifcycleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LifcycleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startActivity(Intent().setClass(this, LifeCycle1Activity().javaClass))
        lifecycle.addObserver(LifecycleEventObserver { lifecycleOwner: LifecycleOwner, event: Lifecycle.Event ->
            println("Lifecycle.Event-----------$event-------------")
        })
        binding.text1.text = "123"
        println("onCreate-----${savedInstanceState?.get(TEXT)}")
    }

    /**
     * 在onStart 之后调用， 同 nCreate()回调 参数是同一个实例
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        println("onRestoreInstanceState----${savedInstanceState.get(TEXT)}")

    }

    /**
     * 界面此时开始显现
     */
    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * 如果在 多窗口失去焦点 或者 对话框部分覆盖 情况下Activity会调用此方法，此时活动UI还能看见，最好不要在这里回收UI
     */
    override fun onPause() {
        super.onPause()
    }

    /**
     * 在这里UI已经完全不可见 做一些释放资源、结束CPU操作的工作
     * 此时界面看不见
     */
    override fun onStop() {
        super.onStop()
    }

    /**
     * 从onStop返回将会调用onRestart
     */
    override fun onRestart() {
        super.onRestart()
        println("-----------onRestart--------")
    }

    /**
     * Activity被销毁前将调用onDestroy()
     * 1.在这里回收资源
     * 2.用户 按返回键、配置更改、在该活动中调用finish() 会触发onDestroy()
     * 3.横竖屏切换 也会触发onDestroy()，用isFinishing()区分这种情况
     */
    override fun onDestroy() {
        super.onDestroy()
        println("isFinishing-----${isFinishing}")//更改配置的isFinishing返回false其它返回true
    }

    /**
     * 重写后可以改变用户按back健的行为
     */
    override fun onBackPressed() {
        super.onBackPressed()
    }

    /**
     * 适合保存轻量简单的 数据
     * 活动因为压倒栈底而调用，在 onStop()方法调用后调用，用户 按back弹出Activity此时不再调用
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState?.apply {
            putString(TEXT, binding.text1.text?.toString())
        }
        println("-----onSaveInstanceState()---------")
    }

    companion object {
        val TEXT = "text1"
    }
}