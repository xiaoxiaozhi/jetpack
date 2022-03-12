package com.example.jetpack.architecturecomponent.uilibs.paging

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jetpack.R
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


/**、
 * [Paging 3.0 简介 | MAD Skills](https://mp.weixin.qq.com/s/57KTSyW8NMeishtwiikdUQ) 有必要读一读。这一系列教程还包括WorkManager和Hilt等等
 * [csdn郭霖paging3教程](https://blog.csdn.net/guolin_blog/article/details/114707250)
 * [GSON官方教程](https://github.com/google/gson/blob/master/UserGuide.md) TODO 有必要总结一下
 * 1. 添加paging分页库 implementation "androidx.paging:paging-runtime:3.1.0"
 * 2. 使用paging
 *    ![从数据源到UI整个过程](https://developer.android.google.cn/topic/libraries/architecture/images/paging3-base-lifecycle.png)
 *    2.1 定义数据源pagingSource 查看代码RepoPagingSource
 *    2.2 在ViewModel中设置 PagingData FLow
 *    2.3 定义RecycleView适配器查看代码RepoAdapter 注意使用了DiffUtil 区分新旧数据
 * 3. 从网络和数据库加载数据
 *    TODO 搞定Room再回来看
 *
 */
class PagingActivity : AppCompatActivity() {
    private val viewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    private val repoAdapter = RepoAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paging)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = repoAdapter
        lifecycleScope.launch {
//            viewModel.getPagingData().collect { pagingData ->
//                repoAdapter.submitData(pagingData)
//            }
            viewModel.getPagingData().collect {
                repoAdapter.submitData(it)
            }
        }
        repoAdapter.addLoadStateListener {
            when (it.refresh) {
                is LoadState.NotLoading -> {
                    progressBar.visibility = View.INVISIBLE
                    recyclerView.visibility = View.VISIBLE
                }
                is LoadState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerView.visibility = View.INVISIBLE
                }
                is LoadState.Error -> {
                    val state = it.refresh as LoadState.Error
                    progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this, "Load Error: ${state.error.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}

private class SomeObject(
    @field:SerializedName("custom_naming") private val someField: String,
    private val someOtherField: String
)