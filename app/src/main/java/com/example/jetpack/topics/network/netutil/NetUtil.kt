package com.example.jetpack.topics.network.netutil

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext

class NetUtil {
    suspend fun isAvailable() {
        CoroutineScope(Dispatchers.Default).async {
            val runtime = Runtime.getRuntime()
            val p = runtime.exec("ping -c 3 www.baidu.com")
            p.waitFor().apply {
                Log.i("Avalible", "Process:$this")
            }
        }.await()
    }
}