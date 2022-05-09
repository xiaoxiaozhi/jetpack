package com.example.jetpack.topics.dependencyinjection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R
import com.example.jetpack.bestpractice.dependencyinjection.AnalyticsInterfaceAdapter
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class Hilt2Activity : AppCompatActivity() {
    //note 组件ApplicationComponent的作用域是@Singleton，实际测试仍然会生城实例，存疑？
    @Inject
    lateinit var analytics: AnalyticsInterfaceAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hilt2)
        println("Hilt2Activity----------------")
        analytics.hash()
    }
}