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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections.synchronizedList
import java.util.concurrent.Executors

/**
 * 本例采用context.contentResolver?.insert 把多媒体文件插入相册，是官方文档上采用的形式，
 * 实践中发现android9和android7出现了问题，无法采用上述形式插入相册，以下是总结日志
 * android 7和android9 使用 context.contentResolver?.insert 返回null无法插入
 * vivo android 9 使用以下形式可以插入，三星 android7使用以下形式不可以
 * sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File("your path"))););
 *
 */

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
    private lateinit var actionPause: () -> Unit


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

    fun pauseCallback(action: () -> Unit) {
        isPause = true
        actionPause = action
    }

    fun resume() {
        isPause = false
        executeList()
    }

    fun isPause() = isPause


    fun haveItem(name: String): Boolean {
        return !currentList.none { it.name == name }.apply { Log.i(TAG, "有文件吗？---$this") }
    }

    suspend fun haveAndDelete(name: String) {
        if (currentList.isNotEmpty() && file.name == name) {
            withContext(Dispatchers.Default) {
                pauseCallback {
                    Log.i(TAG, "删除temp文件---${File(file.absolutePath + ".temp").takeIf { it.exists() }?.delete()}")
//                    MmkvHelper.getInstance().putObject(OkhttpItem.CURRENT, null)
                    currentList.filter { it.name == name }.takeIf { it.isNotEmpty() }?.first()?.also {
                        Log.i(TAG, "移除正下载---${it.name}")
                        currentList.remove(it)
                        Log.i(TAG, "删除后剩余个数---${currentList.size}")
                        resume()
                    }
                }
                delay(300)
            }
        } else {
            currentList.filter { it.name == name }.takeIf { it.isNotEmpty() }?.first()?.also {
                Log.i(TAG, "移除队列文件---${it.name}")
                currentList.remove(it)
                Log.i(TAG, "删除后剩余个数---${currentList.size}")
                resume()
            }
        }
    }

    @WorkerThread
    fun executeList() {
        executorService.execute {
            currentList.iterator().also {
                while (it.hasNext()) {
                    it.next().apply {
//                        MmkvHelper.getInstance().putObject(OkhttpItem.CURRENT, this);
                        file = this.outFile
//                        if (findFile(file.name, type)) return@apply
                        Log.i(TAG, "本地不存在开始下载---${file.name}")
                        val d = File(file.absolutePath + ".temp")
//                        if (downloadFile1(this.url, file)) {
                        if (downloadFile1(this.url, d)) {
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
//                        MmkvHelper.getInstance().putObject(OkhttpItem.CURRENT, null);
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
                } ?: run {
                    if (::empty.isInitialized) empty()
                }
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

    private fun downloadFile1(url: String, outFile: File): Boolean {
        Log.i(TAG, "文件已经存在的大小---${outFile.length()}")
        val randomFile = RandomAccessFile(outFile, "rw")
        randomFile.seek(outFile.length()) //跳过已经下载的字节
        val urlConnection = getConnection1(url, outFile.length())

        Log.i(
            TAG,
            "responseCode---${urlConnection.responseCode} " + "${urlConnection.responseMessage} " + "${
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
            if (::actionPause.isInitialized) actionPause()
            false
        } else {
            Log.i(TAG, "下载完成")
            total = 0
            progress = 0
            urlConnection.disconnect()
            Log.i(TAG, "改名---${outFile.absolutePath.replace(".temp", "")}...")
            outFile.renameTo(File(outFile.absolutePath.replace(".temp", "")))
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

    private fun getConnection1(httpUrl: String, size: Long): HttpURLConnection {
        val url = URL(httpUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-Type", "application/octet-stream")
        connection.setRequestProperty("Connection", "Keep-Alive")
        connection.setRequestProperty("Range", "bytes=${size}-");
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

    private fun insertPic(dataItem: OkhttpItem): Uri? {
        val fileValue = ContentValues().apply {
//            put(MediaStore.Images.ImageColumns.DATA, mPicPath) //指定绝对路径
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, dataItem.outFile.name)
            put(
                //指定相对路径，该例是系统文件夹Pictures的路径，在三星手机上 只指定了相对路径发现无法插入到相册，指定绝对可以插入相册
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
        val fileUri = context.contentResolver?.insert(getImageContentUri(), fileValue)?.also {
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

    private fun insertEmr(dataItem: OkhttpItem): Uri? {
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
        val fileUri = context.contentResolver?.insert(getVideoContentUri(), fileValue)?.also {
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