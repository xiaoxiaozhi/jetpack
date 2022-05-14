package com.example.jetpack.bestpractice.dependencyinjection

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

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

