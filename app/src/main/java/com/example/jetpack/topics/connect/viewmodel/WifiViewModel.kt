package com.example.jetpack.topics.connect.viewmodel

import android.net.wifi.p2p.WifiP2pDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpack.topics.connect.util.WifiUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WifiViewModel @Inject constructor(private val wifiUtil: WifiUtil) : ViewModel() {

    private var discoverJob: Job? = null
    private val _wifiP2pDeviceList = MutableLiveData<List<WifiP2pDevice>>()
    val wifiP2pDeviceList: LiveData<List<WifiP2pDevice>> = _wifiP2pDeviceList
    var isWifiEnabled
        get() = wifiUtil.wifiManager.isWifiEnabled
        set(value) {
            wifiUtil.wifiManager.isWifiEnabled = value
        }

    fun getChannel() = wifiUtil.wifiChannel

    companion object {
        const val TAG = "WifiViewModel"
    }

    fun discoverPeers() = viewModelScope.launch(Dispatchers.Default) {
        while (isActive) {
            wifiUtil.discoverPeers()
            delay(2 * 1000)
        }
    }.apply {
        discoverJob?.cancel()
        discoverJob = this
    }


    fun cancelDiscover() {
        discoverJob?.takeIf { it.isActive }?.cancel()
    }

    fun requestPeers() {
        viewModelScope.launch(Dispatchers.Default) {
            _wifiP2pDeviceList.postValue(wifiUtil.requestPeers().deviceList.toList())
        }
    }

    fun requestConnectionInfo() {
        viewModelScope.launch(Dispatchers.Default) {
            wifiUtil.requestConnectionInfo()
        }
    }

    fun connectDevice(wifiP2pDevice: WifiP2pDevice) {
        wifiUtil.connectDevice(wifiP2pDevice)
    }
}