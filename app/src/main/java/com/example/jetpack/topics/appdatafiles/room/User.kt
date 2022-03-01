package com.example.jetpack.topics.appdatafiles.room

import android.graphics.Bitmap
import androidx.annotation.NonNull
import androidx.room.*

/**
 * 1. 每个数据实体(表)都必须带有 @Entity 注解的类 ,默认情况下，Room 将类名用作数据库表名。如果您希望表具有不同的名称，请设置 @Entity(tableName = "users")
 * 2. 每个 Room 实体都必须定义一个主键
 *    2.1 定义复合主键:如果您需要通过多个列的组合对实体实例进行唯一标识,@Entity(primaryKeys = ["firstName", "lastName"])
 * 3. 忽略字段
 *    如果实体继承了父实体的字段，则使用 ：@Entity(ignoredColumns = ["picture"]) 更容易 FIXME 用Ignore就报错暂时无解， 使用Ignore和ignoredColumns效果一样吗？、
 * 4. 提供表搜索支持 TODO 什么效果？
 *    4.1 提供全文搜索:如果您的应用需要通过全文搜索 (FTS) 快速访问数据库信息，请使用虚拟表（使用 FTS3 或 FTS4 SQLite 扩展模块）为您的实体提供支持。
 *        如需使用 Room 2.1.0 及更高版本中提供的这项功能，请将 @Fts3 或 @Fts4 注解添加到给定实体，代码如下
 *        如果你的应用程序有严格的磁盘空间要求，或者如果你的应用程序没有磁盘空间要求，请使用@Fts3
 *        4.1.1 如果表支持以多种语言，请使用 languageId 选项指定用于存储每一行语言信息的列：例如第一行英语，第二行汉语
 *    4.2 将特定列编入索引
 *        在不支持FTS3或者FTS4 的版本，您仍可以将数据库中的某些列编入索引，请在 @Entity 注解中添加 indices 属性 @Entity(indices = [Index(value = ["last_name", "address"])])
 * 5. 添加基于 AutoValue 的对象 TODO 将java类变成实体，待学习
 *    在 Room 2.1.0 +，您可以将基于 Java 的不可变值类（使用 @AutoValue 进行注解）用作应用数据库中的实体。您可以使用 @PrimaryKey、@ColumnInfo、@Embedded 和 @Relation
 *    为该类的抽象方法添加注解。但是，您必须在每次使用这些注解时添加 @CopyAnnotations 注解，以便 Room 可以正确解释这些方法的自动生成实现。 查看代码 UserJava
 *
 * note:：SQLite 中的表和列名称不区分大小写。 每个字段都要定义成可空类型，否则报错说返回不了
 */
//
//@Fts4(languageId = "lid") //Fts更多操作查看  FtsOptions
@Entity(tableName = "user")
data class User(

//    @ColumnInfo(name = "id") val id: Int?,
    @PrimaryKey @ColumnInfo(name = "first_name") val firstName: String,//每一行末尾都有逗号
    @ColumnInfo(name = "last_name") val lastName: String?,
//    @ColumnInfo(name = "name") val name: String?,
//    @ColumnInfo(name = "type") val type: String?,
//    @ColumnInfo(name = "departmentId") val departmentId: Long?,
//    @ColumnInfo(name = "age") val age: Int?,
//    @ColumnInfo(name = "region") val region: String?,
//    @Ignore val picture: Int?,//增加了忽略字段会报这个错https://stackoverflow.com/questions/44485631/room-persistence-errorentities-and-pojos-must-have-a-usable-public-constructor
    @ColumnInfo(name = "lid") val languageId: Int?

) {

//    @PrimaryKey(autoGenerate = true)
//    @ColumnInfo(name = "rowid")//Fts4的主键名必须是rowid
//    var uid: Long? = 0//主键 TODO 插入时，主键不是自己增加的吗？为什么插不进去
}
//open class User {
//    var picture: Bitmap? = null
//}
//
//@Entity(ignoredColumns = ["picture"])
//data class RemoteUser(
//    @PrimaryKey val id: Int,
//    val hasVpn: Boolean
//) : User()