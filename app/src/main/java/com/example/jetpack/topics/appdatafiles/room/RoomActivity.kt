package com.example.jetpack.topics.appdatafiles.room

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jetpack.databinding.ActivityRoomBinding
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * [掘金文章](https://juejin.cn/post/7033656219369734180)
 * 引入Room 查看project/build.gradle ：kotlin1.6.10 、gradle插件7.1.0  app/buil.gradle 添加 id 'kotlin-kapt' 通过kapt 添加注解
 * 1. 主要组件
 *    1.1 数据库类：用于保存数据库并作为应用持久性数据底层连接的主要访问点。查看 AppDatabase 类
 *    1.2 数据实体:用于表示应用的数据库中的表。 查看代码 User
 *    1.3 数据访问对象(DAO):提供您的应用可用于查询、更新、插入和删除数据库中的数据的方法。 查看代码 UserDao,代码生成位置app/build/generated/ap_generated_source/debug(release)/out/your packagename/
 *    1.4 用法：从Room.databaseBuilder数据库操作接口--->数据表操作Dao--->增删改查数据表
 * 2. 定义对象之间的关系
 *    [实际开发遇到问题的时候可以看](https://developer.android.google.cn/training/data-storage/room/relationships)
 *    多表联合查询、
 * 2. 预填充数据：从位于应用 assets/ 目录中的任意位置的预封装数据库文件预填充 Room 数据库、
 *    2.1 从asset下面加载数据库
 *    2.2 从文件系统加载系统
 * 3. 迁移数据库
 *    3.1 自动迁移：Room 在 2.4.0-alpha01 及更高版本中支持自动迁移。如果您的应用使用的是较低版本的 Room，则必须手动定义迁移。
 *        如需声明两个数据库版本之间的自动迁移，请添加autoMigrations = [AutoMigration(from = 1, to = 2) 查看代码 APPDatabase
 *    3.2 当以下情况发生时：删除或重命名表、删除或重命名列，自动迁移会报错
 *
 * 4. 从数据库到RecyclerVIew
 *    [](https://developer.android.google.cn/codelabs/android-room-with-a-view-kotlin?hl=zh-cn#0)
 * TODO 数据库加密
 * TODO LiveData与Flow区别要看
 * TODO 如果值一样LiveData 不能判断吗？ Flow可以判断 [](https://juejin.cn/post/7007602776502960165#heading-20)
 */
@AndroidEntryPoint
class RoomActivity : AppCompatActivity() {
    lateinit var binding: ActivityRoomBinding

//    @Inject
//    lateinit var repository: WordRepository
//实践表明即使viewmodel不加@HiltViewModel，也可以用viewModels实例化viewmode，那么实例化后的实例有什么区别吗？
    private val viewModel: WordViewModel by viewModels<WordViewModel>()

    //    val viewModel: WordViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //1.1 创建数据库实例
        val db = AppDatabase.getInstance(this)

        //1.3 从数据库类获取DAO对象，与数据库交互
        val userDao = db.userDao()
        lifecycleScope.launch(Dispatchers.Default) {
            userDao.insertUsers(User("z", "x", 10, 15))
            userDao.insertUsers(User("z1", "x1", 10, 20))
            println("size---${userDao.getAll().size}")
            userDao.getAll().forEach {
                println("${it.firstName}----${it.lastName}----${it.languageId}-----")
            }
        }
        //4. 从数据库到RecyclerVIew
        with(binding) {
            viewModel.allWords.observe(this@RoomActivity) {
                if (it.isNotEmpty()) {
                    recyclerview.layoutManager = LinearLayoutManager(this@RoomActivity)
                    recyclerview.adapter =
                        WordListAdapter(it.map { word -> word.word }.toTypedArray())
                }
            }

            button1.setOnClickListener {
                startActivity(Intent(this@RoomActivity, RoomActivity2::class.java))
            }
        }

        //2. 预填充数据
//        Room.databaseBuilder(this, AppDatabase::class.java, "Sample.db")
//            .createFromAsset("database/myapp.db")//assets目录下相对路径
////            .createFromFile(File("mypath"))//Room 会创建指定文件的副本，而不是直接打开它，因此请确保您的应用具有该文件的读取权限。
//            .build()


    }
}