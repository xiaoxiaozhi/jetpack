package com.example.jetpack.topics.network

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.lifecycleScope
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityOkhttpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import java.io.IOException

/**
 * [Okhttp官网](https://square.github.io/okhttp/)
 * [在build.gradle中添加依赖](https://square.github.io/okhttp/#releases)
 * 1.优点
 *   1.1 所有请求共享一个套接字
 *   1.2 连接池减少请求延迟 (这是什么意思？)
 *   1.3 使用GZIP减小下载大小
 *   1.4 使用缓存避免频繁请求网络
 * 2.功能
 *   2.1 请求 每个请求都要有一个URL、 一个方法(get或者post等)、一个请求头。连接失败的情况下，okhttp会持续请求
 *   2.2 响应 返回头、响应码(404找不到或者200成功)、返回body。响应一个请求，如果使用了透明压缩，OkHttp 将删除相应的响应头 Content-Encoding 和 Content-Llength，因为它们不适用于解压缩后的响应主体。
 *
 *   2.3 调用 同步、异步
 *   2.4 dispatch
 * --------------------------------使用-------------------------------
 * 3.get方法
 *   如果body小于1M，可以使用response.body!!.string()获取 否则 请使用流的方式获取。response 和 body的流用完都要关闭
 *   3.1 execute 同步
 *   3.2 enqueue 异步方式获取
 * 4.访问请求头
 *   http请求头有的可以存在多对(例如Accept)，有的只能一对(例如User-Agent)。 header(name, value)设置那些仅有一对的值，重复设置会覆盖之前存在的值
 *   addHeader(name,value)设置那些可以存在多对的值，重复设置会同时存在多对，不会覆盖
 * 5.post方法
 *   5.1 post 字符串 不要发送大于1M的字符串
 *   5.2 post 流 继承RequestBody 覆写contentType() 和 writeTo(sink: BufferedSink)。前者确定流内容的MIME属性，后者生成一个输出流传递给服务器
 *
 *attention: Android P开始 必须使用https，使用http连接会报java.net.UnknownServiceException: CLEARTEXT communication to not permitted by network security
 *           [解决使用http链接报异常](https://blog.csdn.net/weixin_28871645/article/details/114612921)
 *TODO kotlin IO待总结
 */
class OkhttpActivity : AppCompatActivity() {
    lateinit var binding: ActivityOkhttpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView<ActivityOkhttpBinding>(this, R.layout.activity_okhttp)
        //3.1 get方法---同步
        println("3.1 get方法---同步---------------------")
        synchronousGet()
        //3.2 get方法---异步
        println("3.2 get方法---异步---------------------")
        asynchronousGet()
        //4.访问请求头
        println("4.访问请求头---------------------")
        accessingHeaders()
        //5.1 postingString
        println("5.1 postingString---------------------")
        postingString()
        //5.2 post stream
        println("5.2 postingStream---------------------")
        postStream()


    }

    //3.1 get 同步
    fun synchronousGet() {
        val client = OkHttpClient()
        val request = Request.Builder().url("https://publicobject.com/helloworld.txt").build()
        newSingleThreadContext("synchronousGet").use {
            CoroutineScope(it).launch {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
//                println("body size----${response.body?.bytes()?.size}") //不要这样 bytes() 会关闭数据流，导致再从body获取流报异常
                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }
                    response.headers.filter {
                        it.first == "Content-Length"
                    }.forEach {
                        println("body size = ${it.second}")
                        response.body?.charStream().use { reader ->
                            println("结果----${reader?.readText()}")
                        }//使用流的方式获取
//                    println("结果---${response.body!!.string()}")//使用string方式获取
                    }
                }
            }
        }
    }

    //3.2 get 异步
    fun asynchronousGet() {
        lifecycleScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder().url("https://publicobject.com/helloworld.txt").build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println(" e.printStackTrace()-----------------${e.printStackTrace()}")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        println("response-----------------------")
                        for ((name, value) in response.headers) {
                            println("$name: $value")
                        }
                        println(response.body!!.string())
                    }
                }
            })
        }
    }

    //4.访问请求头
    fun accessingHeaders() {
        val request = Request.Builder().url("https://api.github.com/repos/square/okhttp/issues")
            .header("User-Agent", "OkHttp Headers.java").addHeader("Accept", "application/json; q=0.5")
            .addHeader("Accept", "application/vnd.github.v3+json").build()
        println("request---headers")
        for ((name, value) in request.headers) {
            println("$name: $value")
        }
        val client = OkHttpClient()
        lifecycleScope.launch(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                println("response---headers")
                for ((name, value) in response.headers) {
                    println("$name: $value")
                }
                println("Server: ${response.header("Server")}")
                println("Date: ${response.header("Date")}")
                println("Vary: ${response.headers("Vary")}")
                response.body?.charStream().use { reader ->
                    println("accessingHeaders----${reader?.readText()}")
                }
            }
        }
    }

    //5.1 post 字符串
    fun postingString() {
        val postBody = """
        |Releases
        |--------
        |
        | * _1.0_ May 6, 2013
        | * _1.1_ June 15, 2013
        | * _1.2_ August 11, 2013
        |""".trimMargin()
        val client = OkHttpClient()
        val request = Request.Builder().url("https://api.github.com/markdown/raw")
            .post(postBody.toRequestBody(MEDIA_TYPE_MARKDOWN)).build()

        lifecycleScope.launch(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                println("postingString-----" + response.body!!.string())
            }
        }
    }

    //5.2 post 流
    fun postStream() {
        val client = OkHttpClient()

        val request = Request.Builder().url("https://api.github.com/markdown/raw").post(requestBody).build()
        lifecycleScope.launch(Dispatchers.IO){
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                println("postStream-----" + response.body!!.string())
            }
        }
    }

    val requestBody = object : RequestBody() {
        override fun contentType() = MEDIA_TYPE_MARKDOWN

        override fun writeTo(sink: BufferedSink) {
            sink.writeUtf8("Numbers\n")
            sink.writeUtf8("-------\n")
            for (i in 2..997) {
                sink.writeUtf8(String.format(" * $i = ${factor(i)}\n"))
            }
        }

        private fun factor(n: Int): String {
            for (i in 2 until n) {
                val x = n / i
                if (x * i == n) return "${factor(x)} × $i"
            }
            return n.toString()
        }
    }

    companion object {
        val MEDIA_TYPE_MARKDOWN = "text/x-markdown; charset=utf-8".toMediaType()
    }
}