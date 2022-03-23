package com.example.jetpack.topics.appdatafiles.room

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.jetpack.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 引入Room 查看project/build.gradle ：kotlin1.6.10 、gradle插件7.1.0  app/buil.gradle 添加 id 'kotlin-kapt' 通过kapt 添加注解
 * 1. 主要组件
 *    1.1 数据库类：用于保存数据库并作为应用持久性数据底层连接的主要访问点。查看 AppDatabase 类
 *    1.2 数据实体:用于表示应用的数据库中的表。 查看代码 User
 *    1.3 数据访问对象(DAO):提供您的应用可用于查询、更新、插入和删除数据库中的数据的方法。
 * 2. 预填充数据：从位于应用 assets/ 目录中的任意位置的预封装数据库文件预填充 Room 数据库、
 *    2.1 从asset下面加载数据库
 *    2.2 从文件系统加载系统
 * 3. 迁移数据库
 *    3.1 自动迁移：Room 在 2.4.0-alpha01 及更高版本中支持自动迁移。如果您的应用使用的是较低版本的 Room，则必须手动定义迁移。
 *        如需声明两个数据库版本之间的自动迁移，请添加autoMigrations = [AutoMigration(from = 1, to = 2) 查看代码 APPDatabase
 *    3.2 当以下情况发生时：删除或重命名表、删除或重命名列，自动迁移会报错
 */
class RoomActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)
        //1.1 创建数据库实例
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "jetpack"
        ).build()
        //1.3 从数据库类获取DAO对象，与数据库交互
        val userDao = db.userDao()
        lifecycleScope.launch(Dispatchers.Default) {
            userDao.insertUsers(User("z", "x", 10))
            userDao.insertUsers(User("z1", "x1", 10))
            println("size---${userDao.getAll().size}")
            userDao.getAll().forEach {
                println("${it.firstName}----${it.lastName}----${it.languageId}-----")
            }
        }

        //2. 预填充数据
//        Room.databaseBuilder(this, AppDatabase::class.java, "Sample.db")
//            .createFromAsset("database/myapp.db")//assets目录下相对路径
////            .createFromFile(File("mypath"))//Room 会创建指定文件的副本，而不是直接打开它，因此请确保您的应用具有该文件的读取权限。
//            .build()

    }
}