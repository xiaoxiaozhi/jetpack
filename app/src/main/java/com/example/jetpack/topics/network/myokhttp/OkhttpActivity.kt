package com.example.jetpack.topics.network.myokhttp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityOkhttpBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * [Okhttp官网](https://square.github.io/okhttp/)
 * [在build.gradle中添加依赖](https://square.github.io/okhttp/#releases)
 * 1.优点
 *   1.1 所有请求共享一个套接字
 *   1.2 连接池减少请求延迟 (这是什么意思？)
 *   1.3 压缩。
 *       用于文件的压缩算法可以大致分为两类：无损压缩和有损压缩
 *       对于图片文件来说，gif 或者 png 格式的文件就是采用了 无损压缩算法(gzip br)
 *       网络上的视频文件和jpeg图片通常采用 有损压缩算法
 *       webp既可以采用无损压缩算法，又可以采用有损压缩算法，不要对文件类型为压缩格式的文件应用如下两种压缩技术。
 *       浏览器发送 Accept-Encoding 首部，其中包含有它所支持的压缩算法，以及各自的优先级(Accept-Encoding:br,gzip)服务器则从中选择一种，使用该算法对响应的消息主体进行压缩，
 *       并且发送 Content-Encoding 首部来告知浏览器它选择了哪一种算法
 *       okhttp 使用压缩算法 request.addHeader("Accept-Encoding", "gzip")
 *   1.4 使用缓存避免频繁请求网络
 * 2.功能
 *   2.1 请求 每个请求都要有一个URL、 一个方法(get或者post等)、一个请求头。连接失败的情况下，okhttp会持续请求
 *   2.2 响应 返回头、响应码(404找不到或者200成功)、返回body。响应一个请求，如果使用了透明压缩，OkHttp 将删除相应的响应头 Content-Encoding 和 Content-Llength，因为它们不适用于解压缩后的响应主体。
 *   2.3 调用 同步、异步
 *   2.4 dispatch
 * --------------------------------使用-------------------------------
 * 3.get方法
 *   如果body小于1M，可以使用response.body!!.string()获取 否则 请使用流的方式获取。response 和 body的流用完都要关闭
 *   3.1 execute 同步, 同一时间内只能存在一个 execute。执行完才能执行下一个。开启多个线程每个线程都执行execute的话会报SocketException: Connection reset。 如果想并发多个请求请使用 异步api 即 enqueue
 *   3.2 enqueue 异步方式获取. 异步方式的回调 onFailure()方法好像能捕获异常，发现取消调用 cancelCall()产生的异常在 onFailure中捕获
 * 4.访问请求头
 *   http请求头有的可以存在多对(例如Accept)，有的只能一对(例如User-Agent)。 header(name, value)设置那些仅有一对的值，重复设置会覆盖之前存在的值
 *   addHeader(name,value)设置那些可以存在多对的值，重复设置会同时存在多对，不会覆盖
 * 5.post方法
 *   5.1 post 字符串 不要发送大于1M的字符串
 *   5.2 post 流 继承RequestBody 覆写contentType() 和 writeTo(sink: BufferedSink)。前者确定流内容的MIME属性，后者生成一个输出流传递给服务器
 *   5.3 post 文件
 *   5.4 表单上传 使用FormBody.Builder设置键值对
 *   5.5 多种请求体 表单+文件上传 MultipartBody
 * 6.用Moshi解析json返回
 *   利用返回头的Content-Type确定 response.body?.charStream()格式 默认utf-8编码
 *   TODO [官方Moshi](https://github.com/square/moshi) 现代json解析库更适合kotlin
 *   TODO [掘金Moshi教程](https://juejin.cn/post/6969841959082917901)
 * 7.响应缓存
 *   一个用户一天内都打开多次某个页面，而这个页面的内容相对固定，并不是每次都更改。那么我们有必要每次都从服务器中下载资源吗？答案是不用的。此时缓存就排上用场了
 *   缓存实现:需要一个可以读写、有大小限制、私有的缓存目录(cacheDir就很合适)，大多数应用程序应该只调用一次 new OkHttpClient ()，每个实例设置的缓存目录不能一样，否则会损坏缓存内容
 *   查看代码 OkHttpProviderModule 了解如何设置。在请求头中设置Cache-Control: max-stale=3600 。okhttp将按照这个缓存(会覆盖自己设置的缓存吗？？？)
 *   测试发现增加缓存后第一次请求800多毫秒第二次四五十毫秒。
 *   [缓存相关知识](https://juejin.cn/post/6844903974118621191)
 *   attention： CacheControl.FORCE_CACHE禁止使用缓存 CacheControl.FORCE_NETWORK禁止使用网络
 * 8.取消调用
 *   Call.Cancel()立即停止正在进行的调用。如果一个线程当前正在写入请求或读取响应，它将接收一个 IOException。当不再需要调用时，可以使用此选项来节省网络;
 *   例如，当用户从应用程序导航离开时。可以取消同步和异步调用。
 *   TODO 协程取消的时候会执行finally，在finally中取消掉调用
 * 9.连接超时
 *   okhttp支持connect, write, read, 和 call超时 查看代码 OkHttpProviderModule
 *10.更改单个配置
 *   所有的配置都在OkHttpClient.Builder()中创建，但是某个调用想修改配置，又不想再实例化一个Okhttp，请使用 okhttp实例.newBuild()重新配置
 *11.http basic身份认证
 *   Basic认证通过核对用户名、密码的方式，来实现用户身份的验证。服务端首先返回401要求身份认证，客户端再填入 用户名和密码访问
 *   [一文读懂HTTP Basic身份认证](https://juejin.cn/post/6844903586405564430)
 *12.拦截器
 *   继承Interceptors实现自定义。Interceptor.Chain val response = chain.proceed(request) 是拦截器的关键。如果想多次调用chain.proceed()要先关闭之前的response
 *   拦截器分为两种 应用程序拦截器 和 网络拦截器
 *   [这张图可以看到拦截器在请求-响应中的位置](https://square.github.io/okhttp/assets/images/interceptors%402x.png)
 *   12.1 Application Interceptors
 *        使用 OkHttpClient.Builder().addInterceptor( LoggingInterceptor()) 注册app拦截器
 *   12.2 Network Interceptors
 *        使用OkHttpClient.Builder().addNetworkInterceptor(LoggingInterceptor()) 注册网络拦截器
 *13.Call(调用)
 *   13.1 重写请求：okhttp为您自动添加这些请求头部属性 Content-Length(实测没有), Transfer-Encoding(实测没有), User-Agent, Host, Connection,  Content-Type(实测没有),和 Accept-Encoding(默认gzip，已经添加过的okhttp不会自动给你换成gzip).
 *   13.2 重写响应：如果使用了透明压缩，OkHttp 将删除相应的响应头 Content-Encoding 和 Content-Llength，因为它们不适用于解压缩后的响应主体。(测试代码12.2 发现没有删除 Content-Llength 是因为 虽然请求了但是服务端没有使用gzip吗？？？)
 *   13.3 异常重试: retryOnConnectionFailure(true) 默认20次默认开启 无法自定义次数。 为了满足需要我们自定义重试拦截器 RetryInterceptor
 *14.Events(事件)
 *   能捕获http请求过程中所有指标，根据这些信息改善网络体验
 *15.WebSocket
 *16.https
 *   如果服务器使用CA机构颁发的证书，okhttp访问https就像http一样，否则就要配置证书
 *   [自定义证书配置方法](https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/CustomTrust.java)
 *   [蛋老师讲HTTPS](https://www.bilibili.com/video/BV1KY411x7Jp/?spm_id_from=333.788.recommend_more_video.0&vd_source=9cc1c08c51cf20bda524430137dc77bb)
 *   [TLS握手过程](http://m.qpic.cn/psc?/V54UN84b0OHfN43eIX713mRT5H07gkzM/bqQfVz5yrrGYSXMvKr.cqU*YBjHyoIAKYaSfyuH2DfdWaBq3ZLdJ.Vjca6LwC5zATGv2**n2myd6Ew71if8vKLg*MEe0Bpy1MNzXdBisnLc!/b&bo=jQamAwAAAAABBw4!&rf=viewer_4)
 *   官网关于https
 *   第一部分说了 加密套件和TLS版本的配置方法。还有 如果服务器没配置android会报什么错，并提供了一个网站显示服务端的密码套件和TLS 配置
 *   第二部分说了 拒绝某些证书和某些机构颁发的正数
 *   第三部分说了 如何使用自定义证书
 * attention: Android P开始 必须使用https，使用http连接会报java.net.UnknownServiceException: CLEARTEXT communication to not permitted by network security
 *           [解决使用http链接报异常](https://blog.csdn.net/weixin_28871645/article/details/114612921)
 *attention:连接复用 使用keep-alive请求头实现链接复用 在请求-响应结束后 服务端并不会马上释放连接，会有一个超时释放时间，在期间收到相同请求，会使用之前的连接处理请求。在HTTP1.1中是默认开启的
 *          [okhttp 连接复用](https://blog.csdn.net/Androidxiaofei/article/details/125686437)
 *          HTTP Cookie是服务器发送到用户浏览器并保存在本地的一小块数据。浏览器会存储 cookie 并在下次向同一服务器再发起请求时携带并发送到服务器上。
 *          通常，它用于告知服务端两个请求是否来自同一浏览器——如保持用户的登录状态。Cookie 使基于无状态的 HTTP 协议记录稳定的状态信息成为了可能。
 *attention：http请求头没有设置http版本的字段，根据查询到的资料，这是服务端控制的，当服务端返回协议版本(response.protocol)的时候 客户端按照返回的版本跟服务端沟通
 *
 *TODO 最终实现效果 点击按钮执行请求，结果在对话框中显示,利用上 viewModel、 repository 、Flow
 *TODO kotlin IO待总结
 */
@AndroidEntryPoint
class OkhttpActivity : AppCompatActivity() {
    @Inject
    lateinit var okhttp: OkhttpApi

    lateinit var binding: ActivityOkhttpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ConnectionSpec
//        println("sss----$sss")
        binding = setContentView<ActivityOkhttpBinding>(this, R.layout.activity_okhttp)
        //3.1 get方法---同步
        println("3.1 get方法---同步---------------------")
        okhttp.synchronousGet()
        //3.2 get方法---异步
        println("3.2 get方法---异步---------------------")
        okhttp.asynchronousGet()
        //4.访问请求头
        println("4.访问请求头---------------------")
//        okhttp.accessingHeaders() //不知道为什么报错了，头天晚上还是好的
        //5.1 postingString
        println("5.1 postingString---------------------")
        okhttp.postingString()
        //5.2 post stream
        println("5.2 postingStream---------------------")
        okhttp.postStream()
        //5.3 post file
        okhttp.postFile()
        //5.4 表单上传
//        okhttp.postFormParameters()// API是维基百科国内无法访问
        //5.5 多种请求体上传 表单+文件
//        okhttp.postMultipart()//无法访问
        //6.用Moshi解析Json
//        okhttp.parseJSON()//不知道为什么报错了，头天晚上还是好的
        //7.使用响应缓存
        okhttp.responseCaching()
        //8.取消调用
        okhttp.cancelCall()
        //10.修改配置
        okhttp.perConfiguration()
        //11.http basic身份认证
        okhttp.httpAuthentication()
        //12.1 注册app拦截器
        okhttp.appInterceptors()
        //12.2 注册网络拦截器
        okhttp.networkInterceptors()
        //14. okhttp实现websocket
//        val mClient: OkHttpClient = OkHttpClient.Builder().readTimeout(3, TimeUnit.SECONDS) //设置读取超时时间
//            .writeTimeout(3, TimeUnit.SECONDS) //设置写的超时时间
//            .connectTimeout(3, TimeUnit.SECONDS) //设置连接超时时间
//            .build()
//        //连接地址
//        val url = "ws://xxxxx"
//        //构建一个连接请求对象
//        val request = Request.Builder().get().url(url).build();
//        mClient.newWebSocket(request, object :WebSocketListener() {
//            onOpen()，连接成功
//            onMessage(String text)，收到字符串类型的消息，一般我们都是使用这个
//            onMessage(ByteString bytes)，收到字节数组类型消息，我这里没有用到
//            onClosed()，连接关闭
//            onFailure()，连接失败，一般都是在这里发起重连操作
//
//        })
    }
}