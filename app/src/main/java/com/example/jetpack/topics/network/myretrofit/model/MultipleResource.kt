package com.example.jetpack.topics.network.myretrofit.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


data class MultipleResource(val page: Int,
    @SerialName("per_page") val perPage: Int,
    val total: Int,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("data") val data: List<Datum>?)

@Serializable
data class Datum(val id: Int, val name: String, val year: Int, @SerialName("pantone_value") val pantoneValue: String)