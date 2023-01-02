package com.example.jetpack.topics.network.myretrofit

import com.example.jetpack.topics.network.myretrofit.model.MultipleResource
import com.example.jetpack.topics.network.myretrofit.model.User
import com.example.jetpack.topics.network.myretrofit.model.UserList
import retrofit2.http.*


interface NetworkInterface {

    @GET("/api/unknown")
    suspend fun doGetListResources(): MultipleResource

    @POST("/api/users")
    suspend fun createUser(@Body user: User?): User

    @GET("/api/users?")
    suspend fun doGetUserList(@Query("page") page: String?): UserList

    @FormUrlEncoded
    @POST("/api/users?")
    suspend fun doCreateUserWithField(@Field("name") name: String?, @Field("job") job: String?): UserList
}