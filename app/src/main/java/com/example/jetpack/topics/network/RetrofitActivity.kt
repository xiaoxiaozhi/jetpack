package com.example.jetpack.topics.network

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.jetpack.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path


/**
 * [okhttp基础教程](https://blog.csdn.net/m0_37796683/article/details/101029208)
 * [cadn retrofit教程](https://blog.csdn.net/m0_37796683/article/details/90702095) TODO retrofit 怎么利用协程？
 * [retrofit 使用指南 来自官网]( https://square.github.io/retrofit/)
 * 1. 添加 依赖
 *    implementation 'com.squareup.retrofit2:retrofit:2.9.0' 支持java8以及android API21+
 * 2. 每个方法都必须有一个 HTTP 注释，用于提供请求方法和相对 URL。@GET方法，"users/{user}/repos" 是URL，
 *    总共有8个注释HTTP GET POST PUT PATCH DELETE OPTIONS HEAD
 */
class RetrofitActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retrofit)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .build()
        lifecycleScope.launch(Dispatchers.Default) {
            val service = retrofit.create(GitHubService::class.java)
            val repos: Call<List<Repo>> = service.listRepos("octocat")
            service.listRepos("xiaoxiaozhi").execute()
        }

    }
}

interface GitHubService {
    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user: String?): Call<List<Repo>>
}

class Repo {

}
