package com.example.jetpack.topics.appdatafiles

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import com.example.jetpack.R

/**
 * 1. 创建新文件
 * 2. 打开文件
 * 3. 授予对目录内容的访问权限
 * 4. 在所选位置执行操作
 * 5.
 * [MIME 类型列表](https://www.runoob.com/http/mime-types.html)
 */
class SharedDocumentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_document)
        // Request code for creating a PDF document.
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "invoice.pdf")
            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
//            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        })
//      DocumentFile.
    }
}