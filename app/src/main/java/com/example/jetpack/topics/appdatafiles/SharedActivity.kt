package com.example.jetpack.topics.appdatafiles

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.lifecycleScope
import com.example.jetpack.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * TODO k30pro(android12)上安装的app仍然可以在非专属目录上创建文件，这是怎么实现的
 * TODO 找一个权限框架，或者学习官网permission 章节
 * TODO contentResolver 在哪里实例化
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
 *   TODO k30pro android12 验证不取得 READ_EXTERNAL_STORAGE 权限无法访问MediaStore 这是为什么？？？？？
 *   如果您的应用以 Android 10（API 级别 29）或更高版本为目标平台，为了使您的应用从照片中检索未编辑的 Exif 元数据，您需要在应用的清单中声明 ACCESS_MEDIA_LOCATION 权限
 * 3.检查媒体库更新
 *   getVersion() 返回的版本是一个唯一字符串，该字符串会在媒体库发生重大变化时随之变化。如果返回的版本与上次同步的版本不同，请重新扫描并重新同步应用的媒体缓存
 * 4.加载文件缩略图
 *   TODO 待总结
 * 5.打开媒体文件
 *
 *
 */
class SharedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared)
        val videoList = mutableListOf<Video>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )

// Show only videos that are at least 5 minutes in duration.
        val selection = "${MediaStore.Video.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS).toString()
        )
        println("selectionArgs----${selectionArgs[0]}")
// Display videos in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"
        lifecycleScope.launch(Dispatchers.Default) {
            val query = contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            println("query----${query?.count}")
            query?.use { cursor ->
                // Cache column indices.
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

                while (cursor.moveToNext()) {
                    // Get values of columns for a given video.
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val duration = cursor.getInt(durationColumn)
                    val size = cursor.getInt(sizeColumn)

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    videoList += Video(contentUri, name, duration, size)
                    println("videoList.size-----${videoList.size}")
                    videoList.forEach { println("videoItem-----$it") }
                }
            }
        }

    }
}