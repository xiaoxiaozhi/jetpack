package com.example.jetpack.topics.network.myokhttp

import okhttp3.Interceptor
import okhttp3.Response
import kotlin.system.measureTimeMillis

/**
 * 自动添加的这些字段
 * User-Agent: OkHttp Example
 * Host: www.publicobject.com
 * Connection: Keep-Alive
 * Accept-Encoding: gzip
 * If-Modified-Since: Tue, 06 Dec 2022 17:10:08 GMT
 */
class NetworkInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        println(
            """
    NetworkInterceptor--------request
    |url----${request.url}
    |connection----${chain.connection()}
    |headers----${request.headers}
    |--------------------------------
    """.trimMargin()
        )
        var response: Response
        measureTimeMillis {
            //把http 替换成 https
            response = chain.proceed(request)
            println(
                """
    NetworkInterceptor--------response
    |url----${response.request.url}
    |protocol----${response.protocol}
    |body----${response.body?.charStream()?.readText()}
    |response headers----${response.headers}
    |---------------------------------
    """.trimMargin()
            )
        }.run {
            println("tack time----${this} ms")
        }
        return response
    }
}