package com.example.jetpack.test

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityTest3Binding
import com.example.jetpack.topics.network.myokhttp.diyHttp.OkhttpItem
import java.io.File
import java.io.FileInputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.Date


class TestActivity : AppCompatActivity() {
    lateinit var binding: ActivityTest3Binding

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_test3)
//        inputPicture()
        getImageContentUri()
        sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(
                    File(
                        Environment.getExternalStorageDirectory(),
//                        "DCIM" + File.separator + "Camera" + File.separator + "asdf.png"
                        "DCIM" + File.separator + "Camera" + File.separator + "zxcv.jpg"
                    )
                )
            )
        )
//        insertPic(
//            OkhttpItem(
//                "", "", "", Date().time, File(
//                    Environment.getExternalStorageDirectory(),
////                        "DCIM" + File.separator + "Camera" + File.separator + "asdf.png"
//                    "DCIM" + File.separator + "Camera" + File.separator + "zxcv.jpg"
//                )
//            )
//        )
        Log.i(
            TAG, """
           品牌---${Build.BRAND}
           设备版本号---${Build.DISPLAY}
           手机型号---${Build.MODEL}
           产品名称---${Build.PRODUCT}
        """.trimIndent()
        )
        File(
            Environment.getExternalStorageDirectory(),
//                        "DCIM" + File.separator + "Camera" + File.separator + "asdf.png"
            "DCIM" + File.separator + "Camera" + File.separator + "zxcv.jpg"
        ).renameTo(
            File(
                Environment.getExternalStorageDirectory(),
//                        "DCIM" + File.separator + "Camera" + File.separator + "asdf.png"
                "Pictures" + File.separator + "zxcv.jpg"
            )
        )
    }

    private fun inputPicture() {
        //Intent.ACTION_PICK 从数据中选择一个项目 (item)，将被选中的项目返回。
        //MediaStore.Images.Media.EXTERNAL_CONTENT_URI 获取外部的URI
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        //参数一:对应的数据的URI 参数二:使用该函数表示要查找文件的MIME类型
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
            val selectedImage = data.data
            Log.i("TestActivity", "uri---$selectedImage")
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            Log.e("TAG", "onActivityResult: $picturePath")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getImageContentUri() = when {
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) -> MediaStore.Images.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL          //content://media/external/file
        )

        else -> MediaStore.Images.Media.getContentUri("external")
    }.apply { Log.i(TAG, "getImageContentUri----$this") }

    private fun insertPic(dataItem: OkhttpItem): Uri? {
        val fileValue = ContentValues().apply {
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, dataItem.outFile.name)
            put(
                MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "yadea"
            )
            put(MediaStore.Images.Media.DATE_ADDED, dataItem.date)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.ImageColumns.IS_PENDING, 1
                )
            }
        }
        Log.i(TAG, "图片库uri = ${getImageContentUri()}")
        val fileUri = contentResolver?.insert(getImageContentUri(), fileValue)?.also {
            Log.i(TAG, "fileUri-----${it.toString()}")
            contentResolver!!.openFileDescriptor(it, "w").use { fileDescriptor ->
                ParcelFileDescriptor.AutoCloseOutputStream(fileDescriptor).use { out ->
                    FileInputStream(dataItem.outFile).use { fis ->
                        val buffer = ByteArray(1024)
                        var len: Int
                        while (fis.read(buffer).also { len = it } != -1) {
                            out.write(buffer, 0, len)
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                fileValue.clear()
                fileValue.put(MediaStore.Images.ImageColumns.IS_PENDING, 0)
                contentResolver!!.update(it, fileValue, null, null).apply { Log.i(TAG, "IS_PENDING update ----$this") }
            }
        }
        Log.i(TAG, "图片放到共享文件夹 uri---${fileUri}")
        return fileUri
    }

    companion object {
        const val TAG = "TestActivity"
    }
}
