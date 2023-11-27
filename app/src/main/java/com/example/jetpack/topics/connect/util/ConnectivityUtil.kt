package com.example.jetpack.topics.connect.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.net.NetworkInterface
import java.util.Collections


/**
 * 参照 https://developer.android.google.cn/develop/connectivity/network-ops/reading-network-state?hl=zh-cn
 * 感觉前面几篇文章用的都是旧的api
 * 本文以该章节为起始点 https://developer.android.google.cn/develop/connectivity/network-ops/reading-network-state?hl=zh-cn
 *1. 查询当前网络状态
 *   一个Network可以同时拥有多个传输。例如，通过 Wi-Fi 和移动网络运行的 VPN。VPN 支持 Wi-Fi、移动网络和 VPN 传输
 *   connMgr.allNetworks 看日志发现它遍历了所有已连接网络，connManager.activeNetwork 只显示默认网络（wifi和蜂窝同时连接默认网络为wifi，如果wifi不能上网这时候默认网络是什么呢？）
 *   [默认网络概念](https://developer.android.google.cn/develop/connectivity/network-ops/reading-network-state?hl=zh-cn#listening-events)
 *   感觉各有所长为什么前者还要被废弃？
 *2. 监听网络事件
 *
 *
 * Network 类表示设备连接到的一个网络。如果网络连接中断，Network 对象将不再可用。即使设备之后重新连接到同一设备，新的 Network 对象也将表示新网络。
 * LinkProperties 对象包含有关网络链接的信息，例如 DNS 服务器列表、本地 IP 地址以及针对网络安装的网络路由。
 * NetworkCapabilities 对象包含有关网络属性的信息，例如传输方式（Wi-Fi、移动网络、蓝牙）以及网络能力。例如，您可以查询该对象，以确认网络是否能够发送彩信、是否通过强制门户接入，或者是否按流量计费。
 * mac 地址不好获取
 * ip 地址获取目前ip6和ip4混在一起还不知道根据什么判断分离
 * 指定网络发送请求 https://blog.csdn.net/dongziqi_csdn/article/details/111405109
 */
class ConnectivityUtil constructor(val context: Context) {
    private val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        //切换默认网络回调事件
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            wifiManager
            val caps = connManager.getNetworkCapabilities(network)
            val linkProperties = connManager.getLinkProperties(network)
            linkProperties?.linkAddresses?.forEach {
                Log.i(
                    TAG,
                    "address---${it.address}  flags---${it.flags} scope---${it.scope} 前缀长度${it.prefixLength}"
                )
            }
            Log.w(TAG, """
            onAvailable---
            ${caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN)?.takeIf { it }?.let { "vpn" } ?: ""}
            ${
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?.takeIf { it }?.let {
                    "wifi网络---${wifiManager.connectionInfo.ssid}\n " + "mac---${wifiManager.connectionInfo.macAddress}"
                } ?: ""
            }
            ${caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?.takeIf { it }?.let { "蜂窝网络" } ?: ""}
            是否按照流量计费---${
                caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)?.takeIf { it }?.let { "是" } ?: "否"
            }
            带宽---${caps?.linkDownstreamBandwidthKbps}
           
        """.trimIndent())
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
            //ip地址
            Log.w(TAG, "onLinkPropertiesChanged: ip---${linkProperties.linkAddresses}")
        }

        override fun onUnavailable() {
            super.onUnavailable()
            Log.w(TAG, "onUnavailable: ")
        }

        //对于通过 registerDefaultNetworkCallback() 注册的回调，onLost() 表示网络失去成为默认网络的资格
        override fun onLost(network: Network) {
            Log.i(TAG, "onLost: ")
            super.onLost(network)
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            Log.i(TAG, "onCapabilitiesChanged: ")
        }
    }

    init {
        //1-------------------
//        val currentNetwork = connManager.activeNetwork
//        val caps = connManager.getNetworkCapabilities(currentNetwork)
//        Log.i(TAG, """
//            1.查询当前网络传输方式
//            ${caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN)?.takeIf { it }?.let { "vpn" } ?: ""}
//            ${caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?.takeIf { it }?.let { "wifi网络" } ?: ""}
//            ${caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?.takeIf { it }?.let { "蜂窝网络" } ?: ""}
//            是否按照流量计费---${
//            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)?.takeIf { it }?.let { "是" } ?: "否"
//
//        }
//        """.trimIndent())
//        val linkProperties = connManager.getLinkProperties(currentNetwork)
        //2.监听网络事件--------------------
        registerWifiState()
    }

    /**
     * [用ping的方式检查是否连上了互联网，来自网络](https://www.jianshu.com/p/1b542cece605)
     */
    suspend fun isConnectInternet() {
        CoroutineScope(Dispatchers.Default).async {
            val runtime = Runtime.getRuntime()
            val p = runtime.exec("ping -c 3 www.baidu.com")
            p.waitFor().apply {
                Log.i(TAG, "Process:$this")
            }
        }.await()
    }


    fun registerWifiState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //对默认网络监听
            connManager.registerDefaultNetworkCallback(networkCallback);//added in 24
        } else {
            val request = NetworkRequest.Builder().build()
            //对要请求的网络监听
            connManager.registerNetworkCallback(request, networkCallback);//added in 21
            //指定网络发送请求可能跟request有关 https://blog.csdn.net/dongziqi_csdn/article/details/111405109
        }
    }

    fun unregisterWifiState() {
        connManager.unregisterNetworkCallback(networkCallback)
    }

    companion object {
        const val TAG = "ConnectivityUtil"
    }
}