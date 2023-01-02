package com.example.jetpack.topics.network.myretrofit.di

import com.example.jetpack.topics.network.myretrofit.DataRepository
import com.example.jetpack.topics.network.myretrofit.NetworkDataSource
import com.example.jetpack.topics.network.myretrofit.RetrofitDataSource
import com.example.jetpack.topics.network.myretrofit.RetrofitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataBinds {
    @Binds
    fun bindToNetworkSource(impl: RetrofitDataSource): NetworkDataSource

//    @Binds //nowinandroid 的写法
//    fun RetrofitDataSource.bind():NetworkDataSource

    @Binds
    fun bindToDataRepository(impl: RetrofitRepository): DataRepository
}