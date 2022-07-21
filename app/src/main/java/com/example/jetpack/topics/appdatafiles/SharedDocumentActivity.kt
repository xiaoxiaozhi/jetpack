package com.example.jetpack.topics.appdatafiles

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivitySharedDocumentBinding


/**
 * 这一章的几个小节都是通过存储访问框架SAF实现
 * 1. 创建新文件
 *    ACTION_CREATE_DOCUMENT 无法覆盖现有文件。如果您的应用尝试保存同名文件，系统会在文件名的末尾附加一个数字并将其包含在一对括号中。
 * 2. 打开文件
 * 3. 授予对目录内容的访问权限
 * 4. 在所选位置执行操作
 * 5.
 * [MIME 类型列表](https://www.runoob.com/http/mime-types.html)
 */
class SharedDocumentActivity : AppCompatActivity() {
    val uriResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

    }
    lateinit var binding: ActivitySharedDocumentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySharedDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        val storageManager = getSystemService(STORAGE_SERVICE) as StorageManager
//        val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
//        var uri: Uri? = intent.getParcelableExtra("android.provider.extra.INITIAL_URI")
//        println("oldUri----$uri")
//        var scheme: String = uri.toString()
//        println("INITIAL_URI scheme: $scheme")
//        scheme = scheme.replace("/root/", "/document/")
//        scheme += "%3Ajetpack"
//        uri = Uri.parse(scheme)
//        println("newUri----$uri")
//        binding.button1.setOnClickListener {
//            uriResult.launch(Intent(intent).apply {
////                addCategory(Intent.CATEGORY_OPENABLE)
////                type = "application/pdf"
////                putExtra(Intent.EXTRA_TITLE, "invoice.pdf")
//                putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)//TODO 从指定地方打开文件选择器，k30Pro测试不管用
//            })
//        }


        //String startDir = "Android";
        //String startDir = "Download"; // Not choosable on an Android 11 device
        //String startDir = "DCIM";
        //String startDir = "DCIM/Camera";  // replace "/", "%2F"
        //String startDir = "Android";
        //String startDir = "Download"; // Not choosable on an Android 11 device
        //String startDir = "DCIM";
        //String startDir = "DCIM/Camera";  // replace "/", "%2F"
    }

}