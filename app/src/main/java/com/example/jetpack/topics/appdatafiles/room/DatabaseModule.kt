package com.example.jetpack.topics.appdatafiles.room

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
//    @Singleton
//    @Provides
//    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
//        return AppDatabase.getInstance(context)
//    }

    @Provides
    fun provideWordDao(appDatabase: AppDatabase): WordDao {
        println("DatabaseModule-----------------provideWordDao-----${hashCode()}")
        return appDatabase.wordDao()
    }
}