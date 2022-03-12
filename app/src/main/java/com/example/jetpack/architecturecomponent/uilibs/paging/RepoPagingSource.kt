package com.example.jetpack.architecturecomponent.uilibs.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * 继承PagingSource<Int, Repo>() Int 第一个泛型表示页数的类型，(页数不就是整形吗？还能有别的类型？) 第二个表示 每一个item的类型
 */
class RepoPagingSource(private val gitHubService: GitHubService) : PagingSource<Int, Repo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> {
        return try {
            val page = params.key ?: 1 // set page 1 as default
            val pageSize = params.loadSize
            val repoResponse = gitHubService.searchRepos(page, pageSize)
            val repoItems = repoResponse.items
            val prevKey = if (page > 1) page - 1 else null
            val nextKey = if (repoItems.isNotEmpty()) page + 1 else null
            LoadResult.Page(repoItems, prevKey, nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Repo>): Int? = null

}
