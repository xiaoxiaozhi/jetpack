package com.example.jetpack.topics.appdatafiles.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * 1. 该类必须带有 @Database 注解，该注解包含列出所有与数据库关联的数据实体的 entities 数组。
 * 2. 该类必须是一个抽象类，用于扩展 RoomDatabase。
 * 3. 对于与数据库关联的每个 DAO 类，数据库类必须定义一个具有零参数的抽象方法，并返回 DAO 类的实例。 查看代码 UserD奥
 * note:在实例化 AppDatabase 对象时应遵循单例设计模式。每个 RoomDatabase 实例的成本相当高，而您几乎不需要在单个进程中访问多个实例。
 * 如果您的应用在多个进程中运行，请在数据库构建器调用中包含 enableMultiInstanceInvalidation()。这样，如果您在每个进程中都有一个 AppDatabase 实例，
 * 可以在一个进程中使共享数据库文件失效，并且这种失效会自动传播到其他进程中 AppDatabase 的实例。
 * TODO 多进程是什么意思
 */
@Database(
//    entities = [User::class, Book::class],
//    views = [DataView::class],
    entities = [User::class],
    version = 1
//    autoMigrations = [AutoMigration(from = 3, to = 4)]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}