package com.example.jetpack.topics.userdataidentity

import android.Manifest
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Profile
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.database.getStringOrNull
import androidx.lifecycle.lifecycleScope
import com.example.jetpack.R
import com.example.jetpack.havePermissions
import com.example.jetpack.haveStoragePermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * [csdn联系人](https://blog.csdn.net/csdn372301467/article/details/61423397)
 * 运行时权限访问之前要先申请 READ_CONTACTS 这是个运行时权限，要在应用时申请
 * 1. 联系人提供程序组织架构
 *    有三种类型的联系人数据，每种数据都对应提供程序提供的一个表
 *    1.1 Contacts （id、姓名来自于Raw表)等等。
 *        名称引用自RawContacts表
 *        [操作](https://developer.android.google.cn/reference/android/provider/ContactsContract.Contacts#operations)
 *    1.2 RawContacts 账户<--->联系人 关系表。k30pro 只有一个小米账户 这个表的数据时多个账户的聚合包括(id、姓名)
 *        登录不同账户创建联系人，RawContacts会出现多条记录，例如 账户1----联系人1   账户2----联系人1 [这个例子清楚的说明了RawContact数据来源](https://developer.android.google.cn/guide/topics/providers/contacts-provider#RawContactsExample)
 *         TODO 删除的联系人为什么DELETE是 1 而不是这条数据不再显示
 *    1.3 Data Data 是一个通用表，可以保存任何类型的联系人数据。联系人数据包括 （id、姓名、电话号码、邮件、生日、周年纪念日） 等等 [联系人编辑界面可以看到各种联系人数据](http://m.qpic.cn/psc?/V54UN84b0OHfN43eIX713mRT5H07gkzM/bqQfVz5yrrGYSXMvKr.cqUtjPtoB1Qm7uBpoXlyvgxV2*Kbu0NtiVa5RKvuN4J3tABzhvIgM92mPqbKG2frr9gzYVhFFTgq*bF3z3.1tY4s!/b&bo=OARgCQAAAAABB3U!&rf=viewer_4)
 *         存储在给定行中的数据类型由该行的 MIMETYPE 值指定，该值确定通用列 DATA1到 DATA15的含义
 *         DATA表的数据由 MIMETYPE决定。例如，如果数据类型是 Phone.CONTENT_ITEM_TYPE，那么列 DATA1存储电话号码，但是如果数据类型是 Email.CONTENT_ITEM_TYPE，那么 DATA1存储电子邮件地址
 *         DATA的数据类型定义在 CommonDataKinds 常用的有Email 、 Phone
 *         系统不允许 app添加联系人
 *    note:这三个表不支持limit子句， 可能ContactsContract的ContentProvider不支持limit
 * 2. 来自同步适配器的数据
 *    如果您想将服务数据传送至联系人提供程序，则需编写您自己的同步适配器
 * 3. 用户资料
 *    ContactsContract.Contacts 表有一行数据，其中包含设备用户的个人资料.该行的 IS_USER_PROFILE 列。若该联系人是用户个人资料，则此列设为“1”。
 *    Stack Overflow发现 READ_Profile 已经被删除，换成 ndroid.permission.GET_ACCOUNTS，但是这样仍然查询不到用户资料
 *    创建账户 请看AccountManager
 *    TODO 官网提供的例子还是在Contacts表上都找不到用户资料 k30pro，后者查询后发现没有 IS_USER_PROFILE = 1的行
 * 4. Contacts provider 访问
 *    Contacts provider提供操作，其作用类似于各个表之间的数据库连接。
 *    4.1 与之前的查询方式不同，这个是根据联系人的URI查询 联系人姓名 电话号码。 TODO 想要这些信息 在DATA表中也可以查询为什么非要用这个表？？？
 *    4.2 批量操作
 *    4.3 通过Intent执行检索和修改
 *        与4.2相比更适合单个联系人的修改,不需要权限.只给出一个示例，具体操作看 @see IntentActivity
 * TODO 联系人同步适配器 AbstractThreadedSyncAdapter(继承这个类实现联系人同步适配器) 有什么作用
 */
class ContactsProviderActivity : AppCompatActivity() {
    private val openSetting = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private val selectContact = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val contactUri = it.data?.data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts_provider)
        if (haveStoragePermission(Manifest.permission.READ_CONTACTS)) {
            query()
        } else {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    println("权限通过")
                    query()
                } else {
                    showRational()
                }
            }.apply {
                launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun query() {
        lifecycleScope.launch(Dispatchers.Default) {
            //queryImages() //该例测试了limit子句对媒体文件起作用，对联系人不起作用，大概是联系人的contentProvider不支持
            //1.1
//            queryContacts()
            //1.2
//            queryRaw()
            //1.3
            //queryData()
            //3.查询用户资料
//            queryProfile()
            //4.1 利用Contacts.Entity表 根据ID查询 联系人姓名 电话号码
            queryEntry()
            //4.3 通过Intent执行检索和修改
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = ContactsContract.Contacts.CONTENT_TYPE
            }
            selectContact.launch(intent)
        }
    }

    private fun queryEntry() {
//        println(ContentUris.withAppendedId(Contacts.CONTENT_URI, 1).toString() + "------------")
//        println(Uri.withAppendedPath(Contacts.CONTENT_URI, "1").toString() + "-----------------")
        val contactUri = Uri.withAppendedPath(
            Uri.withAppendedPath(Contacts.CONTENT_URI, "1"), Contacts.Entity.CONTENT_DIRECTORY
        )
        //或者
//        val contactUri = Uri.withAppendedPath(
//            ContentUris.withAppendedId(Contacts.CONTENT_URI, 1), Contacts.Entity.CONTENT_DIRECTORY
//        )
        val projection: Array<String> = arrayOf(
            Contacts.Entity.DATA1, Contacts.Entity.MIMETYPE, Contacts.Entity.CONTACT_ID, Contacts.Entity.RAW_CONTACT_ID
        )
        contentResolver.query(contactUri, projection, null, null).use { cursor ->
            println("cursor----$cursor count----${cursor?.count}")
            cursor?.takeIf { it.count > 0 }?.apply {
                while (moveToNext()) {
                    val data = cursor.getStringOrNull(cursor.getColumnIndex(Contacts.Entity.DATA1))
                    val id = cursor.getLong(cursor.getColumnIndex(Contacts.Entity.RAW_CONTACT_ID))
                    val contactId = cursor.getLong(cursor.getColumnIndex(Contacts.Entity.CONTACT_ID))
                    val type = cursor.getString(cursor.getColumnIndex(Contacts.Entity.MIMETYPE))
                    println("contactId---$contactId RAW_CONTACT_ID----$id type-----$type data----$data")
                }
            }
        }
    }

    private fun queryProfile() {
        // Sets the columns to retrieve for the user profile
        val bundle = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, "${ContactsContract.Contacts.IS_USER_PROFILE} = ?")
            putStringArray(
                ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, arrayOf("1")
            )
        }
        val projection = arrayOf(
            Profile._ID,
            Profile.DISPLAY_NAME_PRIMARY,
            Profile.LOOKUP_KEY,
            Profile.PHOTO_THUMBNAIL_URI,
            Profile.IS_USER_PROFILE //用户个人资料，则此列设为“1”。
        )
        val uri = Uri.withAppendedPath(Profile.CONTENT_URI, Contacts.Data.CONTENT_DIRECTORY)
// Retrieves the profile from the Contacts Provider
//        val myCursor = contentResolver.query(
//            uri, projection, null, null, null
//        )
        val myCursor = contentResolver.query(
            Profile.CONTENT_URI, projection, bundle, null
        )
        println("cursor----$myCursor  count----${myCursor?.count}")
//        myCursor?.takeIf { it.count>0 }?.also {cursor ->
//
//        }
        myCursor?.close()
    }

    private fun queryContacts() {
        val bundle = Bundle().apply {}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            contentResolver.query(Contacts.CONTENT_URI, null, null, null).use { cursor ->
                println("cursor-----$cursor count-----${cursor?.count}")
                cursor?.takeIf { it.count > 0 }?.apply {
                    while (moveToNext()) {
                        val id = getLong(getColumnIndexOrThrow(Contacts._ID))
                        val lookupKey = getString(getColumnIndexOrThrow(Contacts.LOOKUP_KEY))
                        val displayName = getString(getColumnIndexOrThrow(Contacts.DISPLAY_NAME))
                        val isUser = getInt(getColumnIndexOrThrow(Contacts.IS_USER_PROFILE))
                        println("lookupKey---$lookupKey id----$id displayName---$displayName  isUser----$isUser")
                    }
                }
            }
        }

    }

    private fun queryRaw() {
        contentResolver.query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null).use { cursor ->
            println("cursor-----$cursor count-----${cursor?.count}")
            cursor?.takeIf { it.count > 0 }?.apply {
                while (moveToNext()) {
//                        getInt(getColumnIndexOrThrow(ContactsContract.RawContacts.DELETED))//默认情况下为“0”，如果该行已被标记为删除，则为“1”。
//                        "最后联系时间---${getInt(getColumnIndexOrThrow(ContactsContract.RawContacts.TIMES_CONTACTED))}" //谷歌不希望开发者获取这个值，没有替代方法
                    val pair = Pair(
                        getString(getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_NAME)),//账户名称 k30pro显示小米ID 一个设备有多个账户
                        getString(getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_TYPE)),//账户类型 k30pro显示 com.xiaomi
                    )
                    val id = getLong(getColumnIndexOrThrow(ContactsContract.RawContacts._ID))
                    val contactName =
                        getString(getColumnIndexOrThrow(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY))
                    val delete = getInt(getColumnIndexOrThrow(ContactsContract.RawContacts.DELETED))
                    println(
                        "联系人名称---${contactName} id = $id 属于这个账户--->$pair 状态---${
                            when (delete) {
                                0 -> "正常"
                                else -> "删除"
                            }
                        }"
                    )
                }
            }?.use {}
        }
    }

    private fun queryData() {
        val projection = arrayOf(
            ContactsContract.Data._ID,
            ContactsContract.Data.RAW_CONTACT_ID,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1
        )
//        val projection = arrayOf(ContactsContract.Data.RAW_CONTACT_ID, ContactsContract.Data.MIMETYPE)
        //从 Android 8.0开始建议使用Bundle 存放条件,Android 11 开始 LIMIT and OFFSET 必须放在 Bundle 中.实际测试发现 android8 到 android 11 bundle中limit不起作用，要放在order中
        val myCursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bundle = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, 10)//ContactsContract 下的contentProvider不支持LIMIT
                putInt(ContentResolver.QUERY_ARG_OFFSET, 0)
                putString(
                    ContentResolver.QUERY_ARG_SQL_SELECTION,
                    "${ContactsContract.Data.MIMETYPE} = ? or ${ContactsContract.Data.MIMETYPE} = ?"
                )
                putStringArray(
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, arrayOf(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                    )
                )
