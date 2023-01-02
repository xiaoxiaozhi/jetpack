package com.example.jetpack.topics.dependencyinjection.mydagger2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.MyApplication
import com.example.jetpack.R
import javax.inject.Inject

/**
 * MyComponent 的注入方法，参数不支持多态。两个Component注入一个依赖会报错。为避免报错要把两个component合二为一。
 * 在第一个@component(depenlies = [()])同时第二个component的注入方法改为返回被注入对象 例如Presenter providerPresenter();
 * @Module
 * @Single 单例对象，但不是全局单例只在注入对象中是单例。换个对象注入就会生成新的对象。要实现全局单例还要把 DaggerMyComponent.create()放在Application中
 * 官网教程
 * Dagger 主要给构造函数和字段注入依赖 @Inject constructor() 和 @Inject fields，Dagger也支持方法注入但是 构造函数以及参数注入是常用做法
 * Dagger也不是什么类都能被注入，缺少构造函数的接口不能被注入，第三方类不能被注入，Configurable objects must be configured!(这是什么？？？)
 * 对于不能被注入的情况，可以使用@Provides fun provideXXX() = 实例。要是这个提供注入的方法也依赖其它类呢？ fun provideXXX(object:Instance) = object；相当于做了一个转发，直接return 参数
 * 我们可以使用 @Binds bindXXX(instanceImp:Instance):Instance 方法来实现这种情况。(android retrofit教程直接说了 @Provides是提供第三方类的依赖，@Binds提供接口依赖。感觉dagger官网说的有问题)
 * 所有的@Provides @Bind 必须属于被@Module注释的类或者接口. 一般来说@Provides 注释的方法前缀provideXXX() @Binds注释的方法签注bindXXX() @Module 注释的类或接口后缀 XXXModule
 *
 */

class Dagger2Activity : AppCompatActivity() {
//    @Inject
//    lateinit var http: HttpObject

    //    @Inject
//    lateinit var http1: HttpObject  //如果@provider处不使用@Singleton 这里哈希值不一样说明产生了了两个
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dagger2)
//        DaggerMyComponent.create()// 为了实现全局单例， MyComponent放在Application里面
//            .injectActivity(this)

//        (application as MyApplication).component.injectActivity(this)// 实现全局单例
//        println("httpObject-----${http.hashCode()}")
    }
}