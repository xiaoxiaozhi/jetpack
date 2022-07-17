package com.example.jetpack.topics.appdatafiles

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.os.storage.StorageManager
import android.provider.BaseColumns
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivitySharedBinding
import com.example.jetpack.haveStoragePermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * TODO k30pro(android12)上安装的app仍然可以在非专属目录上创建文件，这是怎么实现的
 * TODO 源码里的contentResolver 在哪里实例化
 * [共享存储MediaStore](https://blog.csdn.net/LoneySmoke/article/details/108944485)
 * 共享存储
 * 如果用户数据可供或应可供其他应用访问，并且即使在用户卸载应用后也可对其进行保存，请使用共享存储空间。
 * 1. 共享存储类型
 * 1.1 多媒体文件
 *    为了提供更丰富的用户体验，许多应用提供和允许访问位于外部存储卷上的媒体。通过ContentProvider媒体库，可以更轻松地检索和更新这些媒体文件。
 *    即使您的应用已卸载，这些文件仍会保留在用户的设备上。 查看 MediaRelatedActivity 类
 * 1.2. 文档
 * 1.3 数据集 在 Android 11+及更高版本中，系统会缓存多个应用可能使用的大型数据集。这些数据集可为机器学习和媒体播放等用例提供支持。
 *
 * 2.请求权限
 *   已启用分区存储：仅针对搭载 Android 9（API 级别 28）或更低版本的设备请求存储相关权限
 *   未启用分区存储：Android 9 或更低版本的设备上使用，或者您的应用暂时停用分区存储，您必须请求 READ_EXTERNAL_STORAGE 权限才能访问媒体文件。如果要修改媒体文件，您还必须请求 WRITE_EXTERNAL_STORAGE 权限。
 *   如果您的应用以 Android 10（API 级别 29）或更高版本为目标平台，为了使您的应用从照片中检索未编辑的 Exif 元数据，您需要在应用的清单中声明 ACCESS_MEDIA_LOCATION 权限，
 *   访问MediaStore共享媒体库，android 10 只有在访问自己创建的文件才不需要存储权限，访问其它的依然需要
 * 3.检查媒体库更新
 *   getVersion() 返回的版本是一个唯一字符串，该字符串会在媒体库发生重大变化时随之变化。如果返回的版本与上次同步的版本不同，请重新扫描并重新同步应用的媒体缓存
 * 4.查询媒体集合
 *
 * 5.加载文件缩略图
 *   TODO 待总结
 * 6.打开媒体文件
 *   6.1 用文件描述符打开文件
 *   6.2 用文件流打开文件
 *   6.3 绝对路径打开文件
 * 7.访问媒体注意事项
 *   7.1 缓存数据
 *   7.2 性能 直接文件路径依序读取媒体文件时，其性能与 MediaStore API 相当。随机读取和写入媒体文件时，使用 MediaStore API。比File API 快2倍 随机读写文件指的是 用 RandomAccessFile？？？
 *   7.3 DATA列 获取的文件绝对路径，读写的时候要准好处理 FileNotFoundException，如需创建或更新媒体文件，请勿使用 DATA 列的值。请改用 DISPLAY_NAME 和 RELATIVE_PATH 列的值
 *   7.4 存储卷
 *       VOLUME_EXTERNAL 提供设备上所有共享存储卷的视图。您可以读取此合成卷的内容，但无法修改这些内容。
 *       VOLUME_EXTERNAL_PRIMARY 代表设备上的主要共享存储卷。您可以读取和修改此卷的内容
 *       通过k30pro的测试以上两个 uri 查询到的内容一致，都是 content://media/external/video/media/文件ID     还没有发现external_primary 所以还不能确定上面说的是否正确
 *       通过k30pro的测试自己创建的文件 不管是VOLUME_EXTERNAL还是VOLUME_EXTERNAL_PRIMARY 都可以update操作
 *       MediaStore.getExternalVolumeNames() k30pro结果 external_primary；模拟器运行后 external_primary 和 15ec-3213
 *       TODO  示例8在k30pro运行发现VOLUME_EXTERNAL 仍然能添加项目，与描述不符
 *   7.4 媒体拍摄位置
 *       [访问照片位置信息](https://developer.android.google.cn/training/data-storage/shared/media#location-info-photos)
 *       [访问视频元数据](https://developer.android.google.cn/training/data-storage/shared/media#location-info-videos)
 *   7.5 分享  [content provider 创建指南](https://developer.android.google.cn/guide/topics/providers/content-provider-creating)
 *   7.6 媒体文件归属 媒体文件会归属于一个应用，在开了分区存储的情况下 不用权限也能访问(通过MediaStore) 您的应用创建的文件，访问其它应用的媒体文件就需要存储权限
 *       注意如果用户卸载并重新安装您的应用，您必须请求 READ_EXTERNAL_STORAGE 才能访问应用最初创建的文件
 * 8.添加项目
 *   note:  contentResolver.insert 之后要马上利用返回的fileUri 写入文件，测试发现 插入--->查询---->写入 会报找不到文件猜测该条记录可能已经被系统删除
 *   note: 插入相同名称的文件有一定概率 insert 返回 null 就算返回uri也会有一定概率写不进去数据
 *   8.1 独占媒体
 *       如果您的应用执行可能非常耗时的操作（例如写入媒体文件），那么在处理文件时对其进行独占访问非常有用，通过将 IS_PENDING 标记的值设为 1 来获取此独占访问权限
 *   8.2 存储位置
 *       共享的文件最好存储在这类公共文件夹(Environment.DIRECTORY_PICTURES这是一个相对路径，Pictures)，最好用8.1提到的方式保存、读取文件，而不是file API
 *       根据id 获取文件的uri  ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,cursor.getLong(BaseColumns._ID))
 * 9.更新文件
 *   9.1  android 10 以下可以对不属于自己的应用文件，移动、改名,android 10 及以上 只能对自己创建的文件修改
 *        仅在android 10 API 29 update MediaStore.Files的记录会报错which isn't part of well-defined collection not allowed， 为了避免这种情况，插入文件的时候选择MediaStore.Download
 *        TODO 在android10中修改其它app创建的 MediaStore.Files时候，还不知道怎么做 可以看下这个 [](https://stackoverflow.com/questions/55314476/how-to-rename-a-file-in-android-knowing-only-its-media-content-uri)
 *        [用C代码更新](https://developer.android.google.cn/training/data-storage/shared/media#update-native-code)
 *   9.2 更新其它应用创建的媒体文件
 *       在android 10上测试，删除应用，重新安装应用并申请读写权限，在MediaStore.File上找一个文件(发现是删除应用前自己创建的文件)，写入数据结果通过
 *       在k30 pro android12 上测试 在MediaStore.File 上找到一个文件 ，读取和删除都报错RunningException，跟官网介绍的不一致。MediaStore.File 上其它应用创建的文件无法读取吗？？？
 *       在 MediaStore.Images 上找文件，读取时候报 RecoverableSecurityException 错误与预期一致。然后申请权限读写文件
 *       如果您的应用在 Android 11 或更高版本上运行，您可以允许用户向应用授予对一组媒体文件的写入权限
 * 10.移除项目
 *    参照 9.2
 * 11.检查媒体文件更新
 *     MediaStore.getGeneration 还看不出来有什么用
 * 12. 管理媒体文件组
 *    在 Android 11 及更高版本中，您可以要求用户选择一组媒体文件，然后通过一次操作更新这些媒体文件
 *    使用 createWriteRequest() 修改文件。
 *    使用 createTrashRequest() 将文件移入和移出回收站。
 *    使用 createDeleteRequest() 删除文件。
 *    https://developer.android.google.cn/training/data-storage/shared/media#manage-groups-files
 *
 *   note:因为7.6 update操作经常出问题，要在次之前做好兼容工作
 *   [MIME 类型列表](https://www.runoob.com/http/mime-types.html)
 *
 */
class SharedMediaActivity : AppCompatActivity() {
    private val openSetting = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private lateinit var binding: ActivitySharedBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySharedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //3. 返回媒体库版本
        println("MediaStore Version -----${MediaStore.getVersion(this)}")
        if (!haveStoragePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (!it && !ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    openSetting.launch(Intent().apply {
                        action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.parse("package:$packageName")
                    })
                } else {
                    //4. 查询媒体集合
                    queryContent()
                }
            }.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            //4. 查询媒体集合
            queryContent()
        }
        //6.1 文件描述符打开文件
        // Open a specific media item using ParcelFileDescriptor.
//        contentResolver.openFileDescriptor(content-uri, "r").use { pfd ->
//            // Perform operations on "pfd".
//        }
        //6.2 文件流打开文件
        // Open a specific media item using InputStream.
//        contentResolver.openInputStream(content-uri).use { stream ->
//            // Perform operations on "stream".
//        }
        //6.3 绝对路径打开文件
        //使用File API

        //8. 添加项目
        val fileUri = insertFile()
        //9.1 更新
        updateFile1(fileUri)

        //9.2 更新其它应用创建的媒体文件
        //本例从第4节查询的文件选取一个，更改路径 被更改  Video(uri=content://media/external/video/media/24969, name=VID_20200810_152253.mp4, duration=16247, size=42043590, path=/storage/emulated/0/DCIM/Camera/VID_20200810_152253.mp4)
        //TODO 返回的是Unit 怎么处理
        binding.button1.setOnClickListener {
            updateFile2()
        }

        //11
        val storageManager = applicationContext.getSystemService<StorageManager>()!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            println("storageVolumes----${storageManager.storageVolumes[0].mediaStoreVolumeName}")
            storageManager.storageVolumes[0].mediaStoreVolumeName?.let {
                MediaStore.getGeneration(this, it).also {
                    println("getGeneration----$it")
                }
            }
        }
    }

    private fun insertFile(): Uri {
        val fileValue = ContentValues().apply {
            put(
                MediaStore.Files.FileColumns.DISPLAY_NAME, "jetpack${Date().time}.txt"
            )//不要插入相同名称的文件，因为有一定概率 insert 返回 null 就算返回uri也会有一定概率写不进去数据
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Files.FileColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + File.separator + "jetpack"
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                put(
                    MediaStore.Files.FileColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS + File.separator + "jetpack"
                )
            }
            //8.1 独占文件
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Files.FileColumns.IS_PENDING, 1
                )
            }

        }//每次运行都会添加一个，jetpack(1).txt  jetpack(2).txt  jetpack(3).txt.........
        println("MediaStore.Files = ${getFileContentUri()}")
        val fileUri = contentResolver.insert(getFileContentUri(), fileValue)!!.also {
            println("fileUri-----${it.toString()}")
            contentResolver.openFileDescriptor(it, "w").use { fileDescriptor ->
                OutputStreamWriter(
                    ParcelFileDescriptor.AutoCloseOutputStream(fileDescriptor), StandardCharsets.UTF_8
                ).use { write ->
                    write.write("1234一二三四")
                }
            }

            //8.1 独占文件 ，写完之后放开
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                fileValue.clear()
                fileValue.put(MediaStore.Files.FileColumns.IS_PENDING, 0)
                contentResolver.update(it, fileValue, null, null).apply { println("IS_PENDING update ----$this") }
            }
        }
        return fileUri
    }

    private fun updateFile1(fileUri: Uri) {
        val updateValue = when {
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> ContentValues().apply {
                println("android 10 " + Environment.DIRECTORY_DOWNLOADS + File.separator + "update")
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + "update")
                put(MediaStore.Files.FileColumns.IS_PENDING, 0)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> ContentValues().apply {
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + File.separator + "update")
                put(MediaStore.Files.FileColumns.IS_PENDING, 0)
            }
            else -> ContentValues().apply {//小于10
                println(
                    "小于10 绝对路径" + Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOCUMENTS + File.separator + "update" + File.separator + "123"
                )
                //通过剪裁 fileUri 得到 表格的uri 然后搜索，得到DISPLAY_NAME
                put(
                    MediaStore.Video.Media.DATA,
                    Environment.getExternalStorageDirectory().absolutePath + File.separator + Environment.DIRECTORY_DOCUMENTS + File.separator + "update" + File.separator + "123"
                )
            }
        }

        fileUri.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.update(
                    fileUri, ContentValues().apply { put(MediaStore.Files.FileColumns.IS_PENDING, 1) }, null, null
                )
                contentResolver.update(fileUri, updateValue, null, null)

            }
        } ?: println("fileUri----$fileUri")
    }

    private fun updateFile2() {
        queryFirstFile()?.let { it ->
            try {
                OutputStreamWriter(
                    ParcelFileDescriptor.AutoCloseOutputStream(
                        contentResolver.openFileDescriptor(
                            it.contentUri, "wa"
                        )
                    )
                )
//                lifecycleScope.launch(Dispatchers.IO) {
//                    contentResolver.delete(
//                        it.contentUri, "${MediaStore.Files.FileColumns._ID} = ?", arrayOf(it.id.toString())
//                    ).apply { println("delete---$this") }
//                }
            } catch (securityException: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val recoverableSecurityException =
                        securityException as? RecoverableSecurityException ?: throw securityException
                    println("recoverableSecurityException-----${recoverableSecurityException.message}")
                    val intentSender = recoverableSecurityException.userAction.actionIntent.intentSender.let { sender ->
                        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
                            val thumbnail: Bitmap? = activityResult.data?.getParcelableExtra("data")
                            val fullPhotoUri: Uri? = activityResult.data?.data
                            println("thumbnail------$thumbnail    fullPhotoUri-----$fullPhotoUri")
                        }.launch(IntentSenderRequest.Builder(sender).build())
                    }
                } else {
                    throw RuntimeException(securityException.message, securityException)
                }
            }
        } ?: println("queryFirstFile()返回null")
    }

    private fun queryFirstFile() = contentResolver.query(
        getImageContentUri(), null, null, null, null
    ).use {
        if (it != null && it.count > 0 && it.moveToNext()) {
            println("queryFirstFile path -----${it.getString(it.getColumnIndexOrThrow(MediaStore.DownloadColumns.DATA))}")
            val uri = ContentUris.withAppendedId(
                getImageContentUri(), it.getLong(it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
            ).apply { println("queryFirstFile uri-----$this") }
            MediaStoreFile(
                it.getLong(it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)),
                it.getString(it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)),
                Date(TimeUnit.SECONDS.toMillis(it.getLong(it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)))),
                uri
            )
        } else {
            null
        }
    }


    private fun queryContent() {
        val videoList = mutableListOf<Video>()

        val collection = getVideoContentUri()
//        val projection: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            arrayOf(
//                MediaStore.Video.Media._ID,
//                MediaStore.Video.Media.DISPLAY_NAME,
//                MediaStore.Video.Media.DURATION,
//                MediaStore.Video.Media.SIZE,
//                MediaStore.Video.Media.RELATIVE_PATH,
//                MediaStore.Video.Media.DATA
//            )
//        } else {
//            arrayOf(
//                MediaStore.Video.Media._ID,
//                MediaStore.Video.Media.DISPLAY_NAME,
//                MediaStore.Video.Media.DURATION,
//                MediaStore.Video.Media.SIZE,
//                MediaStore.Video.Media.DATA
//            )
//        }
        println("collection-----$collection-------")
        // Show only videos that are at least 5 minutes in duration.
        val selection = "${MediaStore.Video.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(15, TimeUnit.SECONDS).toString()
        )
        println("selectionArgs----${selectionArgs[0]}")
        // Display videos in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC "
        lifecycleScope.launch(Dispatchers.Default) {
//            val query = contentResolver.query(
//                collection, projection, "_id = ?", arrayOf("24969"), sortOrder
//            )
            val query = contentResolver.query(
                collection, null, selection, selectionArgs, sortOrder
            )
            println("query----${query?.count}")
            query?.use { cursor ->
                // Cache column indices.
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val pathColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)// 不实用，返回 DCIM/Camera/
                val dataColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)// 返回绝对路径 /storage/emulated/0/DCIM/Camera/VID_20220526_215803.mp4
                while (cursor.moveToNext()) {
                    // Get values of columns for a given video.
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val duration = cursor.getInt(durationColumn)
                    val size = cursor.getInt(sizeColumn)
                    val path = cursor.getString(dataColumn)
//                    println("${cursor.getString(pathColumn)}")

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                    )
                    // Load thumbnail of a specific media item. 加载缩略图，怎么兼容？？？
                    //5. 加载视频缩略图
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        val thumbnail: Bitmap = contentResolver.loadThumbnail(
//                            contentUri, Size(640, 480), null
//                        )
//                    }
                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    videoList += Video(
                        contentUri, name, duration, size, path
                    ).apply { println("$this ${cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.AUTHOR))}") }
                }

            }
        }
    }

    private fun getVideoContentUri() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {//android 10 api29
//        MediaStore.getExternalVolumeNames(this).forEach { println("getExternalVolumeNames---$it") }
        MediaStore.Video.Media.getContentUri(
//            MediaStore.VOLUME_EXTERNAL          //content://media/external/video/media
            MediaStore.VOLUME_EXTERNAL_PRIMARY    //content://media/external_primary/video/media
            //TODO 在k30pro运行发现VOLUME_EXTERNAL 仍然能添加项目，与描述不符
        )
    } else {
        println("小于 android10 使用 MediaStore.Video.Media.EXTERNAL_CONTENT_URI")
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI // //content://media/external/video/media 动态赋值，运行起来就有值了，但是不知道在哪里赋值
//        MediaStore.Video.Media.getContentUri("external") //另外两种写法
//        Uri.parse("content://media/external/images/media")
    }

    private fun getFileContentUri() = when {
        (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) -> MediaStore.Downloads.getContentUri(
//            MediaStore.VOLUME_EXTERNAL          //content://media/external/file
            MediaStore.VOLUME_EXTERNAL_PRIMARY    //content://media/external_primary/file
        )
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) -> MediaStore.Files.getContentUri(
            MediaStore.VOLUME_EXTERNAL          //content://media/external/file
//            MediaStore.VOLUME_EXTERNAL_PRIMARY    //content://media/external_primary/file
        )
        // //content://media/external/video/media 动态赋值，运行起来就有值了，但是不知道在哪里赋值
        else -> MediaStore.Files.getContentUri("external")//或者是这种写法
//        else->  Uri.parse("content://media/external/files")
    }.apply { println("getFileContentUri----$this") }

    private fun getImageContentUri() = when {
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) -> MediaStore.Images.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL          //content://media/external/file
//            MediaStore.VOLUME_EXTERNAL_PRIMARY    //content://media/external_primary/file
        )
        // //content://media/external/video/media 动态赋值，运行起来就有值了，但是不知道在哪里赋值
        else -> MediaStore.Images.Media.getContentUri("external")//或者是这种写法
//        else->  Uri.parse("content://media/external/files")
    }.apply { println("getImageContentUri----$this") }
}

