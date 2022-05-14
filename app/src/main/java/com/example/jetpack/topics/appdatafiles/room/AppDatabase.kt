package com.example.jetpack.topics.appdatafiles.room

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.*


/**
 * 1. 该类必须带有 @Database 注解，该注解包含列出所有与数据库关联的数据实体的 entities 数组。
 * 2. 该类必须是一个抽象类，用于扩展 RoomDatabase。
 * 3. 对于与数据库关联的每个 DAO 类，数据库类必须定义一个具有零参数的抽象方法，并返回 DAO 类的实例。 查看代码 UserD奥
 * note:在实例化 AppDatabase 对象时应遵循单例设计模式。每个 RoomDatabase 实例的成本相当高，而您几乎不需要在单个进程中访问多个实例。
 * 如果您的应用在多个进程中运行，请在数据库构建器调用中包含 enableMultiInstanceInvalidation()。这样，如果您在每个进程中都有一个 AppDatabase 实例，
 * 可以在一个进程中使共享数据库文件失效，并且这种失效会自动传播到其他进程中 AppDatabase 的实例。
 * TODO 多进程是什么意思
 */
/**
 * The Room database for this app
 */
@Database(
    entities = [User::class, Word::class],
    version = 2
//    exportSchema = false//如果不设置这个属性 [会报错](https://blog.csdn.net/hexingen/article/details/78725958)
)
//@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun wordDao(): WordDao


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
                .addCallback(
                    object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            println("AppDatabase-------------数据库被第一次创建时调用---------------")
                            //填充数据
                            OneTimeWorkRequestBuilder<FillDataWoker>()
                                .build().apply {
                                    WorkManager.getInstance(context).enqueue(this)
                                }
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            println("AppDatabase-------------数据库被打开时调用---------------")
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            println("AppDatabase-------------数据库被破坏性迁移时调用---------------")
                        }
                    }
                )
                .build()
//            return Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build() //数据库将在系统内存中创建，如果您终止了该应用程序（杀死进程），则数据库将被删除并且数据将不会持久保存。可以在测试时使用
        }

        //[sqlite官网](https://www.sqlite.org/index.html) 没找到具体实例
        private var migrations1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("Alter table $USER_TABLE_NAME add COLUMN age INTEGER")
            }
        }
    }

    //    open fun getInMemoreyDatabase(context: Context): RoomDb? {
//        if (INSTANCE == null) {
//            synchronized(sLock) {
//                if (INSTANCE == null) {
//                    INSTANCE = Room.inMemoryDatabaseBuilder(
//                        context.applicationContext,
//                        RoomDb::class.java
//                    ).build()
//                }
//            }
//        }
//        return INSTANCE
//    }
    class FillDataWoker(val context: Context, workerParameters: WorkerParameters) :
        CoroutineWorker(context, workerParameters) {
        override suspend fun doWork(): Result {
            AppDatabase.getInstance(context).wordDao().insert(
                listOf(
                    Word("hello"), Word("word"),
                    Word("TODO")
                )
            )
            return Result.success()
        }

    }
}
