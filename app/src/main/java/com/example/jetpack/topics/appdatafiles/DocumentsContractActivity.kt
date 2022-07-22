package com.example.jetpack.topics.appdatafiles

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import com.example.jetpack.databinding.ActivityDocumentContractBinding
import com.example.jetpack.getImageDocumentUri
import com.example.jetpack.getRootDocumentUri
import com.example.jetpack.storageManager

/**
 * DocumentsProvider是Document的内容提供者提供了DocumentContract的基础实现 要理解DocumentContract先看 [DocumentsProvider](https://developer.android.google.cn/reference/android/provider/DocumentsProvider)
 * DocumentsContract.Document的主要内容是DocumentsProvider的列  在contentResolver.query 查询中 提供project选项
 * TODO 位运算保存状态 [缺少移除状态的方法](https://blog.csdn.net/qq_40616887/article/details/114917833)
 */
class DocumentsContractActivity : AppCompatActivity() {
    private val activityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        println("flag-----${it.data?.flags}")
        it.data?.data?.apply {
            println("返回Uri---$this")
            val document = DocumentFile.fromSingleUri(this@DocumentsContractActivity, this)
//            document.do
            println("documentUri----${document?.uri}")
            contentResolver.query(this, arrayOf(DocumentsContract.Document.COLUMN_FLAGS), null, null).use { cursor ->
                cursor?.takeIf { cursor.count > 0 }?.apply {
                    while (moveToNext()) {
                        val columnFlag = getInt(getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_FLAGS))
                        println("columnFlag---$columnFlag")//k30pro 16711 这个值在注释中找不到
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
    lateinit var binding: ActivityDocumentContractBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentContractBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
}