package com.example.jetpack.bestpractice.dependencyinjection

import com.example.jetpack.topics.appdatafiles.room.AppDatabase
import javax.inject.Inject

class AnalyticsProviderAdapter @Inject constructor(private val database: AppDatabase) {
    fun data() {
        println("dataBase hash----${database.hashCode()}")
    }
}