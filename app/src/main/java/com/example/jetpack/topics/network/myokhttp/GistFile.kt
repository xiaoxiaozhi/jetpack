package com.example.jetpack.topics.network.myokhttp

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class GistFile(var content: String?)
