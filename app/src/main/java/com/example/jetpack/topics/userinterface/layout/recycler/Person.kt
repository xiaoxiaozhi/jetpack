package com.example.jetpack.topics.userinterface.layout.recycler

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Person(
    val id: Long,
    val name: String,
    val telephone: String,
) : Parcelable
