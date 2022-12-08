package com.example.jetpack.topics.network.myokhttp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class OkhttpApiImpl @Inject constructor(@ApplicationContext val context: Context) : OkhttpApi {
    @Inject
    lateinit var client: OkHttpClient

    //3.1 get方法---同步
    override fun synchronousGet() {
        val request = Request.Builder().url("https://publicobject.com/helloworld.txt").build()
        newSingleThreadContext("synchronousGet").use {
            CoroutineScope(it).launch {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
//                println("body size----${response.body?.bytes()?.size}") //不要这样 bytes() 会关闭数据流，导致再从body获取流报异常
                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }
                    response.headers.filter { pair ->
                        pair.first == "Content-Length"
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

    //3.2 get方法---异步
    override fun asynchronousGet() {
        newSingleThreadContext("asynchronousGet").use {
            CoroutineScope(it).launch {
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
    }

    //4.访问请求头
    override fun accessingHeaders() {
        newSingleThreadContext("accessingHeaders").use {
            CoroutineScope(it).launch {
                val request = Request.Builder().url("https://api.github.com/repos/square/okhttp/issues")
                    .header("User-Agent", "OkHttp Headers.java").addHeader("Accept", "application/json; q=0.5")
                    .addHeader("Accept", "application/vnd.github.v3+json").build()
                println("request---headers----${request.headers}")
//                for ((name, value) in request.headers) {
//                    println("$name: $value")
//                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println("accessingHeaders----onFailure ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")
                            println("response---headers-----${response.headers}")
//                    for ((name, value) in response.headers) {
//                        println("$name: $value")
//                    }
                            println("Server: ${response.header("Server")}")
                            println("Date: ${response.header("Date")}")
                            println("Vary: ${response.headers("Vary")}")
                            response.body?.charStream().use { reader ->
                                println("accessingHeaders----${reader?.readText()}")
                            }
                        }
                    }

                })
            }
        }
    }

    //5.1 postingString
    override fun postingString() {
        newSingleThreadContext("postingString").use {
            CoroutineScope(it).launch {
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
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println("postingString-----IOException " + e.message)
                    }

                    override fun onResponse(call: Call, response: Response) = response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        println("postingString-----" + response.body!!.string())
                    }
                })

            }
        }
    }

    //5.2 post stream
    override fun postStream() {
        newSingleThreadContext("postStream").use {
            CoroutineScope(it).launch {
                val request = Request.Builder().url("https://api.github.com/markdown/raw").post(requestBody).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println("postStream-----IOException " + e.message)
                    }

                    override fun onResponse(call: Call, response: Response) = response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        println("postStream-----" + response.body!!.string())
                    }

                })

            }
        }
    }

    //5.3 post File
    override fun postFile() {
        newSingleThreadContext("postFile").use {
            CoroutineScope(it).launch {
                if (!File(context.filesDir, "README.md").exists()) {
                    val filename = "README.md"
                    val fileContents = "Hello world!"
                    context.openFileOutput(filename, AppCompatActivity.MODE_PRIVATE).use { stream ->
                        stream.write(fileContents.toByteArray())
                    }
                }
                val file = File(context.filesDir, "README.md")
                val request = Request.Builder().url("https://api.github.com/markdown/raw")
                    .post(file.asRequestBody(MEDIA_TYPE_MARKDOWN)).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println("postFile---IOException " + e.message)
                    }

                    override fun onResponse(call: Call, response: Response) = response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        println("postFile---" + response.body!!.string())
                    }

                })
            }
        }
    }///data/user/0/com.example.jetpack/files/README.md

    //5.4 表单上传
    override fun postFormParameters() {
        newSingleThreadContext("postFormParameters").use {
            CoroutineScope(it).launch {
                val formBody = FormBody.Builder().add("search", "Jurassic Park").build()
                val request = Request.Builder().url("https://en.wikipedia.org/w/index.php").post(formBody).build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    println("postFormParameters----" + response.body!!.string())
                }
            }
        }
    }

    //5.5 多种请求体上传 表单+文件
    override fun postMultipart() {
        newSingleThreadContext("postMultipart").use {
            CoroutineScope(it).launch {
                // Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
                val requestBody =
                    MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("title", "Square Logo")
                        .addFormDataPart(
                            "image", "xnr.png", File(context.filesDir, "xnr.png").asRequestBody(MEDIA_TYPE_PNG)
                        ).build()

                val request = Request.Builder().header("Authorization", "Client-ID $IMGUR_CLIENT_ID")
                    .url("https://api.imgur.com/3/image").post(requestBody).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println("postMultipart----onFailure ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) = response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        println("postMultipart----" + response.body!!.string())
                    }

                })

            }
        }
    }

    //6.用Moshi解析Json
    override fun parseJSON() {
        newSingleThreadContext("parseJSON").use {
            CoroutineScope(it).launch {
                val moshi = Moshi.Builder().build()
                val gistJsonAdapter = moshi.adapter(Gist::class.java)
                val request = Request.Builder().url("https://api.github.com/gists/c2a7c39532239ff261be").build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println("parseJSON-----onFailure  ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) = response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        val gist = gistJsonAdapter.fromJson(response.body!!.source())
                        for ((key, value) in gist!!.files!!) {
                            println("parseJSON-----key=$key  value=${value.content}")
                        }
                    }
                })
            }
        }
    }

    //7.使用响应缓存
    override fun responseCaching() {
        newSingleThreadContext("responseCaching").use {
            CoroutineScope(it).launch {
                val request =
                    Request.Builder().url("https://publicobject.com/helloworld.txt").addHeader("protocol", "http/2.0")
                        .build()
                val response1Body: String
                measureTimeMillis {
                    response1Body = client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $it")

                        println("Response 1 response:          $response")
                        println("Response 1 cache response:    ${response.cacheResponse}")
                        println("Response 1 network response:  ${response.networkResponse}")
                        return@use response.body!!.string()
                    }
                }.apply { println("spend time1 $this") }

                measureTimeMillis {
                    val response2Body = client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("Unexpected code $it")

                        println("Response 2 response:          $response")
                        println("Response 2 cache response:    ${response.cacheResponse}")
                        println("Response 2 network response:  ${response.networkResponse}")
                        return@use response.body!!.string()
                    }
                    println("Response 2 equals Response 1? " + (response1Body == response2Body))
                }.apply { println("spend time2 $this") }


            }
        }
    }

    //8.取消调用
    override fun cancelCall() {
        newSingleThreadContext("responseCaching").use {
            CoroutineScope(it).launch {
                val startTime = System.currentTimeMillis()
                println("cancelCall---startTime-----$startTime")
                val request =
                    Request.Builder().url("https://httpbin.org/delay/2") // This URL is served with a 2 second delay.
                        .build()
                val call = client.newCall(request)
                launch(Dispatchers.Default) {
                    delay(100)
                    println("cancelCall before-----${System.currentTimeMillis() - startTime}")
                    call.cancel()
                    println("cancelCall after-----${System.currentTimeMillis() - startTime}")
                }
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println("cancelCall onFailure------${e.printStackTrace()} at ${System.currentTimeMillis() - startTime}")
                    }

                    override fun onResponse(call: Call, response: Response) = response.use {
                        println("cancelCall----" + response.body!!.string() + " at ${System.currentTimeMillis() - startTime}")
                    }

                })
