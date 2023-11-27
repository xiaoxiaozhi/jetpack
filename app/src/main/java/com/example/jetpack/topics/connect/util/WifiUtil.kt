package com.example.jetpack.topics.connect.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class WifiUtil(val context: Context) {
    val wifiManager: WifiManager by lazy { context.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    private val wifiP2pManager by lazy { context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager } // 获取 WiFiP2pManager
    val wifiChannel: WifiP2pManager.Channel? by lazy {
        Log.i(TAG, "WifiUtil获取mainLooper---${context.mainLooper.hashCode()}")
        wifiP2pManager?.initialize(context, context.mainLooper) {
            Log.i(TAG, "wifiChannel---onChannelDisconnected")
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun discoverPeers() = suspendCancellableCoroutine<Boolean> {
        wifiP2pManager?.discoverPeers(wifiChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                if (it.isActive) {
                    it.resume(true)
                }
                Log.i(TAG, "discoverPeers---onSuccess")
            }

            /**
             * WifiP2pManager.P2P_UNSUPPORTED, 1 设备不支持 wifi直连
             * WifiP2pManager.ERROR 0 内部错误导致
             * WifiP2pManager.BUSY  2 框架繁忙无法提供服务
             */
            override fun onFailure(p0: Int) {
                if (it.isActive) {
                    it.resume(false)
                }
                Log.i(TAG, "discoverPeers---onFailure---$p0")
            }
        })
//        wifiP2pManager?.discoverPeersOnSpecificFrequency()
    }

    @SuppressLint("MissingPermission")
    suspend fun requestPeers() = suspendCancellableCoroutine<WifiP2pDeviceList> {
        wifiP2pManager?.requestPeers(wifiChannel) { devices ->
            it.takeIf { it.isActive }?.resume(devices)
//            Log.i(TAG, it.deviceList.joinToString { "${it.deviceName} -> ${it.deviceAddress}  " })
//            it.deviceList.takeIf { it.isNotEmpty() }?.first()?.also { p2pDevice ->
//
//            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connectDevice(p2pDevice: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = p2pDevice.deviceAddress
            wps.setup = WpsInfo.PBC
        }
        wifiP2pManager?.connect(wifiChannel, config, object : WifiP2pManager.ActionListener {

            override fun onSuccess() {
                Log.i(TAG, "连接成功")
            }

            override fun onFailure(reason: Int) {
                Log.i(TAG, "连接失败---$reason")
            }
        })
    }

    fun requestConnectionInfo() {
        wifiP2pManager?.requestConnectionInfo(wifiChannel, connectionListener)
    }

    @SuppressLint("MissingPermission")
    suspend fun createGroup() = suspendCancellableCoroutine<Boolean> {
        removeGroupIfNeed()
        wifiP2pManager?.createGroup(wifiChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                val log = "createGroup onSuccess"
                it.resume(true)
            }
            override fun onFailure(reason: Int) {
                val log = "createGroup onFailure: $reason"
                it.resume(false)
            }
        })
    }

    fun removeGroup() {
        removeGroupIfNeed()
    }

    @SuppressLint("MissingPermission")
    suspend fun getGroupInfo() = suspendCancellableCoroutine<String> {
        wifiP2pManager?.requestGroupInfo(wifiChannel) { group ->

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                """
                |是群组拥有者---${group?.isGroupOwner}
                |群组拥有者名称---${group?.owner?.deviceName}
                |群组拥有者地址---${group?.owner?.deviceAddress}
                |接口---${group?.`interface`}
                |群组名称---${group?.networkName}
                |frequency---${group?.frequency}
                |密码---${group?.passphrase}
            """.trimIndent().apply {
                    it.resume(this)
                    Log.i(TAG, this)
                }

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                """
                |是群组拥有者---${group.isGroupOwner}
                |群组拥有者名称---${group.owner.deviceName}
                |群组拥有者地址---${group.owner.deviceAddress}
                |接口---${group.`interface`}
                |群组名称---${group.networkName}
                |frequency---${group.frequency}
                |networkId---${group.networkId}
                |密码---${group.passphrase}
            """.trimIndent().apply {
                    it.resume(this)
                    Log.i(TAG, this)
                }

            } else {
                """
                |是群组拥有者---${group.isGroupOwner}
                |群组拥有者名称---${group.owner.deviceName}
                |群组拥有者地址---${group.owner.deviceAddress}
                |接口---${group.`interface`}
                |群组名称---${group.networkName}
                |密码---${group.passphrase}
            """.trimIndent().apply {
                    it.resume(this)
                    Log.i(TAG, this)
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun removeGroupIfNeed() {
        wifiP2pManager?.requestGroupInfo(wifiChannel) { group ->
            if (group == null) {

            } else {
                wifiP2pManager?.removeGroup(wifiChannel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        val log = "removeGroup onSuccess"
                    }

                    override fun onFailure(reason: Int) {
                        val log = "removeGroup onFailure: $reason"
                    }
                })
            }
        }
    }

    private val connectionListener = WifiP2pManager.ConnectionInfoListener { info ->
        Log.i(TAG, "groupOwnerAddress---${info.groupOwnerAddress?.hostAddress}")
        // After the group negotiation, we can determine the group owner
        // (server).
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
            Log.i(TAG, "群组所有者---")
        } else if (info.groupFormed) {
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
            Log.i(TAG, "群组成员---")
        }
    }


    companion object {
        const val TAG = "WifiUtil"
    }
}