package com.example.rxjavatest

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.example.rxjavatest.databinding.MainActivityBinding
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins

/**
 * 如果 观察者没有 复写 onError方法，就会报错 https://github.com/ReactiveX/RxJava/wiki/Error-Handling
 */
class MainActivity : ComponentActivity() {
    lateinit var binding: MainActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.main_activity)
        binding.button1.setOnClickListener {
            Observable.create<String> {
                it.onNext("打车")
//                it.onError(Exception("it's a mistake1"))
//                it.onError(Exception("it's a mistake2"))
                throw Exception("it's a mistake1")
                it.onComplete()
            }
//                .subscribe({ println("$it") })
//                .subscribe({ println("$it") }, { "exception---${it.message}" }, { println("完成") })
                .subscribe({ println("$it") }, Throwable::printStackTrace, { println("完成") })
        }
//        RxJavaPlugins.setErrorHandler {
//            println("it------${it.message}")
//        }
    }
}