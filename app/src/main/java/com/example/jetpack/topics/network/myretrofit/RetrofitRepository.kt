package com.example.jetpack.topics.network.myretrofit


import com.example.jetpack.topics.network.myretrofit.model.MultipleResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class RetrofitRepository @Inject constructor(private val dataSource: NetworkDataSource) : DataRepository {
    //    override suspend fun getNews(): Flow<MultipleResource> = flow {
//        emit(dataSource.doGetListResources())
//    }
    override suspend fun getNews(): MultipleResource = dataSource.doGetListResources()

}