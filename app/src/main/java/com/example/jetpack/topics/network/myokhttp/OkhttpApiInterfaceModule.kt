package com.example.jetpack.topics.network.myokhttp

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OkhttpApiInterfaceModule {
    @Binds
    abstract fun bindOkHttp(okHttpImpl: OkhttpApiImpl): OkhttpApi

}