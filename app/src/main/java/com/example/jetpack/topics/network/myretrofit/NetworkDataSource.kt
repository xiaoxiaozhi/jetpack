package com.example.jetpack.topics.network.myretrofit

import com.example.jetpack.topics.network.myretrofit.model.MultipleResource
import com.example.jetpack.topics.network.myretrofit.model.User
import com.example.jetpack.topics.network.myretrofit.model.UserList

interface NetworkDataSource {

    suspend fun doGetListResources(): MultipleResource


    suspend fun createUser(user: User?): User


    suspend fun doGetUserList(page: String?): UserList


    suspend fun doCreateUserWithField(name: String?, job: String?): UserList
}