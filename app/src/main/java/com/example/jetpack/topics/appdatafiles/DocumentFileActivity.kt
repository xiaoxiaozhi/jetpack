package com.example.jetpack.topics.appdatafiles

import android.content.Intent
import android.content.Intent.CATEGORY_OPENABLE
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.os.EnvironmentCompat
import androidx.documentfile.provider.DocumentFile
import com.example.jetpack.databinding.ActivityDocumentFileBinding
import com.example.jetpack.storageManager
import java.io.File
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * 模拟传统File系统的一套接口，底层由DocumentProvider和File支持，能在开启分区存储的情况下在不属于自己的空间读写文件，但是使用它会有一定的开销，如果在意性能请使用DocumentsContract
 * 主要用途DocumentFile 在android 10 以后 仍然可以在不属于自己的外部存储空间创建 文件和文件夹 content:// URI 允许您使用临时访问权限授予读写访问权限
 * Document和File有以下几个区别
 * - Document将其显示名称和 MIME 类型表示为单独的字段，而不是依赖于文件扩展名
 * - Document没有强烈的路径概念,单个Document可能是多个目录的子目录，所以它不知道父目录是谁，您可以轻松地遍历父文档到子文档树，但是不能遍历子文档到父文档树
 * - 每个文档在该提供程序中都有一个唯一的标识符
 * 1. 创建Document
 *    fromTreeUri(context: Context, treeUri: Uri ): DocumentFile?  其中treeURI是调用 Intent(Intent.ACTION_OPEN_DOCUMENT_TREE) 选中文件夹后返回的uri
 *    fromSingleUri(context: Context,singleUri: Uri ): DocumentFile? 其中singleUri是调用 Intent#ACTION_OPEN_DOCUMENT or Intent#ACTION_CREATE_DOCUMENT .选中文件夹后返回的uri
 *    fromFile(file: File): DocumentFile 创建一个 DocumentFile，以给定的File作为文档树的跟。注意这种方式创建的Document不会有读写权限，同时getUri返回的是file://而不是content://
 *note: 根目录 uri  content://com.android.externalstorage.documents/root/primary
 *note：照片目录 uri content://com.android.externalstorage.documents/document/primary%3ADCIM%2FCamera
 *note: Uri格式 协议名://<authority>/<path>/<id> 可以看出 根文档 和 照片文档 的路径不同。从uri上看不到上下级关系，这是不同于File的一点
 * TODO globalUri 和 globalDocument 要放在ViewModel里面
 *TODO FileProvider待总结
 */
class DocumentFileActivity : AppCompatActivity() {
    lateinit var binding: ActivityDocumentFileBinding
    lateinit var globalUri: Uri
    lateinit var globalDocument: DocumentFile


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentFileBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            println("uri---${it.data?.data}")
//        }.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))//k30pro 打开后 会定位到DICM/Camera文件夹
        binding.button1.setOnClickListener {
            println("s----1")
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {//ACTION_OPEN_DOCUMENT_TREE 选择目录后系统会记录下次再次打开仍会导航到该目录。
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, getTreeUri())
                treeResult.launch(this)
            }
        }
        binding.button2.setOnClickListener {
            globalDocument.createDirectory("321")
        }
        binding.button3.setOnClickListener {
            globalDocument.createFile("text/plain", "ss")?.apply {
                contentResolver.openFileDescriptor(uri, "w").use {
                    OutputStreamWriter(
                        ParcelFileDescriptor.AutoCloseOutputStream(it), StandardCharsets.UTF_8
                    ).use { write ->
                        write.write("1234rr恭喜发财")
                    }
                }
            }
        }
        binding.button4.setOnClickListener {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {//ACTION_OPEN_DOCUMENT_TREE 选择目录后系统会记录下次再次打开仍会导航到该目录。
                type = "image/*"
//                resolveActivity(packageManager)?.let { activityResult.launch(this) }
//                    ?: println(" No Activity found to handle Intent { act=android.intent.action.OPEN_DOCUMENT }") //为什么resolveActivity 返回空
                documentResult.launch(this)
            }
        }
        binding.button5.setOnClickListener {
//            val sm = getSystemService<StorageManager>()
//            println("path----${sm?.storageVolumes?.get(0)?.directory}")
//            val uri = FileProvider.getUriForFile(
//                this,
//                "com.android.externalstorage.documents",
//                sm?.storageVolumes?.get(0)?.directory!!
//            )
//            println("uri-----$uri")
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {//ACTION_OPEN_DOCUMENT_TREE 选择目录后系统会记录下次再次打开仍会导航到该目录。
                type = "image/*"
                putExtra(Intent.EXTRA_TITLE, "文件ming.txt")//文件名称
                addCategory(CATEGORY_OPENABLE) //如果 需要对返回的 数据进行读写操作
                putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI, getUri("alipay")
//                    getUri("alipay%2FCamera") //Document不存在则跳转到上一次选择的Document。如果第一次运行则跳转到 根Document
                ) //[URI怎么获得](https://stackoverflow.com/a/67515474/1179614)
//                resolveActivity(packageManager)?.let { activityResult.launch(this) }
//                    ?: println(" No Activity found to handle Intent { act=android.intent.action.OPEN_DOCUMENT }") //为什么resolveActivity 返回空
                documentResult.launch(this)
            }
        }
    }

    private val treeResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            println("documentTree uri----${activityResult.data?.data}")
            //如果您确实需要对整个文档子树进行完全访问那么首先启动ACTION_OPEN_DOCUMEN_TREE，让用户选择一个目录。
            // 然后将生成的意图 getData ()传递到 fromTreeUri (Context，Uri)以开始处理用户选择的树。
            activityResult.data?.data?.let {
                globalUri = it
                globalDocument = DocumentFile.fromTreeUri(this, it)?.apply {
                    listFiles().forEach { item ->
                        val name = item.name
                        val type = item.type
                        val uri = item.uri
                        println("名字---$name 类型----$type uri----$uri 是目录吗？----${item.isDirectory}")
                    }
                }!!
            }
        }
    private val documentResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            println("document uri----${activityResult.data?.data}")
            activityResult.data?.data?.let {
                DocumentFile.fromSingleUri(this, it)?.let { document ->
                    val name = document.name
                    val type = document.type
                    val uri = document.uri
                    println("名字---$name 类型----$type uri----$uri 是文件吗？----${document.isFile}")
                }
            }
        }

    private fun getUri(path: String?): Uri {
//        startDir = "DCIM%2FCamera";
        val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
        var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
        uri = if (path == null) {
            Uri.parse(uri.toString().replace("/root/", "/document/"))
        } else {
            Uri.parse(uri.toString().replace("/root/", "/document/") + "%3A" + path)
        }
        println("getUri----$uri")
        return uri
    }

    /**
     * 返回根目录Uri
     */
    private fun getTreeUri(): Uri? {
        val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
        var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
        println("getUri----$uri")
        return uri
    }
}