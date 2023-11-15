package com.example.jetpack.topics.network.myokhttp.diyHttp

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.WorkerThread
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections.synchronizedList
import java.util.concurrent.Executors


// [okhttp下载和断点续传,后者没有验证](https://blog.csdn.net/Taozi825232603/article/details/117003324)
// https://www.jianshu.com/p/198a62f5c90c  这个验证过了
class OkHttpUtil private constructor(val context: Context) {
    companion object : SingletonHolder<OkHttpUtil, Context>(::OkHttpUtil)

    val TAG = "OkHttpUtil"
    private val awaitList = synchronizedList(mutableListOf<OkhttpItem>())
    private val currentList = mutableListOf<OkhttpItem>()
    private val downloadedList = mutableListOf<OkhttpItem>()
    private var progress: Int = 0
    private var total: Int = 0
    private var temp: Int = 0
    private lateinit var file: File
    private lateinit var current: (List<OkhttpItem>) -> Unit
    private lateinit var downloaded: (List<OkhttpItem>) -> Unit
    private lateinit var listen: (file: File, progress: Int, total: Int) -> Unit
    private val executorService = Executors.newSingleThreadExecutor()
    private lateinit var empty: () -> Unit
    private var isPause: Boolean = false


    fun getCurrentExecuteInfo(c: (current: List<OkhttpItem>) -> Unit,
        d: (downloaded: List<OkhttpItem>) -> Unit,
        l: (file: File, progress: Int, total: Int) -> Unit,
        e: () -> Unit) {
        this.current = c
        this.downloaded = d
        this.listen = l
        this.empty = e
        if (::file.isInitialized) listen(file, temp, total)
        downloaded(downloadedList)
        current(currentList)
        if (currentList.isEmpty() && awaitList.isEmpty()) empty()
    }

    fun addExecuteList(list: List<OkhttpItem>) {
        Log.i(TAG, "接收到的列表---${list.toString()}")
        currentList.addAll(list)
    }

    fun pause() {
        isPause = true
    }

    fun resume() {
        isPause = false
        executeList()
    }

    @WorkerThread
    fun executeList() {
        executorService.execute {
            currentList.iterator().also {
                while (it.hasNext()) {
                    it.next().apply {
                        file = this.outFile
//                        if (findFile(file.name, type)) return@apply
                        Log.i(TAG, "本地不存在开始下载---${file.name}")
                        if (downloadFile1(this.url, file)) {
                            Log.i(TAG, "下载成功开始插入相应文件 type=${this.type}")
                            when (this.type) {
                                OkhttpItem.MOVIE -> insertVideo(this)
                                OkhttpItem.PHOTO -> insertPic(this)
                                OkhttpItem.EMR -> insertEmr(this)
                            }
                            downloadedList.add(0, this)
                        } else {
                            Log.i(TAG, "下载失败")
                        }

                    }
                    if (isPause) {
                        break
                    } else {
                        it.remove()
                        if (::current.isInitialized) current(currentList.toList())
                        if (::downloaded.isInitialized) downloaded(downloadedList.toList())
                    }
                }
            }
            if (isPause) {

            } else {
                awaitList.takeIf { it.isNotEmpty() }?.apply {
                    currentList.addAll(this)
                    clear()
                    executeList()
                } ?: if (::empty.isInitialized) empty() else Unit
            }
        }
    }

    private fun findFile(name: String, type: String) = File(
        when (type) {
            OkhttpItem.MOVIE -> context.filesDir.toString() + File.separator + "Movies"
            OkhttpItem.PHOTO -> context.filesDir.toString() + File.separator + "Images"
            else -> context.filesDir.toString() + File.separator + "Emrs"
        }, name
    ).exists()


    //正常下载代码
    private fun downloadFile(url: String, outFile: File): Boolean {
        val urlConnection = getConnection(url)
        Log.i(TAG, "responseCode---${urlConnection.responseCode} ${urlConnection.responseMessage}")
        if (!(urlConnection.responseCode == 200 || urlConnection.responseCode == 206)) {
            return false
        }
        urlConnection.inputStream.use { input ->
            total = urlConnection.contentLength
            Log.i(TAG, "打开输入流下载---$total")
            FileOutputStream(outFile).use { out ->
                Log.i(TAG, "打开输出流")
                val buffer = ByteArray(1024 * 1024)
                var len: Int
                var temp = 0
                while (input.read(buffer).also { len = it } != -1) {
//                    Log.i(TAG, "读到字节数---$len")
                    out.write(buffer, 0, len)
                    temp += len
                    if (::listen.isInitialized) listen(file, temp, total)
                }
            }
        }
        Log.i(TAG, "下载完成")
        total = 0
        progress = 0
        urlConnection.disconnect()
        return true
    }