//                putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Files.FileColumns.DATE_ADDED))//按某个列排序
//                putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_ASCENDING)//升序ASCENDING  降序DESCENDING
            }
            //返回支持取消的结果集，为了性能考虑 1. 提供projection参数避免查询不必要的数据 2. 在 selection参数中使用 phone=? 而不是清楚的的提供值 phone = 13813930373
            contentResolver.query(ContactsContract.Data.CONTENT_URI, projection, bundle, null)//

        } else {
            val order = " LIMIT 10 OFFSET 0"
            contentResolver.query(ContactsContract.Data.CONTENT_URI, projection, null, null, order)
        }
        myCursor.use { cursor ->
            println("cursor-----$cursor count-----${cursor?.count}")
            cursor?.takeIf { it.count > 0 }?.apply {
                while (moveToNext()) {
                    val rawId = getLong(getColumnIndexOrThrow(ContactsContract.Data.RAW_CONTACT_ID))
                    val mime = getString(getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE))
                    val data1 =
                        getString(getColumnIndexOrThrow(ContactsContract.Data.DATA1))//方便起见还有别名，例如 电话类型。DATA1 的别名就是 Phone.NUMBER
                    when (mime) {
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> ""
                    }
                    println("raw_id----$rawId  MIME-----$mime data1----$data1")
                }
            }
        }
    }

    private fun queryImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bundle = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, 100)
            }
            contentResolver.query(
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
                arrayOf(ContactsContract.Contacts._ID),
                bundle,
                null
            )?.count.also {
                println("count--------$it")
            }
        }
    }


    private fun showRational() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            println("用户拒绝后显示原因")//
        } else {
            println("用户点击了禁止再次访问")// 这时候要 导航到权限设置窗口，手动设置
            // 必须在 LifecycleOwners的STARTED 之前调用 registerForActivityResult. 否则报错 推荐用委托形式
            openSetting.launch(Intent().apply {
                action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.parse("package:$packageName")
            })
        }
    }
}