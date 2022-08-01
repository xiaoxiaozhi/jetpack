package com.example.jetpack.topics.backgroundtask

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.jetpack.R
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutionException

/**
 * 返回异步计算的结果，允许添加监听如果计算已经完成立即返回，计算没有完成等待完成后返回
 * 1. 创建 ListenableFuture
 *    1.1 同步方法 要执行的方法不使用异步又想封装在ListenableFuture<>
 *    1.2 异步方法 使用协程扩展函数创建 ListenableFuture<>
 *    1.3 [将已有的 接口接入 ListenableFuture<>](https://developer.android.google.cn/reference/androidx/concurrent/futures/CallbackToFutureAdapter)
 * 2. 获取结果\
 *    2.1 异步获取结果
 *    2.2  Futures.addCallback() 不光获取ListenableFuture成功结果，还能获取失败结果
 * note： 引入 guava
 *        implementation "com.google.guava:guava:31.0.1-android"
 *        implementation "androidx.concurrent:concurrent-futures:1.1.0"
 *        implementation "org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.6.0"
 *
 * TODO Throwable Exception Error 查看java核心技术一卷
 */
class ListenableFutureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listenable_future)
        val future1: ListenableFuture<String> = lifecycleScope.future { "sds" }
        //1.1 同步
//        Futures.immediateFuture(getQueryResult()).get()
        //1.2 异步
        val future2 = lifecycleScope.future { getQueryResult() }
        //2.1 异步获取结果的几种形式
        future2.addListener({
            val str = future2.get()// 在协程中执行，结果返回再addListener第二个参数表示的线程
            println("${Thread.currentThread().name}]   str------------------$str")
        }, mainExecutor)

        lifecycleScope.launch(Dispatchers.Default) {
            println("get()----------${future2.get()}")// 挂起函数,在主线程调用会报错
        }
        lifecycleScope.launch(Dispatchers.Default) {
            println("await()----------${future2.await()}")
        }
        //2.2  Futures.addCallback()  获取结果
        val future3 = lifecycleScope.future { getThrowableResult() }
        Futures.addCallback(future3, object : FutureCallback<String> {
            override fun onSuccess(result: String?) {
                // handle success
                println("onSuccess------$result")
            }

            override fun onFailure(t: Throwable) {
                println("onFailure------${t.message}")
            }
        }, mainExecutor)

    }

}

private suspend fun getQueryResult(): String {
    //执行一些代码，最后返回结果
    delay(5000)
    return "123"
}

private suspend fun getThrowableResult(): String {
    delay(2000)
    throw Exception(Throwable("坏事了"))
    return "456"
}
