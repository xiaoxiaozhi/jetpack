package com.example.jetpack.topics.appdatafiles.room

import android.content.Context
import androidx.databinding.adapters.Converters
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

/**
 * 1. 该类必须带有 @Database 注解，该注解包含列出所有与数据库关联的数据实体的 entities 数组。
 * 2. 该类必须是一个抽象类，用于扩展 RoomDatabase。
 * 3. 对于与数据库关联的每个 DAO 类，数据库类必须定义一个具有零参数的抽象方法，并返回 DAO 类的实例。 查看代码 UserD奥
 * note:在实例化 AppDatabase 对象时应遵循单例设计模式。每个 RoomDatabase 实例的成本相当高，而您几乎不需要在单个进程中访问多个实例。
 * 如果您的应用在多个进程中运行，请在数据库构建器调用中包含 enableMultiInstanceInvalidation()。这样，如果您在每个进程中都有一个 AppDatabase 实例，
 * 可以在一个进程中使共享数据库文件失效，并且这种失效会自动传播到其他进程中 AppDatabase 的实例。
 * TODO 多进程是什么意思
 */
//@Database(
////    entities = [User::class, Book::class],
////    views = [DataView::class],
//    entities = [User::class],
//    version = 1
////    autoMigrations = [AutoMigration(from = 3, to = 4)]
//)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun userDao(): UserDao
//}

/**
 * The Room database for this app
 */
@Database(
    entities = [User::class],
    version = 2
//    exportSchema = false//如果不设置这个属性 [会报错](https://blog.csdn.net/hexingen/article/details/78725958)
)
//@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao


    // sunFlower---项目里面的数据库创建方式
    companion object {
        val USER_TABLE_NAME = "user"
        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "jetpack")
//                .allowMainThreadQueries()//数据库的增删改查都不能再主线程允许，调用该方法后强制可以
                .fallbackToDestructiveMigration()//删除所有数据，并创建新数据库
                .addMigrations(migrations1_2)
//                .addCallback(
//                    object : RoomDatabase.Callback() {
//                        override fun onCreate(db: SupportSQLiteDatabase) {
//                            super.onCreate(db)
//                            val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>()
//                                .setInputData(workDataOf(KEY_FILENAME to PLANT_DATA_FILENAME))
//                                .build()
//                            WorkManager.getInstance(context).enqueue(request)
//                        }
//                    }
//                )
                .build()
        }

        //[sqlite官网](https://www.sqlite.org/index.html) 没找到具体实例
        private var migrations1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table $USER_TABLE_NAME add COLUMN age INTEGER")
            }
        }
    }

}
