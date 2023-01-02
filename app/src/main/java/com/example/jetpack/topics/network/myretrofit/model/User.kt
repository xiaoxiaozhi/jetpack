package com.example.jetpack.topics.network.myretrofit.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class User(val name: String?, val job: String?, val id: String?, val createdAt: String?)