package com.example.jetpack.topics.permission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Telephony
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.example.jetpack.R

/**
 * [官方库easyPermission](https://github.com/googlesamples/easypermissions)
 * [权限 API 参考文档页面](https://developer.android.google.cn/reference/android/Manifest.permission)
 * 应用权限有助于保护用户隐私
 * 受限数据，例如系统状态和用户的联系信息。
 * 受限操作，例如连接到已配对的设备并录制音频。
 * 1. 使用权限的流程
 *    请确定您是否无需声明权限即可获取相关信息或执行相关操作，应用必须访问受限数据或执行受限操作才能实现某个用例，请声明相应的权限。有些权限是用户安装应用时自动授予的权限，称为安装时权限。
 *    其他权限则需要应用在运行时进一步请求权限，此类权限称为运行时权限。
 *    [流程图](https://developer.android.google.cn/images/training/permissions/workflow-overview.svg)
 * 2. 权限类型
 *    2.1 安装时权限
 *        安装时权限授予应用对受限数据的访问权限，并允许应用执行对系统或其他应用只有最低影响的受限操作(系统会为普通权限分配“normal”保护级别，)。如果您在应用中声明了安装时权限，
 *        系统会在用户安装您的应用时自动授予应用相应权限。应用商店会在用户查看应用详情页面时向其显示安装时权限通知,安装时权限包括 签名权限 和 普通权限
 *        签名权限：当应用声明了其他应用已定义的签名权限时，如果两个应用使用同一证书进行签名，系统会在安装时向前者授予该权限。保护级别“signature”
 *        普通权限：保护级别"normal”
 *        安装时权限是不需要申请就能访问到
 *    2.2 运行时权限
 *        运行时权限也称为危险权限，此类权限授予应用对受限数据的额外访问权限，并允许应用执行对系统和其他应用具有更严重影响的受限操作 保护级别 dangerous
 *        应用安装在搭载 Android 6.0（API 级别 23）或更高版本的设备上，则您必须自己请求权限。
 *        尽可能在使用到运行时权限的时候申请，例如需要拍照的时候再申请camera权限
 *    2.3 特殊权限
 * 3. 权限使用最佳做法
 *    3.1 请求最少数量权限
 *        [怎么用最少权限实现功能感觉讲的很鸡肋](https://developer.android.google.cn/training/permissions/evaluating)
 *        如果有其它应用能实现此功能，使用 intent 将任务委托给其他应用
 *        显示附近地点，如果很频繁的获取则需要获取ACCESS_COARSE_LOCATION(network_provider)，偶尔一次请考虑改为让用户输入地址或邮政编码。需要精确位置请求 ACCESS_FINE_LOCATION(GPS_provider)
 *        拍摄照片:用户可能会在您的应用中使用预安装的系统相机应用来拍摄照片。在这种情况下，请勿声明 CAMERA 权限，而是改为调用 ACTION_IMAGE_CAPTURE intent 操作
 *        录制视频:用户可能会在您的应用中使用预安装的系统相机应用来录制视频。在这种情况下，请勿声明 CAMERA 权限，而是改为调用 ACTION_VIDEO_CAPTURE intent 操作。
 *        在应用中暂停媒体: 在用户接听电话或用户配置的闹钟触发时，您的应用应暂停播放所有媒体，直到其重新获得音频焦点再恢复播放。
 *                        如需支持此功能，请勿声明 READ_PHONE_STATE 权限，而是改为实现 onAudioFocusChange() 事件处理程序
 *        过滤来电:为了给用户最大限度地减少不必要的干扰，您的应用可能会过滤掉垃圾来电。如需支持此功能，请勿声明 READ_PHONE_STATE 权限，而是改用 CallScreeningService API。
 *    3.2 将运行时权限与特定操作相关联
 *        尽可能往后推迟到在应用的用例流程中请求权限，例如需要拍照的时候再申请camera权限
 *    3.3 权限继承
 *        添加某个库时，您也会继承它的权限要求。
 * 4. 权限声明
 *    4.1 将硬件声明为可选
 *         <uses-feature android:name="android.hardware.camera" android:required="true" /> 必须在有摄像头的设备才能运行 默认false
 *         // 如果上面的失效，这段代码能检查是否真正具有这些设备
 *             if (applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
 *                 // Continue with the part of your app's workflow that requires a
 *                 // front-facing camera.
 *                } else {
 *                        // Gracefully degrade your app experience
 *                }
 * 5. 权限请求
 *    5.1 基本原则
 *        当用户开始与需要相关权限的功能互动时，在具体使用情境下请求权限。
 *        不要阻止用户使用应用。始终提供选项供用户取消与权限相关的指导界面流程。
 *        如果用户拒绝或撤消某项功能所需的权限，请适当降级您的应用以便让用户可以继续使用您的应用（可能通过停用需要权限的功能来实现）。
 *        不要对系统行为做任何假设。例如，假设某些权限会出现在同一个权限组中。权限组的作用只是在应用请求密切相关的多个权限时，帮助系统尽可能减少向用户显示的系统对话框数量。
 *    5.2 请求权限工作流
 *        [工作流程图](https://developer.android.google.cn/images/training/permissions/workflow-runtime.svg)
 *        5.2.4 检查用户是否已经获取到权限
 *        5.2.5 确定您的应用是否应向用户显示理由,如果用户知道应用需要相应权限的原因，他们会更容易接受权限请求
 *        5.2.6 请求单个或多个权限，位置信息比较特殊，跟位置相关的权限有多个，以下根据App的位置需求请求相关权限 TODO 官网举例前台位置信息和后台位置信息，待总结
 *        note 如果您的应用以 Android 11（API 级别 30）或更高版本为目标平台并且数月未使用，系统会重置运行时权限
 * 6. 默认程序
 *    官网例子，要先成为默认处理程序，然后才能申请相关权限
 * 7. 利用权限限制与其它应用交互
 *    限制与Activity：   <activity android:name=".topics.anim.Transition1Activity" android:permission="android.permission.INTERNET" ></activity>
 *    表示拥有android.permission.INTERNET 权限的应用才能调用此Activity
 *    系统会在 Context.startActivity() 和 Activity.startActivityForResult() 期间检查该权限。如果调用方没有所需的权限，将会发生 SecurityException。
 *    限制与Service：  同上
 *    限制与ContentProvider: 与上面稍有不同 android:readPermission 和 android:writePermission
 *    限制与广播 同上
 *
 *
 *
 */
