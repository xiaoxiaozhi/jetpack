package com.example.jetpack.topics.connect

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityWifiBinding
import com.example.jetpack.havePermissions
import com.example.jetpack.topics.connect.adapter.WifiDeviceAdapter
import com.example.jetpack.topics.connect.viewmodel.WifiViewModel
import com.google.android.gms.common.util.DataUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStreamWriter
import java.security.Permission

/**
 * 学习wifi直连之前需要先学习wifi协议，官网课程没有相关部分，看不明白
 * 网上找来的学习笔记 [WiFi Direct即P2P协议学习笔记](https://blog.csdn.net/m0_38059875/article/details/122075907)
 * [Android 实现无网络传输文件,](https://juejin.cn/post/6844903565186596872) 还是难以理解
 * [wifi系列文章](https://www.zhihu.com/column/c_1610405613384474625) 科普性质的文章
 * [深入理解Android：Wi-Fi，NFC和GPS](https://static.kancloud.cn/alex_wsc/android-wifi-nfc-gps/414116)
 * [这个例子可以](https://github.com/leavesCZY/WifiP2P)
 *
 * 1. WLAN 扫描流程
 * 1.1 为 SCAN_RESULTS_AVAILABLE_ACTION 注册一个广播监听器，
 * 1.2使用 WifiManager.startScan() 请求扫描。异步的结果稍后会通过广播接收,需要注册 Manifest.permission.CHANGE_WIFI_STATE权限
 * 1.3使用 WifiManager.getScanResults() 获取扫描结果
 * 需要申请的权限，否则报错,需要动态申请的权限 ACCESS_COARSE_LOCATION 或者 ACCESS_FINE_LOCATION
 *     <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
 *     <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 *     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 *     <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
 *在搭载 Android 8.0（API 级别 26）及更高版本的设备上，您可以使用 CompanionDeviceManager 代表应用对附近的配套设备执行扫描，而不需要位置权限
 *2. 实现wifi直连
 *   2.1 注册广播  每个回调可以调用什么什么方法在下方都已经列出来， WifiP2pManager.Channel = wifiP2pManager.initialize 将您的应用注册到WIFI框架
 *   2.2 查询附近设备  wifiP2pManager?.discoverPeers开始搜索直连设备,如果有设备会通过广播接收，再调用requestPeers请求已发现的设备
 *       调用discoverPeers搜索动作会持续多久呢？？？？？？
 *       翻看tFileTransporter的源码发现   discoverPeers 要循环执行才能发现p2p设备
 *       discoverPeers()--->接收广播WIFI_P2P_PEERS_CHANGED_ACTION广播--->调用 requestPeers() 以获取对等设备的更新列表。
 *       实际发现 discoverPeers 方法会触发SCAN_RESULTS_AVAILABLE_ACTION的广播回调，相当于替代了startScan() 方法
 *       但是为什么wifi直连的广播没有触发返回错误码2
 *   2.3 连接到对等设备
 * 需要申请的权限，因为要在两台设备传输文件，所以要申请网络权限
 * <上面的权限也需要/>
 * <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.INTERNET" />
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 *  在以 Android 13（API 级别 33）及更高版本为目标平台的应用中，discoverPeers() 和 connect() 都需要 android.permission.NEARBY_WIFI_DEVICES 权限。
 *  在以较低 Android 版本为目标平台的应用中，这些方法需要 ACCESS_FINE_LOCATION 权限。
 * <uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES" android:usesPermissionFlags="neverForLocation" /> android13才需要的权限
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="32" />，最多在android12申请，再往上就不需要
 * 3. WIFI 感知 TODO 没有总结
 */
@AndroidEntryPoint
class WifiActivity : AppCompatActivity() {
    val wifiManager: WifiManager by lazy { applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    val wifiP2pManager by lazy { applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager } // 获取 WiFiP2pManager
    var wifiChannel: WifiP2pManager.Channel? = null
    private lateinit var binding: ActivityWifiBinding
    private val adapter: WifiDeviceAdapter = WifiDeviceAdapter()
    private val viewModel by viewModels<WifiViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wifi)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)
        if (!viewModel.isWifiEnabled) {
            //android10 29之后系统不再允许app开关wifi，调用始终返回false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))//让用户手动打开开关
            } else {
                viewModel.isWifiEnabled = true
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        }.also { permissions ->
            havePermissions(permissions).takeIf { it.isNotEmpty() }?.also { permissions ->
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                    for ((key, value) in it) Log.i(TAG, "key = $key , value = $value")
                }.launch(permissions)
            }
        }

        //1.2 查询附近wifi 结果通过广播异步返回
