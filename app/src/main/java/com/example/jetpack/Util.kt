package com.example.jetpack

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

class Util {
    companion object {
        fun log(message: String) = println("[${Thread.currentThread().name}] $message")
    }
}

//TODO 什么时候执行