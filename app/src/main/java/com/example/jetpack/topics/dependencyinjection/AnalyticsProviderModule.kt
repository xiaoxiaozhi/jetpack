package com.example.jetpack.bestpractice.dependencyinjection

import android.content.Context
import androidx.room.Room
import com.example.jetpack.topics.appdatafiles.room.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AnalyticsProviderModule {
    @Singleton
    @Provides
    fun provideMusicDB(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context, AppDatabase::class.java, "jetpack"
        ).build()
    }

}