//        val success = wifiManager.startScan()
        wifiChannel = wifiP2pManager?.initialize(applicationContext, mainLooper, null) // 初始化 WifiChannel
        viewModel.getChannel()?.also {
            Log.i(TAG, "wifiChannel存在 注册广播----")
            val intentFilter1 = IntentFilter().apply {
                addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION) // Wifi 直连可用状态改变
                addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION) // Wifi 直连发现的设备改变
                addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION) // Wifi 直连的连接状态改变
            }
            registerReceiver(wifiReceiver, intentFilter1)
        } ?: {
            Log.i(TAG, "wifiChannel不存在----")
        }
        //2.2 查询附近设备
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.discoverPeers()
//            lifecycleScope.launch(Dispatchers.Default) {
//                repeat(10) {
//                    wifiP2pManager?.discoverPeers(wifiChannel, object : WifiP2pManager.ActionListener {
//                        override fun onSuccess() {
////                        if (it.isActive) {
////                            it.resume(true)
////                        }
//                            Log.i(WifiUtil.TAG, "discoverPeers---onSuccess")
//                        }
//
//                        /**
//                         * WifiP2pManager.P2P_UNSUPPORTED, 1 设备不支持 wifi直连
//                         * WifiP2pManager.ERROR 0 内部错误导致
//                         * WifiP2pManager.BUSY  2 框架繁忙无法提供服务
//                         */
//                        override fun onFailure(p0: Int) {
////                        if (it.isActive) {
////                            it.resume(false)
////                        }
//                            Log.i(WifiUtil.TAG, "discoverPeers---onFailure---$p0")
//                        }
//                    })
//                    delay(1000)
//                }
//            }
        }
        viewModel.wifiP2pDeviceList.observe(this) {
            adapter.setDataList(it)
        }
        adapter.setOnClick {
            viewModel.connectDevice(it)
        }
    }

    private val wifiReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    // WIFI开关状态返回
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                        Log.e(TAG, "Wifi p2p disabled.")
                    } else {
                        Log.d(TAG, "Wifi p2p enabled.")
                    }
                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    Log.i(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION")
                    //-------------------------官方
                    // Call WifiP2pManager.requestPeers() to get a list of current peers
                    viewModel.cancelDiscover()
                    viewModel.requestPeers()
                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
//                    // Respond to new connection or disconnections
                    Log.i(TAG, "WIFI_P2P_CONNECTION_CHANGED_ACTION")
//                    //应用可使用 requestConnectionInfo()、requestNetworkInfo() 或 requestGroupInfo() 来检索当前连接信息
////                    wifiP2pManager?.requestConnectionInfo()
//                    val networkInfo: NetworkInfo? =
//                        intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)!! as NetworkInfo
//
//                    if (networkInfo?.isConnected == true) {
//
//                        // We are connected with the other device, request connection
//                        // info to find group owner IP
//
//                        wifiP2pManager?.requestConnectionInfo(wifiChannel, connectionListener)
//                    }
                    viewModel.requestConnectionInfo()
                }

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    // Respond to this device's wifi state changing
                    Log.d(TAG, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
//                    应用可使用 requestDeviceInfo() 来检索当前连接信息。
                }
            }
        }
    }
    private val connectionListener = WifiP2pManager.ConnectionInfoListener { info ->

        // String from WifiP2pInfo struct
        val groupOwnerAddress: String = info.groupOwnerAddress.hostAddress

        // After the group negotiation, we can determine the group owner
        // (server).
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
        } else if (info.groupFormed) {
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
        }
    }

    val wifiScanReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                Log.i(TAG, "onReceive---scanSuccess")
                scanSuccess()
            } else {
                Log.i(TAG, "onReceive---scanFailure")
                scanFailure()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver)
        unregisterReceiver(wifiReceiver)
    }


    private fun scanSuccess() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
//            val results = wifiManager.scanResults
//            results.takeIf { it.isNotEmpty() }?.apply { Log.i(TAG, "size---${this.size}") }?.forEach {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    it.wifiSsid?.bytes?.apply {
//                        Log.i(TAG, "wifi名称---${String(this, charset = Charsets.UTF_8)}")
//                    }
//                } else {
//                    Log.i(TAG, "wifi名称---${it.BSSID}")
//                }
//            } ?: Log.i(TAG, "scanSuccess----结果为null")
        } else {
            Log.i(TAG, "scanSuccess---没有ACCESS_FINE_LOCATION权限")
        }
    }

    private fun scanFailure() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "没有ACCESS_WIFI_STATE权限")
            null
        } else {
            Log.i(TAG, "扫描结果")
//            wifiManager.scanResults.apply {
//                forEach {
//                    Log.i(TAG, "扫描结果---${it.toString()}")
//                }
//            }
        }
    }

    companion object {
        const val TAG = "WifiActivity"
    }
}