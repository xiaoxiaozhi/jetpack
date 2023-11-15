package com.example.jetpack.topics.connect

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.jetpack.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

/**
 * 1.确定设备的网络类型，这还不能判断能否联网，需要调用network.isConnected进一步确定是否连接到这些网络（）
 * 实际测试发现，只要是连上wifi 或者 基站 isConnected都是 true ，所以这个只能判断连上网络，能不能访问互联网，不能确定
 * 2.网上的方式检查是否连接上了互联网，原理是ping 百度
 * 3.[监听网络事件](https://developer.android.com/training/basics/network-ops/reading-network-state?hl=zh-cn#listening-events)
 *   ConnectivityManager.registerDefaultNetworkCallback(NetworkCallback) 默认网络发生变化监听事件，看原文吧有点长
 *   ConnectivityManager.registerNetworkCallback(NetworkCallback)
 */
class ConnectivityActivity : AppCompatActivity() {
    companion object {
        const val TAG = "ConnectivityActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connectivity)
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var isWifiConn: Boolean = false
        var isMobileConn: Boolean = false
        //1. connMgr.allNetworks 遍历获取设备的所有网络
        connMgr.allNetworks.forEach { network ->
            connMgr.getNetworkInfo(network)?.apply {

                Log.i(TAG, "type---$type ${typeName} ${ this.detailedState.name}")
                if (type == ConnectivityManager.TYPE_WIFI) {
                    isWifiConn = isWifiConn or isConnected
                }
                if (type == ConnectivityManager.TYPE_MOBILE) {
                    isMobileConn = isMobileConn or isConnected
                }
            }
        }
        Log.d(TAG, "Wifi connected: $isWifiConn")//
        Log.d(TAG, "Mobile connected: $isMobileConn")
        sdasd()
//        val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
//        networkInfo?.isConnected  //替代遍历，感觉没什么鸟用

    }

    /**
     * 2.
     * [用ping的方式检查是否连上了互联网，来自网络](https://www.jianshu.com/p/1b542cece605)
     */
    fun sdasd() {
        newSingleThreadContext("connect").use {
            CoroutineScope(it).launch {
                val runtime = Runtime.getRuntime()
                val p = runtime.exec("ping -c 3 www.baidu.com")
                p.waitFor().apply {
                    Log.i(TAG, "Process:$this")
                }
            }
        }
//        lifecycleScope.async(Dispatchers.Default) {
//            val runtime = Runtime.getRuntime()
//            val p = runtime.exec("ping -c 3 www.baidu.com")
//            p.waitFor().apply {
//                Log.i("Avalible", "Process:$this")
//            }
//        }.await()
    }
}