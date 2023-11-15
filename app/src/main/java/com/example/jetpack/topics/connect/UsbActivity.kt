package com.example.jetpack.topics.connect

import android.app.PendingIntent
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityUsbBinding

/**
 * 1.枚举设备
 *   1.1通过Intent过滤器，当USB设备连接时，Android会启动包含:android.hardware.usb.action.USB_DEVICE_ATTACHED
 *      Action的意图，我们可以在Manifest中注册某个Activity支持处理该Action，让系统自动将连接设备的抽
 *      象对象 UsbDevice 发送至我们注册的Activity：(查看AndroidManifest.xml)还可以配置具体过滤哪些设备，详情请看device_filter.xml文件
 *   1.2主动枚举
 *      通过UsbManager.deviceList 获取连接上的USB设备
 *   1.3获取通信权限
 *      如果通过就
 *
 *
 *
 */
class UsbActivity : AppCompatActivity() {
    lateinit var binding: ActivityUsbBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_usb)
        val usbManager = getSystemService(USB_SERVICE) as UsbManager

        usbManager.deviceList.map {
            it.value
        }.takeIf { it.isNotEmpty() }?.also {
            val device = it[0]
            binding.button1.text = it[0].deviceName

            binding.button1.setOnClickListener {
//                if (!usbManager.hasPermission(device)) {
//                    val permissionIntent = PendingIntent.getBroadcast(
//                        this,
//                        0, Intent(com.enjoy.usbsamples.MainActivity.UsbReceiver.ACTION_PERMISSION),
//                        PendingIntent.FLAG_IMMUTABLE
//                    )
//                    usbManager.requestPermission(device, permissionIntent)
//                } else {
//                    showUsbDevices(usbDevice)
//                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        //2.1 通过Intent获取刚刚插拔的usb设备
        val usbDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            intent?.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
        }
        Log.i(TAG, "onNewIntent设备---${usbDevice?.deviceName}")
    }

    companion object {
        const val TAG = "UsbActivity"
    }
}