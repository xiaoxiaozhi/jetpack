package com.example.jetpack.topics.appdatafiles.contentprovider

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.UserDictionary
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.jetpack.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 1. 在下列情况下，您需要自定义内容提供程序：
 *    1.1 您希望在自己的应用中实现自定义搜索建议
 *    1.2 您需要使用内容提供程序向桌面小部件公开应用数据
 *    1.2 您希望将自己应用内的复杂数据或文件复制并粘贴到其他应用中
 * 2. 内容URI
 *    content://user_dictionary/words
 *    user_dictionary 字符串是提供程序的授权，words 字符串是表的路径。
 *    许多提供程序都允许通过将 ID 值追加到 URI 末尾以访问表中的单个行  ContentUris.withAppendedId(UserDictionary.Words.CONTENT_URI, 4)
 *    如需从用户字典中检索 _ID 为 4 的行
 * 3. 从提供程序检索数据
 *    如需从提供程序检索数据，请按照以下基本步骤执行操作
 *    3.1 请求对提供程序的读取访问权限
 *    3.2 构建查询
 *     note:由于API 23开始，UserDictionary.Words.CONTENT_URI 只能通过 IME 和[拼写检查器访问] 所以官网例子无法运行
 *    note：ContentResolver.query()应在线程中执行
 * 4. ContentProvider数据类型
 *    cursor?.getType()//获取该列的数据类型 contentResolver.getType(uri) 获取数据类型
 * 5. ContentProvider访问方式
 *    5.1 批量访问 @see MyContentProvider#applyBatch()
 *    5.2 contentResolver.query 异步查询（在线程中调用该代码实现异步）
 *    5.3 通过Intent访问数据 查看 @see IntentActivity
 * 6.使用“存储访问框架”打开文件 SAF 查看 @see IntentActivity
 *   借助 SAF，用户可轻松浏览和打开各种文档、图片及其他文件，而不用管这些文件来自其首选文档存储提供程序中的哪一个。跨所有应用和提供程序以统一的方式浏览文件并访问最近用过的文件
 *   SAF包含一下几种
 *   6.1 文档提供程序  DocumentsProvider
 *   6.2 客户端应用 - 一种定制化的应用，它会调用 ACTION_CREATE_DOCUMENT、ACTION_OPEN_DOCUMENT 和 ACTION_OPEN_DOCUMENT_TREE intent 操作并接收文档提供程序返回的文件。
 *   6.3 选择器 - 一种系统界面，可让用户访问所有文档提供程序内满足客户端应用搜索条件的文档。
 */
class ContentProviderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_provider)
        lifecycleScope.launch(Dispatchers.Default) {
            val singleUri: Uri = ContentUris.withAppendedId(UserDictionary.Words.CONTENT_URI, 4)
            println("singleUri-----$singleUri")
            //3.2 构建查询
            buildQuery()
            //4. ContentProvider数据类型
            ContactsContract.AUTHORITY
        }
    }

    private fun buildQuery() {
        //note 从 API 23开始，UserDictionary.Words.CONTENT_URI 只能通过 IME 和[拼写检查器访问](https://developer.android.com/guide/topics/text/spell-checker-framework)
        contentResolver.query(
            UserDictionary.Words.CONTENT_URI, arrayOf(
                UserDictionary.Words.WORD, UserDictionary.Words.FREQUENCY, UserDictionary.Words.LOCALE
            ), null, arrayOf(""), null
        ).use { cursor ->
            cursor?.let { println("cursor.count-----${it.count}") } ?: println("cursor-----null")
//                cursor?.getType()//获取该列的数据类型
//                cursor?.takeIf { it.count > 0 }?.apply {
//                    while (moveToNext()) {
//                        UserWord(
//                            getColumnName(getColumnIndexOrThrow(UserDictionary.Words.WORD)),
//                            getLong(getColumnIndexOrThrow(UserDictionary.Words.APP_ID)),
//                            getInt(getColumnIndexOrThrow(UserDictionary.Words.FREQUENCY)),
//                            getString(getColumnIndexOrThrow(UserDictionary.Words.LOCALE))
//                        ).also(::println)
//                    }
//                }
        }
    }

}