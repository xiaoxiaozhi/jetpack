package com.example.jetpack.topics.intent

import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityIntentBinding

/**
 * 1. Intent 功能
 *    1.1 启动Activity startActivityForResult is instead of registerForActivityResult()
 *    1.2 启动服务 service大部分功能被Work代替，具体请看 WorkManagerActivity
 *    1.3 启动广播 各版本对广播做了限制，具体请看 broad.md
 * 2. Intent类型
 *    2.1 显式  指定组件名称来启动
 *    2.2 隐式 使用隐式 Intent 时，Android 系统通过将 Intent 的内容与在设备上其他应用的清单文件中声明的 Intent 过滤器进行比较，从而找到要启动的相应组件。
 *            如果 Intent 与 Intent 过滤器匹配，则系统将启动该组件，并向其传递 Intent 对象。如果多个 Intent 过滤器兼容，则系统会显示一个对话框，支持用户选取要使用的应用。
 *            如果您没有为 Activity 声明任何 Intent 过滤器，则 Activity 只能通过显式 Intent 启动。
 *            启动 Service 时，请始终使用显式 Intent，且不要为服务声明 Intent 过滤器，从 Android 5.0（API 级别 21）开始，如果使用隐式 Intent 调用 bindService()，系统会抛出异常。
 *            note: 被启动组件的
 * 3. 构建Intent
 *    3.1 组件名称 通过构造函数或者setComponent()、 setClassName()、 setClass() 设置ComponentName ，看参数就知道最终目的都是获取 ComponentName
 *                ComponentName对象是四大组件的标识符，包含两条信息，包名和类名 创建方式也围绕着这两条信息 ComponentName(pkg: String, cls: String)
 *    3.2 操作 您可以使用 setAction() 或 Intent 构造函数为 Intent 指定操作。
 *            操作分为两类 ACTION_VIEW 和 ACTION_SEND。
 *            前者 向用户显示的信息（例如，要使用图库应用查看的照片；或者要使用地图应用查看的地址）  k30pro 测试发现需要加上type 否则找不到组件而报错 type = "text/plain"
 *            后者如果您拥有一些用户可通过其他应用（例如，电子邮件应用或社交共享应用）共享的数据，
 *    3.3 数据 应用的数据类型通常由Action决定，如果操作是 ACTION _ EDIT，则数据应包含要编辑的文档的 URI。 setData(设置contentUri) setType(设置MIME类型) setDataAndType(两个都设置)
 *    3.4 类别 处理该Intent的组件的附加信息，使用 addCategory()设置 一个Intent中可以包含任意数量的类别，但是大多数Intent都不需要。常见的有 CATEGORY_BROWSABLE，处理该Intent的组件是个web浏览器处理
 *            CATEGORY_LAUNCHER 处理该Intent的组件是一个任务的初始Activity
 *    3.5 Extra 一个键值对使用putExtra()设置，如果有多个Extra使用putExtras(Bundle).类似使用URI的操作。有些操作不使用URI使用Extra. 系统已经在Intent中定义了多种Extra。如需自己定义请使用
 *              确保将应用的软件包名称作为前缀 EXTRA_GIGAWATTS = "com.example.jetpack.EXTRA_GIGAWATTS"
 *    3.6 标志 查看 TaskBackStackActivity
 *    3.7 显示Intent  设置好组件名
 *    3.8 隐式Intent 强调action，调用任何应用来执行该操作.如果没有Activity接收程序将崩溃, 使用Intent.resolveActivity()避免这种情况，如果返回为空说明没找到能操作该Action的组件
 *    3.9 使用应用选择器  Intent.createChooser(Intent(), title) 不使用选择器的话，用户将一个应用默认选中，以后都会默认这个应用。使用选择器后，默认按钮会消失，之后用户都会选择应用
 * 4. 接收隐式Intent
 *    定义组件可以接收哪些隐式 Intent，请在清单文件中使用 <intent-filter> 定义过滤器，一个组件可以定义多个过滤器，每个过滤器包含<action>、<data> 或 <category> 三种元素
 *    要接收隐式 Intent，必须在 Intent 过滤器中 <intent-filter>添加 <category android:name="android.intent.category.DEFAULT" /> 否则无法隐式启动
 *    系统通过将 Intent 与过滤器的三个元素进行比较，如果这三项全都匹配则将Intent传递给组件(没有定义某一个元素就不匹配)
 * 5. PendingIntent
 *    PendingIntent 对象是 Intent 对象的包装器。PendingIntent 的主要目的是授权外部应用使用包含的 Intent，就像是它从您应用本身的进程中执行的一样。PendingIntent主要用在三方面
 *    5.1 通知
 *    5.2 桌面小部件
 *    5.3 AlarmManager
 *    使用待定 PendingIntent 时，应用不会使用调用（如 startActivity()）执行该 Intent。而是工厂方法创建响应组件的PendingIntent
 *    PendingIntent.getActivity()，适用于启动 Activity 的 Intent。
 *    PendingIntent.getService()，适用于启动 Service 的 Intent。
 *    PendingIntent.getBroadcast()，适用于启动 BroadcastReceiver 的 Intent。
 * 6. Intent解析
 *    PackageManager 提供一整套 query...() 方法来返回所有能够接受特定 Intent 的组件。此外，还会提供一系列类似的 resolve...() 方法来确定响应 Intent 的最佳组件
 * 7. [常见的Intent](https://developer.android.google.cn/guide/components/intents-common#CreateTimer)
 *    7.1 创建闹钟 创建定时器 显示所有闹铃
 *    7.2 添加日历事件
 *    7.3 相机 拍摄照片或视频并将其返回  以静态图像模式启动相机应用 以视频模式启动相机应用
 *    7.4 联系人/人员应用
 *    7.5 电子邮件
 *    7.6 文件存储 检索特定类型文件 打开特定类型文件
 *    7.7 音乐或视频 播放媒体文件 查询播放音乐
 *    ......具体看该页
 */
class IntentActivity : AppCompatActivity() {
    private val resultForActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            val thumbnail: Bitmap? = activityResult.data?.getParcelableExtra("data")
            val fullPhotoUri: Uri? = activityResult.data?.data
            println("thumbnail------$thumbnail    fullPhotoUri-----$fullPhotoUri")
        }
    private lateinit var binding: ActivityIntentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntentBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        3. 构建Intent
        binding.button1.setOnClickListener {
            Intent().apply {
                //3.1 参数1：packageName就是AndroidManifest.xml文件中根结点下的package, 参数2：activity节点中name属性的值
                component = ComponentName(this@IntentActivity.packageName, Intent1Activity::class.java.name)
                println(component)
                startActivity(this)
            }
        }
        //3.8 安全隐式调用 Intent
        binding.button2.setOnClickListener {
            Intent().apply {
                action = "com.example.jetpack.action"
                resolveActivity(packageManager)?.let { startActivity(this) } ?: println("resolveActivity----null")
            }
        }
        //3.9 应用选择器
        binding.button3.setOnClickListener {
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "q123")
                type = "text/plain"
                resolveActivity(packageManager)?.let {
                    startActivity(Intent.createChooser(this, "123"))
                } ?: println("resolveActivity----null")
            }
        }

        //7.6 返回特定文件
        binding.button4.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                resultForActivity.launch(this)
            } ?: println("返回特定文件 null")
        }
    }
}