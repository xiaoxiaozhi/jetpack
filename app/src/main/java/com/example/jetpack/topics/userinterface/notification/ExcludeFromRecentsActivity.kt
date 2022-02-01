package com.example.jetpack.topics.userinterface.notification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R

/**
 * 从通知过来，intent标志 FLAG_ACTIVITY_NEW_TASK和taskAffinity(非默认值)创建新实例、创建新任务，如果任务存在则创建新实例加入到这个任务
 *
 */
class ExcludeFromRecentsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exclude_from_recents)
    }
}