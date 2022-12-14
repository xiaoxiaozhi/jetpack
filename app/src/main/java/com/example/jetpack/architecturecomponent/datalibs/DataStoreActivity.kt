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
 * Jetpack DataStore 是一种数据存储解决方案，允许您使用协议缓冲区存储键值对或类型化对象。DataStore 使用 Kotlin 协程和 Flow 以异步、一致的事务方式存储数据。
 * 1. 为了正确使用 DataStore，请始终谨记以下规则：
 *    1.1 请勿在同一进程中为给定文件创建多个 DataStore 实例，否则会破坏所有 DataStore 功能。如果给定文件在同一进程中有多个有效的 DataStore，DataStore 在读取或更新数据时将抛出 IllegalStateException。
 *    1.2 DataStore 的通用类型必须不可变。更改 DataStore 中使用的类型会导致 DataStore 提供的所有保证失效，并且可能会造成严重的、难以发现的 bug。强烈建议您使用可保证不可变性、具有简单的 API 且能够高效进行序列化的协议缓冲区。
 *    1.3 切勿在同一个文件中混用 SingleProcessDataStore 和 MultiProcessDataStore。如果您打算从多个进程访问 DataStore，请始终使用 MultiProcessDataStore。
 * 2. DataStore 提供两种不同的实现：Preferences DataStore 和 Proto DataStore。
 *    2.1 Preferences DataStore
 *       使用 DataStore和Preferences类将简单的键值对保留在磁盘上。此实现不需要预定义的架构，也不确保类型安全。在build.gradle 引入依赖 implementation "androidx.datastore:datastore-preferences-core:1.0.0"
 *       在您的 Kotlin 文件顶层调用该实例一次，便可在应用的所有其余部分通过此属性访问该实例。这样可以更轻松地将 DataStore 保留为单例
 *       2.1.1 键类型函数
 *             需要使用键类型函数，为实例(DataStore<Preferences>)中的每个值定义一个键 例如 intPreferencesKey("example_counter")
 *       2.1.2 存数据
 *       2.1.3 读数据
 *    2.2 Proto DataStore
 *       将数据作为自定义数据类型的实例进行存储。此实现要求您使用协议缓冲区来定义架构，但可以确保类型安全。在build.gradle引入依赖 implementation "androidx.datastore:datastore-core:1.0.0"
 *       TODO 有点复杂，以后用到再总结
 * [协议缓冲区 protocol buffers](https://developers.google.com/protocol-buffers/docs/kotlintutorial)
 * protocol buffers 谷歌开发的编码方式用来序列化和检索结构化数据(例如电话薄 名字、号码、住址 这是一个结构化数据)，使用kotlinx.serialization也可以序列化但是C++和Python不兼容，
 * 使用xml人类能方便读取但是性能很差
 */
class DataStoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataStoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //2.1 创建
        //查看代码 SpreadFunction.kt 最后一行
        //2.1.1. 键类型函数
        val EXAMPLE_COUNTER = intPreferencesKey("example_counter")//还有stringPreferencesKey
        val EXAMPLE_COUNTER1 = intPreferencesKey("example_counter1")
        lifecycleScope.launch {
            //2.1.2 存数据
            dataStore.edit { settings ->
                val currentCounterValue = settings[EXAMPLE_COUNTER] ?: 0
                settings[EXAMPLE_COUNTER] = currentCounterValue + 1// 这一行是存储数据
            }
            //2.1.3 读数据
            val exampleCounterFlow: Flow<Int> = dataStore.data.map { preferences ->
                // No type safety.
                preferences[EXAMPLE_COUNTER] ?: 0
            }
            exampleCounterFlow.collect {
                println("读取数据-----pre $it")
            }
        }
    }
}