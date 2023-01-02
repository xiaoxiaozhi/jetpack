package com.example.jetpack.topics.network.myretrofit

import com.example.jetpack.topics.network.myretrofit.model.MultipleResource
import kotlinx.coroutines.flow.Flow


interface DataRepository {
//    suspend fun getNews(): Flow<MultipleResource>
    suspend fun getNews(): MultipleResource
}