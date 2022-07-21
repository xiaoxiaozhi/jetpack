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
import java.io.File
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * 模拟传统File系统的一套接口，底层由DocumentProvider和File支持，能在开启分区存储的情况下在不属于自己的空间读写文件，但是使用它会有一定的开销，如果在意性能请使用DocumentsContract
 * Document和File有以下几个区别
 * - Document将其显示名称和 MIME 类型表示为单独的字段，而不是依赖于文件扩展名
 * - Document没有强烈的路径概念,单个Document可能是多个目录的子目录，所以它不知道父目录是谁，您可以轻松地遍历父文档到子文档树，但是不能遍历子文档到父文档树
 * - 每个文档在该提供程序中都有一个唯一的标识符
 * 1. 创建Document
 *    fromTreeUri(context: Context, treeUri: Uri ): DocumentFile?  其中treeURI是调用 Intent(Intent.ACTION_OPEN_DOCUMENT_TREE) 选中文件夹后返回的uri
 *    fromSingleUri(context: Context,singleUri: Uri ): DocumentFile? 其中singleUri是调用 Intent#ACTION_OPEN_DOCUMENT or Intent#ACTION_CREATE_DOCUMENT .选中文件夹后返回的uri
 *    fromFile(file: File): DocumentFile 创建一个 DocumentFile，以给定的File作为文档树的跟。注意这种方式创建的Document不会有读写权限，同时getUri返回的是file://而不是content://
 *TODO globalUri 和 globalDocument 要放在ViewModel里面
 *TODO putExtra(DocumentsContract.EXTRA_INITIAL_URI,uri) 溢栈有网友说 uri 由FileProvider.getUriForFile(context: Context, authority: String, file: File) 存疑
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
//                putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri) //这个URI怎么获得
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
}