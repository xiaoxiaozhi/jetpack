package com.example.jetpack.architecturecomponent.uilibs.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * 继承PagingSource<Int, Repo>() 此例用 Retrofit加载数据，所以 Key是 Int类型
 * nextKey 为空意味不能再加载
 */
class RepoPagingSource(private val gitHubService: GitHubService) : PagingSource<Int, Repo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> {
        return try {

            val page = params.key ?: 1 // set page 1 as default
            val pageSize = params.loadSize//TODO Repository类中 设置的大小是50 为什么第一次是150
            println("params.key---${params.key} pageSize----${params.loadSize}  $pageSize")
            val repoResponse = gitHubService.searchRepos(page, pageSize)
            val repoItems = repoResponse.items
            val prevKey = if (page > 1) page - 1 else null
            val nextKey = if (repoItems.isNotEmpty()) page + 1 else null
//            if (page == 3) {
//                throw Exception("要死要死")
//            }
            LoadResult.Page(repoItems, prevKey, nextKey)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    /**
     * TODO 这个方法什么时候调用 异常 刷新都调用不了
     * state.anchorPosition 最后访问的索引，如果没有则是null
     * state.closestPageToPosition() 强制列表中的索引到给定索引
     */
    override fun getRefreshKey(state: PagingState<Int, Repo>): Int? {
        println("state.anchorPosition-----${state.anchorPosition}")
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }

}
