package com.example.jetpack.architecturecomponent.uilibs.paging

import com.google.gson.annotations.SerializedName

class RepoResponse(
    @SerializedName("items") val items: List<Repo> = emptyList()
)
