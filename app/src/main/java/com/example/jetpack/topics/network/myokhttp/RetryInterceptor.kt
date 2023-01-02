package com.example.jetpack.topics.network.myokhttp

import okhttp3.Interceptor
import okhttp3.Response

class RetryInterceptor : Interceptor {
    var maxRetry: Int = 0
    var retryNum = 0
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        println("request-----${request.headers}")
        var response = chain.proceed(request)
        while (!response.isSuccessful && retryNum < maxRetry) {
            retryNum++;
            println("retryNum=" + retryNum);
            response.close()
            response = chain.proceed(request);
        }
        return response
    }
}