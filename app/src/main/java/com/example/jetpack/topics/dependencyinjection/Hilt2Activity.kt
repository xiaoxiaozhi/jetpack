package com.example.jetpack.topics.dependencyinjection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R
import com.example.jetpack.bestpractice.dependencyinjection.AnalyticsInterfaceAdapter
import com.example.jetpack.bestpractice.dependencyinjection.AnalyticsProviderAdapter
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 测试 SingletonComponent 作用域 和文档说的一样，如果想每次返回的实例都是单例请假@Singleton 注释
 */
@AndroidEntryPoint
class Hilt2Activity : AppCompatActivity() {
    //note 组件SingletonComponent的作用域是@Singleton，本例用了AnalyticsInterfaceModule 做测试，如果能打印 hashCode()就可以发现还是一个。
    @Inject
    lateinit var analytics: AnalyticsInterfaceAdapter

    @Inject
    lateinit var providerAdapter: AnalyticsProviderAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hilt2)
        println("Hilt2Activity----------------")
        analytics.hash()
        providerAdapter.data()
    }
}