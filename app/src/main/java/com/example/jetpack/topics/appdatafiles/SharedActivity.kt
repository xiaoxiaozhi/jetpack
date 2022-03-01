package com.example.jetpack.topics.appdatafiles

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R

/**
 * 共享存储
 * 存储您的应用打算与其他应用共享的文件，包括媒体、文档和其他文件
 * 1. 多媒体文件
 *    为了提供更丰富的用户体验，许多应用提供和允许访问位于外部存储卷上的媒体。通过ContentProvider媒体库，可以更轻松地检索和更新这些媒体文件。
 *    即使您的应用已卸载，这些文件仍会保留在用户的设备上。 查看 MediaRelatedActivity 类
 * 2. 文档
 * 3. 数据集 在 Android 11+及更高版本中，系统会缓存多个应用可能使用的大型数据集。这些数据集可为机器学习和媒体播放等用例提供支持。
 *
 */
class SharedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared)
    }
}