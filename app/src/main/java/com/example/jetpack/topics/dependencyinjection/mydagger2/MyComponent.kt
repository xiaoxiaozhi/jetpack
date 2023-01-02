package com.example.jetpack.topics.dependencyinjection.mydagger2

import dagger.Component

@Component(modules = [(HttpProvider::class)])// 不用小括号包裹就找不到 HttpProvider
interface MyComponent {
    fun injectActivity(activity: Dagger2Activity)//参数不支持多态
}