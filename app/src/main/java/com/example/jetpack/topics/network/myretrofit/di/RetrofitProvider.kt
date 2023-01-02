package com.example.jetpack.topics.network.myretrofit.di

import android.content.Context
import com.example.jetpack.topics.network.myretrofit.NetworkInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RetrofitProvider {
    @Singleton
    @Provides
    fun provideRetrofit(@ApplicationContext context: Context, client: OkHttpClient): NetworkInterface {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client1 = client.newBuilder().addInterceptor(interceptor).build()
        println("RetrofitProvider--------------provideRetrofit-----${hashCode()}")
        return Retrofit.Builder().baseUrl("https://reqres.in").addConverterFactory(GsonConverterFactory.create())
            .client(client1)
            .build().create(NetworkInterface::class.java)
    }
}