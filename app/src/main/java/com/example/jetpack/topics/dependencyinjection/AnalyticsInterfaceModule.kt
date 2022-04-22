package com.example.jetpack.bestpractice.dependencyinjection

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Qualifier

/**
 * 1. 接口依赖注入
 */
@Module
@InstallIn(ApplicationComponent::class)
abstract class AnalyticsInterfaceModule {
    @Binds
    abstract fun bindAnalyticsService(
        analyticsServiceImpl: AnalyticsServiceImpl
    ): AnalyticsService

}