    //断点续传代码
    private fun downloadFile1(url: String, outFile: File): Boolean {
        Log.i(TAG, "文件已经存在的大小---${outFile.length()}")
        val randomFile = RandomAccessFile(file, "rw")
        randomFile.seek(outFile.length()) //跳过已经下载的字节
        val urlConnection = getConnection1(url, outFile.length())

        Log.i(
            TAG, "responseCode---${urlConnection.responseCode} " + "${urlConnection.responseMessage} " + "${
                urlConnection.getHeaderField("Content-Range")
            }"
        )
        if (!(urlConnection.responseCode == 200 || urlConnection.responseCode == 206)) {
            return false
        }
        urlConnection.inputStream.use { input ->
//            total = urlConnection.contentLength
            total = urlConnection.getHeaderField("Content-Range").split("/").toList().last().toInt()
            Log.i(TAG, "打开输入流下载---$total")
            val buffer = ByteArray(1024 * 1024)
            var len: Int
            temp = outFile.length().toInt()
            while (input.read(buffer).also { len = it } != -1) {
//                    Log.i(TAG, "读到字节数---$len")
                if (isPause) {
                    break
                }
                randomFile.write(buffer, 0, len)
                temp += len
                if (::listen.isInitialized) listen(file, temp, total)
            }
        }
        return if (isPause) {
            Log.i(TAG, "暂停后temp大小---$temp")
            false
        } else {
            Log.i(TAG, "下载完成")
            total = 0
            progress = 0
            urlConnection.disconnect()
            true
        }

    }

    private fun getConnection(httpUrl: String): HttpURLConnection {
        val url = URL(httpUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-Type", "application/octet-stream")
        connection.setRequestProperty("Connection", "Keep-Alive")
        connection.setRequestProperty("Range", "bytes=0-");
        connection.connect()
        return connection
    }

    /**
     * Range: bytes=0-499      表示第 0-499 字节范围的内容
     * Range: bytes=500-999    表示第 500-999 字节范围的内容
     * Range: bytes=-500       表示最后 500 字节的内容
     * Range: bytes=500-       表示从第 500 字节开始到文件结束部分的内容
     * Range: bytes=0-0,-1     表示第一个和最后一个字节  事实证明正常下载bytes=0- 就可以，为什么还要有这种写法
     * Range: bytes=500-600,601-999 同时指定几个范围
     */
    private fun getConnection1(httpUrl: String, size: Long): HttpURLConnection {
        val url = URL(httpUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-Type", "application/octet-stream")
        connection.setRequestProperty("Connection", "Keep-Alive")
        connection.setRequestProperty("Range", "bytes=${size}-");//断点续传关键在于这句从断点的位置往后到结尾
        connection.connect()
        return connection
    }

    //TODO hilt 注入的形式放在viewModels，做到viewModels与context的分离, 插入方法要放在全局中
    private fun insertVideo(dataItem: OkhttpItem): Uri {
        val fileValue = ContentValues().apply {
//            Log.i("名字", "name----${dataItem.outFile.name}")
            put(MediaStore.Video.VideoColumns.DISPLAY_NAME, dataItem.outFile.name)
            put(
                MediaStore.Video.VideoColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + File.separator + "yadea"
            )
            put(MediaStore.Video.VideoColumns.DATE_ADDED, dataItem.date)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Video.VideoColumns.IS_PENDING, 1
                )
            }
        }
        Log.i(TAG, "MediaStore.Files = ${getVideoContentUri()}")
        val fileUri = context.contentResolver?.insert(getVideoContentUri(), fileValue)!!.also {
            Log.i(TAG, "fileUri-----${it.toString()}")
            context.contentResolver!!.openFileDescriptor(it, "w").use { fileDescriptor ->
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
                fileValue.put(MediaStore.Video.VideoColumns.IS_PENDING, 0)
                context.contentResolver!!.update(it, fileValue, null, null)
                    .apply { Log.i(TAG, "IS_PENDING update ----$this") }
            }
        }
        Log.i(TAG, "视频放到共享文件夹 uri---${fileUri}")
        return fileUri
    }

    private fun insertPic(dataItem: OkhttpItem): Uri {
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
        val fileUri = context.contentResolver?.insert(getImageContentUri(), fileValue)!!.also {
            Log.i(TAG, "fileUri-----${it.toString()}")
            context.contentResolver!!.openFileDescriptor(it, "w").use { fileDescriptor ->
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
                context.contentResolver!!.update(it, fileValue, null, null)
                    .apply { Log.i(TAG, "IS_PENDING update ----$this") }
            }
        }
        Log.i(TAG, "图片放到共享文件夹 uri---${fileUri}")
        return fileUri
    }

    private fun insertEmr(dataItem: OkhttpItem): Uri {
        Log.i(TAG, "开始插入紧急文件1")
        val fileValue = ContentValues().apply {
            put(MediaStore.Video.VideoColumns.DISPLAY_NAME, dataItem.name)
            put(
                MediaStore.Video.VideoColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "yadea"
            )
            put(MediaStore.Video.VideoColumns.DATE_ADDED, dataItem.date)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Video.VideoColumns.IS_PENDING, 1
                )
            }
        }
        Log.i(TAG, "开始插入紧急文件2")
        Log.i(TAG, "MediaStore.Files = ${getVideoContentUri()}")
        val fileUri = context.contentResolver?.insert(getVideoContentUri(), fileValue)!!.also {
            Log.i(TAG, "fileUri-----${it.toString()}")
            context.contentResolver!!.openFileDescriptor(it, "w").use { fileDescriptor ->
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
                fileValue.put(MediaStore.Video.VideoColumns.IS_PENDING, 0)
                context.contentResolver!!.update(it, fileValue, null, null)
                    .apply { Log.i(TAG, "IS_PENDING update ----$this") }
            }
        }
        Log.i(TAG, "紧急视频放到 uri---${fileUri}")
        return fileUri
    }

    private fun getVideoContentUri() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {//android 10 api29
        MediaStore.Video.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )
    } else {
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }

    private fun getImageContentUri() = when {
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) -> MediaStore.Images.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL
        )

        else -> MediaStore.Images.Media.getContentUri("external")
    }.apply { Log.i(TAG, "getImageContentUri----$this") }
}