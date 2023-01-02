package com.example.jetpack.topics.network.myretrofit

import com.example.jetpack.topics.network.myretrofit.model.MultipleResource
import com.example.jetpack.topics.network.myretrofit.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RetrofitDataSource @Inject constructor(val network: NetworkInterface) : NetworkDataSource {
    override suspend fun doGetListResources(): MultipleResource = withContext(Dispatchers.IO) {
        network.doGetListResources()
    }


    override suspend fun createUser(user: User?) = withContext(Dispatchers.IO) {
        network.createUser(user)
    }


    override suspend fun doGetUserList(page: String?) = withContext(Dispatchers.IO) {
        network.doGetUserList(page)
    }


    override suspend fun doCreateUserWithField(name: String?, job: String?) = withContext(Dispatchers.IO) {
        network.doCreateUserWithField(name, job)
    }


}