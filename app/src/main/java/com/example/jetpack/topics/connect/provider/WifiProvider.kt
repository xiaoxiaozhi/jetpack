package com.example.jetpack.topics.connect.provider

import android.content.Context
import com.example.jetpack.topics.connect.util.WifiUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WifiProvider {
    @Singleton
    @Provides
    fun provideWifi(@ApplicationContext context: Context): WifiUtil {
        return WifiUtil(context)
    }
}