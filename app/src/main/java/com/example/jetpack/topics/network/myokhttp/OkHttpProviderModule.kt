package com.example.jetpack.topics.network.myokhttp

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OkHttpProviderModule {
    @Singleton
    @Provides
    fun providerCline(@ApplicationContext context: Context): OkHttpClient {
        // 10 MiB
        return OkHttpClient.Builder().cache(Cache(directory = context.cacheDir, maxSize = 10L * 1024L * 1024L))
            .connectTimeout(5, TimeUnit.SECONDS).writeTimeout(5, TimeUnit.SECONDS).readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.SECONDS)
//            .retryOnConnectionFailure(true)//异常重试 默认开
//            .followRedirects(true)//重定向 默认开
            .build()
//        return OkHttpClient()
    }
}