class PermissionActivity : AppCompatActivity() {
    private val openSetting = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        //5.2.4 检查是否已经拥有该权限
        println("${haveStoragePermission(Manifest.permission.ACCEPT_HANDOVER)}")

        //5.2.5 显示理由
        val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(
            this, Manifest.permission.ACCEPT_HANDOVER
        ) //请求了但是被拒绝了，此时返回true。然后就弹个窗解释下问什么要这个请求，这个方法就是专门处理这种情况的。[详情请看](https://www.jianshu.com/p/61b3ef6f7ac5)
        println("shouldShow-----$shouldShow")

        //5.2.6 请求权限 (使用契约类请求ActivityResultContract)
        //TODO registerForActivityResult() 对intent 的封装，待列出所有功能
        //使用 registerForActivityResult() 请求权限 实际上是对Intent的封装
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                println("权限通过")
            } else {
                showRational()
            }
        }.apply {
            launch(Manifest.permission.ACCEPT_HANDOVER)
        }

        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            for ((key, value) in it) Log.i("quanxian", "key = $key , value = $value")
//            if (it) {
//                println("权限通过")
//            } else {
//                showRational()
//            }
        }.apply {
            launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }

        // 自行管理请求权限，使用 requestPermissions 请求，在onRequestPermissionsResult() 返回结果
        // ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCEPT_HANDOVER),101);

        //6. 成为默认
        //TODO 没反应, 并且官网提供了怎么称为短信默认应用，其它呢？？？
//        openSetting.launch(Intent().apply {
//            action = Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT
//            putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
//        })
        val setSmsAppIntent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
        setSmsAppIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
        startActivityForResult(setSmsAppIntent, 110)
    }

    private fun haveStoragePermission(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun showRational() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCEPT_HANDOVER)) {
            println("用户拒绝后显示原因")//
        } else {
            println("用户点击了禁止再次访问")// 这时候要 导航到权限设置窗口，手动设置
            // 必须在 LifecycleOwners的STARTED 之前调用 registerForActivityResult. 否则报错 推荐用委托形式
            openSetting.launch(Intent().apply {
                action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.parse("package:$packageName")
            })
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("requestCode------$requestCode")

    }
}