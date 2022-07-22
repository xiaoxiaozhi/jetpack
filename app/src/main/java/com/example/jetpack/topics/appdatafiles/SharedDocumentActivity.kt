package com.example.jetpack.topics.appdatafiles

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivitySharedDocumentBinding
import com.example.jetpack.getImageDocumentUri
import java.io.FileDescriptor
import java.io.IOException


/**
 * 这一章的几个小节都是通过存储访问框架SAF实现
 * 1. 创建新文件
 *    ACTION_CREATE_DOCUMENT 无法覆盖现有文件。如果您的应用尝试保存同名文件，系统会在文件名的末尾附加一个数字并将其包含在一对括号中。
 *    查看代码 @see DocumentFileActivity
 * 2. 打开文件
 *    note 在 Android 11 api30+您不能使用 ACTION_OPEN_DOCUMENT intent 操作来请求用户从以下目录中选择单独的文件：Android/data/ 和Android/obb/ 目录及其所有子目录
 *    查看代码 @see DocumentFileActivity
 * 3. 授予对目录内容的访问权限
 * 4. 在所选位置执行操作
 *    4.1 确定支持的操作
 *    4.2 保留权限
 *        当您的应用打开文件进行读取或写入时，系统会向应用授予对该文件的 URI 的访问权限，该授权在用户重启设备之前一直有效，
 *        如需在设备重启后保留对文件的访问权限并提供更出色的用户体验，您的应用可以“获取”系统提供的永久性 URI 访问权限
 *        note:如果关联的文档被移动或删除,takePersistableUriPermission也没有用处
 *    4.3 打开文档
 *        打开位图 getBitmapFromUri(uri)
 *        打开输入流  contentResolver.openInputStream(uri)
 *    4.4 修改文档
 *        保证Document.COLUMN_FLAGS的值 包含 FLAG_SUPPORTS_WRITE
 *    4.5 删除文档
 *        Document.COLUMN_FLAGS 包含 SUPPORTS_DELETE
 * 5.
 * [MIME 类型列表](https://www.runoob.com/http/mime-types.html)
 */
class SharedDocumentActivity : AppCompatActivity() {
    private val activityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        println("flag-----${it.data?.flags}")
        it.data?.data?.apply {
            println("返回Uri---$this")

            //4.2 保留权限---------------------------------------------
//            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//            contentResolver.takePersistableUriPermission(this, takeFlags)
            //--------------------------------------------------------

            //4.3 打开输入流-----------------------------------------
//            contentResolver.openInputStream(this)
            //--------------------------------------------------

            val document = DocumentFile.fromSingleUri(this@SharedDocumentActivity, this)
//            document.do
            println("documentUri----${document?.uri}")
            contentResolver.query(this, arrayOf(DocumentsContract.Document.COLUMN_FLAGS), null, null).use { cursor ->
                cursor?.takeIf { cursor.count > 0 }?.apply {
                    while (moveToNext()) {
                        val columnFlag = getInt(getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_FLAGS))
                        println("columnFlag---$columnFlag")//位运算保存状态
                        if (columnFlag and DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL == DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL) {
                            println("支持缩略图")
                        }
                        if (columnFlag and DocumentsContract.Document.FLAG_SUPPORTS_DELETE == DocumentsContract.Document.FLAG_SUPPORTS_DELETE) {
                            println("支持删除")
                        }
                    }
                }
            }
        }
    }
    lateinit var binding: ActivitySharedDocumentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySharedDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //4.1 确定Document支持的操作
        binding.button1.setOnClickListener {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//                val treeUri = storageManager.storageVolumes[0].createOpenDocumentTreeIntent()
//                    .getParcelableExtra<Uri>(DocumentsContract.EXTRA_INITIAL_URI)
                val image = getImageDocumentUri()
                println("treeUri-----$image")
                type = "image/*"
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, image)
                activityResult.launch(this)
            }
        }
    }

    //4.3 打开位图 Bitmap
    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor: ParcelFileDescriptor? = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        return image
    }

}