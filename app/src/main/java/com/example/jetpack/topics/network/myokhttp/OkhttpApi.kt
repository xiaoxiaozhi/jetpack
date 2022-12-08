package com.example.jetpack.topics.network.myokhttp

interface OkhttpApi {
    //3.1 get方法---同步
    fun synchronousGet()

    //3.2 get 异步
    fun asynchronousGet()

    //4.访问请求头
    fun accessingHeaders()

    //5.1 postingString
    fun postingString()

    //5.2 post stream
    fun postStream()

    //5.3 post File
    fun postFile()

    //5.4 表单上传
    fun postFormParameters()

    //5.5 多种请求体上传 表单+文件
    fun postMultipart()

    //6.用Moshi解析Json
    fun parseJSON()

    //7.使用响应缓存
    fun responseCaching()

    //8.取消调用
    fun cancelCall()

    //10.修改配置
    fun perConfiguration()

    //11.http basic身份认证
    fun httpAuthentication()

    //12.1 应用程序拦截器
    fun appInterceptors()

    //12.2 网络拦截器
    fun networkInterceptors()

}