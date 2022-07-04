package com.example.jetpack.topics.appdatafiles

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageManager.ACTION_CLEAR_APP_CACHE
import android.os.storage.StorageManager.ACTION_MANAGE_STORAGE
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityStorageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * 数据和文件存储
 * 1.存储类别
 *   ![详细信息](https://developer.android.google.cn/training/data-storage)
 *   1.1 应用专属存储空间：存储仅供应用使用的文件，可以存储到内部存储卷中的专属目录或外部存储空间中的其他专属目录。不需要额外权限，卸载后删除
 *       1.1.1 内部存储空间：系统会阻止其他应用访问内部存储空间，并且在 Android 10（API 级别 29）及更高版本中，系统会对这些位置进行加密。
 *             data/user/0/com.example.jetpack/files 用于放置持久化文件 /data/user/0/com.example.jetpack/cache 用于放置缓存文件
 *             但是这些空间比较小，使用前注意查询设备可用空间
 *       1.1.2 外部存储空间：如果内部存储空间不足以存储文件，请转到外部存储空间。其他应用可以在具有适当权限的情况下访问外部存储空间，
 *             但存储在内部存储空间中的文件主要目的是供自己应用使用; Android 4.4（API 级别 19）或更高版本中 应用无需请求任何与存储空间相关的权限即可访问外部存储空间中的应用专属目录。
 *             卸载应用后，系统会移除这些目录中存储的文件;在Android 9-（API28）只要您的应用具有适当的存储权限(可能是WRITE_EXTERNAL_STORAGE)，就可以访问属于其他应用的应用外部空间专用文件， Android 10+（API29）
 *             及更高版本为目标平台的应用在默认情况下被授予了对外部存储空间的分区访问权限（即分区存储）。启用分区存储后，应用将无法访问属于其他应用的应用专属目录。即除了自己专属目录其它访问不了
 *       note：如需进一步保护应用专属文件，请使用 Android Jetpack 中包含的 Security 库对这些静态文件进行加密。加密密钥专属于您的应用。
 *   1.2 共享存储：存储您的应用打算与其他应用共享的文件，包括媒体、文档和其他文件。查看代码 SharedActivity
 *   1.3 偏好设置：以键值对形式存储私有原始数据。
 *   1.4 数据库：使用 Room 持久性库将结构化数据存储在专用数据库中。在存储敏感数据（不可通过任何其他应用访问的数据）时，
 *       应使用内部存储空间、偏好设置或数据库。内部存储空间的一个额外优势是用户无法看到相应数据。
 * 2. 存储位置
 *    Android 提供两类物理存储位置：内部存储空间和外部存储空间。在大多数设备上，内部存储空间小于外部存储空间。
 *    可移除卷（例如 SD 卡）在文件系统中属于外部存储空间。Android 使用路径（例如 /sdcard）表示这些存储设备
 *    默认情况下，应用本身存储在内部存储空间中。不过，如果您的 APK 非常大，在manifest文件中指明存储在外部空间
 *    android:installLocation="preferExternal"
 * 3. 对外部存储空间的访问权限
 *    Android 11 引入了 MANAGE_EXTERNAL_STORAGE 权限，该权限提供对应用专属目录和 MediaStore 之外文件的写入权限。TODO
 *    3.1 分区存储
 *        以 Android 10（API 级别 29）及更高版本为目标平台的应用在默认情况下被授予了对外部存储空间专属目录访问权限（即分区存储）
 *        Android 4-9 不要权限也可以访问应用外部空间专属目录，其它目录需要WRITE_EXTERNAL_STORAGE权限。Android10  不要权限也可以访问应用外部空间专属目录，其它目录给了READ权限也访问不了
 *        停用分区存储  <application android:requestLegacyExternalStorage="true" ... > 该方案是一个过渡方案只在android10起效
 * 4. 查询可用存储空间(看下面例子)
 *    4.1 创建存储空间管理 activity：在清单文件中使用<application android:manageSpaceActivity  系统可以启动改类管理存储空间，即使 android:exported=false
 *        正常情况下，在设置--->应用信息界面，只有清除缓存一个按钮，点击会清楚全部缓存，但是一些重要的信息不想被清楚，设置manageSpaceActivity后
 *        系设置--->应用信息界面就会多一个管理空间选项(小米手机要点清除数据才会浮现管理空间选项)，在里面要清除哪些缓存可以自己设置
 *    4.2 清除外部存储空间缓存
 *        ACTION_CLEAR_APP_CACHE ：该 操作会严重影响设备的电池续航时间，并且可能会从设备上移除大量的文件。Android 11 API30
 *
 *  5. 挂载路径
 *     (官网没有提到)
 *
 *
 */
class StorageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStorageBinding
    private lateinit var storageManager: StorageManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //1.1.1.1 访问内部空间持久性文件
        println(filesDir.toString())// /data/user/0/com.example.jetpack/files 内部持久性空间
        println(cacheDir.toString())// /data/user/0/com.example.jetpack/cache 内部缓存空间
        val file = File(filesDir, "inner").apply {
            if (!exists()) {
                createNewFile()
            }
        }
        //1.1.1.2 输出流写入内部空间持久性文件
        val filename = "myfile"
        val fileContents = "Hello world!"
        openFileOutput(filename, MODE_PRIVATE).use {
            it.write(fileContents.toByteArray())
        }//在内部空间用输出流写文件，如果不存在则会创建
        // 在搭载 Android 7.0（API 级别 24）或更高版本的设备上，除非您将 Context.MODE_PRIVATE 文件模式传递到 openFileOutput()，
        // 否则会发生 SecurityException。
        // 如需允许其他应用访问存储在内部存储空间内此目录中的文件，请使用具有 FLAG_GRANT_READ_URI_PERMISSION 属性的 FileProvider TODO 不是不能访问吗？什么意思 查看FileProvider

        //1.1.1.3 输入流读取内部空间持久性文件
        openFileInput("myfile").bufferedReader().useLines { lines ->
            lines.fold("") { some, text ->
                "$some\n$text"
            }//fold 带初始值的累加函数
        }//在内部空间用输入流转字符流读取文件，如果不存在则会报错
        //1.1.1.4 移除内部空间持久性文件
        deleteFile(filename)

        //1.1.1.5 查看内部空间持久性文件列表
        println("内部空间持久性文件列表---${fileList().toList()}")
        //1.1.1.6 创建目录，在内部存储空间。API文档说是子目录实际上files 同级目录
        getDir("fileChild", MODE_PRIVATE).apply {
            println("fileChild 是否存在----${exists()}---${absolutePath}")
        }
        //1.1.1.7 创建内部空间缓存文件
        File.createTempFile(//这种方式创建的 文件会在前缀和后缀之间生成一个随机数，虽然可以通过返回的File确定文件名但是这样做不太方便
            filename, ".txt", cacheDir
        )//note:此缓存目录旨在存储应用的少量敏感数据。如需确定应用当前可用的缓存空间大小，请调用 getCacheQuotaBytes()。
        //      当设备的内部存储空间不足时，Android 可能会删除这些缓存文件以回收空间。因此，请在读取前检查缓存文件是否存在。
        File(cacheDir, "temp.txt").apply {
            if (!exists()) createNewFile()// 更喜欢文件名明确的方尺方式 创建缓存文件
        }

        //1.1.1.8 移除内部空间缓存文件
        File(cacheDir, "temp.txt").apply {
            if (exists()) {
                println("存在")
                delete()
            } else {
                println("不存在")
            }
        }
        //1.1.1.9 查询catch配额大小
        storageManager = applicationContext.getSystemService<StorageManager>()!!
        getExternalFilesDir(null)?.let {
            // TODO k30pro 上得到配额 64M  ，已经占用的缓存2M， 感觉像是固定值之后遇到其它例子再探究
            val uuid = storageManager.getUuidForPath(it)//返回存储器的 uuid。path属于该存储器。
            println("catch 配额空间---${storageManager.getCacheQuotaBytes(uuid) shr 20}MB")//如果超过这个空间，系统缓存不足的时候会最先删除您应用的缓存数据；另外这个空间是动态变化的
//            codeCacheDir //存储应用程序生成的已编译或优化代码 这个是什么？？？
            println("已经存在缓存大小----${storageManager.getCacheSizeBytes(uuid) shr 20}MB")//获取 已经存在的缓存大小，这个方法跟踪的缓存数据总是包括 Context.getCacheDir ()和 Context.getCodeCacheDir () ，如果主共享/外部存储与私有数据驻留在同一个存储设备上，它还包括 Context.getExternalCacheDir ()，配额大小 - 缓存大小 得出可以存储的大小
        }//Android 不是java Path接口在Android 8+才添加
        //1.1.2.1 验证外部空间读写性
        println("isExternalStorageWritable------${isExternalStorageWritable()}")//验证是否可以写
        println("isExternalStorageReadable------${isExternalStorageReadable()}")//验证是否可以读
        //1.1.2.2 选择外部空间存储位置
        val externalStorageVolumes: Array<out File> = ContextCompat.getExternalFilesDirs(applicationContext, null)
        externalStorageVolumes.map { it.absolutePath }.forEach() {
            println("外部存储空间挂载点------${it.toString()}")//如果有sd卡插槽，这里会返回几个外部存储空间
        }
        println("第一个外部空间存储位置----${externalStorageVolumes[0]}") //有的设备提供多个sd插槽或者分配内部存储空间为外部存储空间。这两种情况下提供多个外部存储空间位置，使用第一个为主存储位置

        //1.1.2.3 访问外部专属空间持久性文件
        ///storage/emulated/0/Android/data/com.example.jetpack/files/Documents/inner
        File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "inner").apply {
            if (!exists()) createNewFile() else println("Environment.DIRECTORY_DOCUMENTS存在")
        }//类型不为空，则会在files下面创建该文件夹，例如Environment.DIRECTORY_DOWNLOADS，则会创建Download文件夹

        //1.1.2.4 创建外部空间缓存文件
        File(externalCacheDir, "catch1").apply {
            if (!exists()) createNewFile()
        }
        //1.1.2.5 移除外部空间缓存文件
        File(externalCacheDir, "catch1").apply {
            if (exists()) delete()
        }//没有也不会报错
        //1.1.2.6 媒体内容要存储在外部空间专属位置
        getAppSpecificAlbumStorageDir(
            this, "pic1.jpg"
        )//务必保存在预定义的文件夹下面(使用Environment.下面的类型)  例如 DIRECTORY_PICTURES
        //4. 查询可用空间
        val NUM_BYTES_NEEDED_FOR_MY_APP = 1024 * 1024 * 10L;

        //TODO getUuidForPath 找到兼容性方法
        val appSpecificInternalDirUuid: UUID = storageManager.getUuidForPath(filesDir)
        val availableBytes: Long =
            storageManager.getAllocatableBytes(appSpecificInternalDirUuid)  //查询给定存储设备上最多可以分配给应用多少存储空间，通常大于实际存储空间因为系统可以删除其它应用的catch文件。像录制视频这样需要无限量存储空间的需求，调用间隔应该大于30S
        if (availableBytes >= NUM_BYTES_NEEDED_FOR_MY_APP) {
            storageManager.allocateBytes(//当可分配空间大于需要空间时候，开始分配空间
                appSpecificInternalDirUuid, NUM_BYTES_NEEDED_FOR_MY_APP
            )//分配空间，如果空间不够系统将删除缓存文件，像录制视频这样需要无限量存储空间的需求，调用间隔应该大于60S
        } else {
            val storageIntent = Intent().apply {//当可分配空间小于需要空间，则打开清楚缓存界面让用户手动清除
                // To request that the user remove all app cache files instead, set
                // "action" to ACTION_CLEAR_APP_CACHE.
                action = ACTION_MANAGE_STORAGE
            }
            ACTION_MANAGE_STORAGE
            startActivity(storageIntent)
        }//还有另一种方法，在不知道所需的确切存储空间时候，不事先调用getAllocatableBytes和allocateBytes，直接写入文件，出现 IOException 时将其捕获，catch里面处理

        //4.2 清除外部存储空间缓存
        ACTION_CLEAR_APP_CACHE //允许用户清空外部存储空间缓存，显示一个对话框让用户自己选择，
        binding.button1.setOnClickListener {
//            startActivityForResult(Intent().apply { action = ACTION_CLEAR_APP_CACHE }, 110, null)
            startActivity(Intent().apply { action = ACTION_CLEAR_APP_CACHE })
        }//FIXME 点了没反应


        //BufferedWriter(FileWriter(File(""),true))//续写文件 用 FileWriter,然后通过组合BufferedWriter 获得 带缓冲区、添加文本的 输出流
        //kotlin.io 扩展 https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io/
        //kotlin.io.Path 扩展 https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.io.path/ TODO Path("")报错用不了，很奇怪。实验性质扩展不建议使用
        //5. 挂载路径
        storageManager.storageVolumes.forEach() {
            println("挂载点------${it.directory?.absolutePath}-----挂载状态${it.state}")//小米k30 pro只有一个挂载点 /storage/emulated/0,模拟器除了上述挂载点还有 /storage/15EC-3213
        }
    }


    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in setOf(
            Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY
        )
    }

    fun getAppSpecificAlbumStorageDir(context: Context, albumName: String): File? {
        // Get the pictures directory that's inside the app-specific directory on
        // external storage.
        val file = File(
            context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES
            ), albumName
        )
        if (!file?.mkdirs()) {
            Log.e("FileError", "Directory not created")
        }
        return file
    }
}