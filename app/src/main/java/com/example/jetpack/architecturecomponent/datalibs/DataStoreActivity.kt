package com.example.jetpack.architecturecomponent.datalibs

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.jetpack.R
import com.example.jetpack.dataStore
import com.example.jetpack.databinding.ActivityDataStoreBinding
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Jetpack:DataStore必知的几个优点和 sp 比较  https://zhuanlan.zhihu.com/p/343202145
 * https://mp.weixin.qq.com/s/vTnY63XajWJ-BkXLoM99Hw
 * -----------1. 从DataStore中存取数据-----------------------
 * -----------2. 从 SharedPreferences 迁移到 Preferences DataStore------------------------
 * -----------3. Proto DataStore----------------------------
 *
 */
class DataStoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataStoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //1. 存取数据
        val EXAMPLE_COUNTER = intPreferencesKey("example_counter")//还有stringPreferencesKey
        val EXAMPLE_COUNTER1 = intPreferencesKey("example_counter1")

        lifecycleScope.launch {
            dataStore.edit {
                val currentCounterValue = it[EXAMPLE_COUNTER] ?: 0
                it[EXAMPLE_COUNTER] = currentCounterValue + 1
                it[EXAMPLE_COUNTER1] = currentCounterValue + 2
            }
            dataStore.data.collect {
                println("collect-----pre $it")
                println("collect-----${it[EXAMPLE_COUNTER1]}") //通过打印可知，Flow里面只有一个 Preferences
            }
//            val exampleCounterFlow: Flow<Int> = dataStore.data
//                .map {
//                    // No type safety.
//                    it[EXAMPLE_COUNTER1] ?: 0
//                }
//            exampleCounterFlow.collect {
//                println("dataStor get $it")
//            }
        }


    }
}