package com.example.jetpack.topics.network.myokhttp.diyHttp

import java.io.File

data class OkhttpItem(val type: String, val url: String, val name: String, val date: Long, val outFile: File) {
    companion object {
        const val MOVIE = "MOVIE"
        const val PHOTO = "PHOTO"
        const val EMR = "EMR"
    }
}
