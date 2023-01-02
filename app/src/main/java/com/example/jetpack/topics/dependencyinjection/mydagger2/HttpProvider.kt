package com.example.jetpack.topics.dependencyinjection.mydagger2

import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

@Module
@DisableInstallInCheck
class HttpProvider {
    @Provides
    @Singleton
    fun providerHttp(): HttpObject {
        return HttpObject()
    }
}