package com.example.jetpack.architecturecomponent.uilibs.paging

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 定义网络接口
 */
interface GitHubService {

    @GET("search/repositories?sort=stars&q=Android")
    suspend fun searchRepos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): RepoResponse//TODO 不应该返回Call<类> 这样的类型吗？不返回Call还怎么执行 同步和异步？这是okhttp的内容

    companion object {
        private const val BASE_URL = "https://api.github.com/"

        fun create(): GitHubService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GitHubService::class.java)
        }
    }//TODO 每次执行都会创建一个新的 GitHubService ？？？

}
