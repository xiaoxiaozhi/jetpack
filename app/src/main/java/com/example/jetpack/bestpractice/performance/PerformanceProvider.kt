package com.example.jetpack.bestpractice.performance

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PerformanceProvider {
    @Singleton
    @Provides
    fun providerPerformance(): PerformanceMonitor {
        return PerformanceMonitor()
    }
}