//
            }
        }


    }

    //10.修改配置
    override fun perConfiguration() {
        newSingleThreadContext("perConfiguration").use {
            CoroutineScope(it).launch {
                val startTime = System.currentTimeMillis()
                val formBody = FormBody.Builder().add("search", "Jurassic Park").build()
                val request = Request.Builder().url("https://en.wikipedia.org/w/index.php").post(formBody).build()
                val client1 = client.newBuilder().connectTimeout(500, TimeUnit.MILLISECONDS).build()
                try {
                    println("perConfiguration client1 execute at ${System.currentTimeMillis() - startTime}")
                    client1.newCall(request).execute().use { response ->
                        println("perConfiguration Response 1 succeeded: $response")
                    }
                } catch (e: IOException) {
                    println("perConfiguration Response 1 failed: $e at ${System.currentTimeMillis() - startTime}")
                }
                val client2 = client.newBuilder().connectTimeout(3000, TimeUnit.MILLISECONDS).build()
                try {
                    println("perConfiguration client2 execute at ${System.currentTimeMillis() - startTime}")
                    client2.newCall(request).execute().use { response ->
                        println("perConfiguration Response 2 succeeded: $response")
                    }
                } catch (e: IOException) {
                    println("perConfiguration Response 2 failed: $e at ${System.currentTimeMillis() - startTime}")
                }

            }
        }
    }

    //11.http basic身份认证
    override fun httpAuthentication() {
        newSingleThreadContext("httpAuthentication").use {
            CoroutineScope(it).launch {
                val request = Request.Builder().url("https://publicobject.com/secrets/hellosecret.txt").build()
                val client1 = client.newBuilder().authenticator(object : Authenticator {
                    @Throws(IOException::class)
                    override fun authenticate(route: Route?, response: Response): Request? {
                        if (response.request.header("Authorization") != null) {
                            return null // Give up, we've already attempted to authenticate.
                        }
                        println("httpAuthentication Authenticating for response: $response")
                        println("httpAuthentication Challenges: ${response.challenges()}")
                        val credential = Credentials.basic("jesse", "password1")
                        return response.request.newBuilder().header("Authorization", credential).build()
                    }
                }).build()
                client1.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println("httpAuthentication-----onFailure  ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        println("httpAuthentication----- ${response.body!!.string()}")
                    }

                })
                client1.newCall(request).execute().use { response ->

                }
            }
        }
    }

    //12.1 应用程序拦截器
    override fun appInterceptors() {
        newSingleThreadContext("appInterceptors").use {
            CoroutineScope(it).launch {
                val request = Request.Builder().url("http://www.publicobject.com/helloworld.txt")
                    .header("User-Agent", "OkHttp Example").build();
                val client1 = client.newBuilder().addInterceptor(AppInterceptor()).build()
                client1.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println("appInterceptors-----onFailure ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        println("appInterceptors-----${response.body?.charStream()?.readText()}")
                    }
                })
            }
        }
    }

    //12.2 网络拦截器
    override fun networkInterceptors() {
        newSingleThreadContext("networkInterceptors").use {
            CoroutineScope(it).launch {
                val request = Request.Builder().url("https://www.publicobject.com/helloworld.txt")
                    .header("User-Agent", "OkHttp Example").build();
                val client1 = client.newBuilder().addNetworkInterceptor(NetworkInterceptor()).build()
                client1.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        println("networkInterceptors-----onFailure ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
//                        println("networkInterceptors-----${response.body?.charStream()?.readText()}")
                        //在拦截器中打印，这里就不再打印了
                    }
                })
            }
        }
    }


    private val requestBody = object : RequestBody() {
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
        private val IMGUR_CLIENT_ID = "9199fdef135c122"
        private val MEDIA_TYPE_PNG = "image/png".toMediaType()
    }
}