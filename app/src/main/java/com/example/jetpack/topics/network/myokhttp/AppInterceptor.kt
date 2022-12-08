package com.example.jetpack.topics.network.myokhttp

import okhttp3.Interceptor
import okhttp3.Response
import kotlin.system.measureTimeMillis

class AppInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        println(
            """
    AppInterceptor--------request
    |url----${request.url}
    |connection----${chain.connection()}
    |headers----${request.headers}
    |--------------------------------
    """.trimMargin()
        )
        chain.connection().toString()
        var response: Response
        measureTimeMillis {
            //把http 替换成 https
            response = chain.proceed(request.newBuilder().url(request.url.toString().replace("http", "https")).build())
            println(
                """
    AppInterceptor--------response
    |url----${response.request.url}
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