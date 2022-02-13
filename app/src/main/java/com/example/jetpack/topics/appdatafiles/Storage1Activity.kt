package com.example.jetpack.topics.appdatafiles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.jetpack.R

/**
 * 续 上接Storage1Activity
 *   1.2 共享存储：存储您的应用打算与其他应用共享的文件，包括媒体、文档和其他文件。
 *   1.3 偏好设置：以键值对形式存储私有原始数据。
 *   1.4 数据库：使用 Room 持久性库将结构化数据存储在专用数据库中。在存储敏感数据（不可通过任何其他应用访问的数据）时，
 *       应使用内部存储空间、偏好设置或数据库。内部存储空间的一个额外优势是用户无法看到相应数据。
 */
class Storage1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage1)
    }
}