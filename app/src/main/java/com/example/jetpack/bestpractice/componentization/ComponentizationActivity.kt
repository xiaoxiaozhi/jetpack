package com.example.jetpack.bestpractice.componentization

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.jetpack.R
import java.lang.reflect.Proxy

/**
 * https://github.com/androidmalin/AndroidComponentPlugin
 * 参考这个仓库 实现了4.1 到 14的全部兼容
 * 插件化参考文章 https://juejin.cn/post/6973888932572315678
 * 如何加载并执行插件 Apk 中的代码（ClassLoader Injection）
 * 让系统能调用插件 Apk 中的组件（Runtime Container）
 * 正确识别插件 Apk 中的资源（Resource Injection）
 * 1.
 * 反射前先遍历方法和字段，实践发现ActivityTaskManager 内的方法都不能反射到，字段IActivityTaskManagerSingleton 可以反射到
 */
class ComponentizationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_componentization)

        //1. 加载
        Handler(Looper.getMainLooper()).postDelayed({
            LoadApkUtil().combineDex(this)
        }, 2 * 1000)
        //2.
        val atm = Class.forName("android.app.ActivityTaskManager")
        val iatmField = atm.getDeclaredField("IActivityTaskManagerSingleton").apply {
            isAccessible = true
            Log.i(TAG, "iatmField---${get(null)?.hashCode()}")
        }
        val singletonClazz = Class.forName("android.util.Singleton")
        singletonClazz.declaredMethods.takeIf { it.isNotEmpty() }?.forEach {
            Log.i(TAG, "$it ----")
        }?:Log.i(TAG,"singletonClazz 没有可以反射的方法")
        val iatm = singletonClazz.getDeclaredMethod("get").invoke(iatmField.get(null))
//        val iActivityTaskManagerProxy = Proxy.newProxyInstance(
//            Thread.currentThread().contextClassLoader, arrayOf(iActivityTaskManagerClazz),
//            IActivityInvocationHandler(iActivityTaskManager, context, subActivityClazz)
//        )
    }

    companion object {
        const val TAG = "ComponentizationActivity"
    }
}