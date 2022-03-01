package com.example.jetpack.topics.appdatafiles.room

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * 1. DAO 剖析
 *    为防止查询阻止界面，Room 不允许在主线程上访问数据库。此限制意味着您必须将 DAO 查询设为异步
 *    通过定义数据访问对象 (DAO) 与存储的数据进行交互，必须为每个DAO添加注解 @Dao，有两种方式可以跟数据库进行交互，编写和不编写sql
 * 2. 不编写sql
 *    Room 提供了方便的注解，用于定义无需编写 SQL 语句即可执行简单插入、更新和删除的方法。如果您需要定义更复杂的插入、更新或删除操作，
 *    或者需要查询数据库中的数据，请改用查询方法。
 *    2.1 插入
 *        @Insert 方法的每个参数必须是带有 @Entity 注解的 Room 数据实体类的实例或数据实体类实例的集合。
 *        如果 @Insert 方法接收单个参数，则会返回 long 值，这是插入项的新 rowId。如果参数是数组或集合，则该方法应改为返回由 long 值组成的数组或集合
 *    2.2 更新
 *        @Update 方法接受数据实体实例作为参数。 以下代码展示了一个 @Update 方法示例，该方法尝试更新数据库中的一个或多个 User 对象
 *        Room 使用主键将传递的实体实例与数据库中的行进行匹配。如果没有具有相同主键的行，Room 不会进行任何更改。
 *        @Update 方法可以选择性地返回 int 值，该值指示成功更新的行数。
 *    2.3 删除
 *        Room 使用主键将传递的实体实例与数据库中的行进行匹配。如果没有具有相同主键的行，Room 不会进行任何更改。
 *        @Delete 方法可以选择性地返回 int 值，该值指示成功删除的行数。
 * 3. 查询方法
 *    使用 @Query 注解，您可以编写 SQL 语句并将其作为 DAO 方法公开。使用这些查询方法从应用的数据库查询数据，或者需要执行更复杂的插入、更新和删除操作
 *    3.1 返回表格列的子集 代码如下
 *    3.2 传递参数给查询 代码如下
 *    3.3 传递一组参数 代码如下
 *    3.4 查询多个表 代码如下
 *    3.5 返回映射子集对象
 * 4. 特殊返回值类型 TODO Paging 库学完回来看
 *    4.1 使用 Paging 库将查询分页
 *    4.2 直接光标访问
 *
 */
@Dao
interface UserDao {
    //2.1 插入一个或多个 实体类
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsers(users: User)//TODO 插入两个User为什么只查询到一个

    @Insert
    suspend fun insertBothUsers(user1: User, user2: User)

    @Insert
    suspend fun insertUsersAndFriends(user: User, friends: List<User>)

    //2.2 更新
    @Update
    suspend fun updateUsers(vararg users: User)

    //2.3 删除
    @Delete
    suspend fun deleteUsers(vararg users: User)

    //3. 查询方法
    @Query("SELECT * FROM user")
    suspend fun getAll(): List<User>
//
//    //3.1 返回表格列的子集
//    @Query("SELECT first_name, last_name FROM user")
//    fun loadFullName(): List<User>
//
//    // 传递参数给查询
//    @Query("SELECT * FROM user WHERE age > :minAge")
//    fun loadAllUsersOlderThan(minAge: Int): Array<User>
//
//    // 传递多个参数
//    @Query("SELECT * FROM user WHERE age BETWEEN :minAge AND :maxAge")
//    fun loadAllUsersBetweenAges(minAge: Int, maxAge: Int): Array<User>//传递多个参数
//
//    //3.2 多次引用同一个参数
//    @Query(
//        "SELECT * FROM user WHERE first_name LIKE :search " +
//                "OR last_name LIKE :search"
//    )
//    fun findUserWithName(search: String): List<User>//多次引用同一个参数
//
//    //3.3 传递一组参数
//    @Query("SELECT * FROM user WHERE region IN (:regions)")
//    fun loadUsersFromRegions(regions: List<String>): List<User>//传递一组参数，返回了相关地区的所有用户的相关信息：

    //3.4 查询多个表
//    @Query(
//        "SELECT * FROM book " +
//                "INNER JOIN loan ON loan.book_id = book.id " +
//                "INNER JOIN user ON user.id = loan.user_id " +
//                "WHERE user.name LIKE :userName"
//    )
//    fun findBooksBorrowedByNameSync(userName: String): List<Book>

//    //3.5 返回多重映射
//    @Dao
//    interface UserBookDao {
//        @Query(
//            "SELECT user.name AS userName, book.name AS bookName " +
//                    "FROM user, book " +
//                    "WHERE user.id = book.user_id"
//        )
//        fun loadUserAndBookNames(): LiveData<List<UserBook>>
//
//        // You can also define this class in a separate file.
//        data class UserBook(val userName: String?, val bookName: String?)
//    }
//
//    //3.5 如果不需要返回对象，二是返回map
//    @MapInfo(keyColumn = "userName", valueColumn = "bookName")
//    @Query(
//        "SELECT user.name AS username, book.name AS bookname FROM user " +
//                "JOIN book ON user.id = book.user_id"
//    )
//    fun loadUserAndBookNames(): Map<String, List<String>>
//
//
//    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<User>
//
//    @Query(
//        "SELECT * FROM user WHERE first_name LIKE :first AND " +
//                "last_name LIKE :last LIMIT 1"
//    )
//    fun findByName(first: String, last: String): User
//
//    @Insert
//    fun insertAll(vararg users: User)
//
//    @Delete
//    fun delete(user: User)
}