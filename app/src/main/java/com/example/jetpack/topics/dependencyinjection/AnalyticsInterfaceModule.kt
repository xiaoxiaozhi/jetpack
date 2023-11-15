package com.example.jetpack.bestpractice.dependencyinjection

import com.example.jetpack.topics.dependencyinjection.AnalyticsServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 1. 接口依赖注入
 * 绑定到SingletonComponent指AnalyticsInterfaceModule的实例绑定到了SingletonComponent。作用范围是Application。AnalyticsService如果不用@Singleton每次会返回新实例
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsInterfaceModule {
    @Binds
    abstract fun bindAnalyticsService(
        analyticsServiceImpl: AnalyticsServiceImpl
    ): AnalyticsService

}

