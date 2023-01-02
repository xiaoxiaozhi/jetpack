package com.example.jetpack.architecturecomponent.uilibs.paging

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityDataBindingBinding
import com.example.jetpack.databinding.ActivityPagingBinding
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
 * [简书Paging3教程](https://juejin.cn/post/6844904193468137486)
 * Paging 库可帮助您加载和显示来自本地存储或网络中更大的数据集中的数据页面
 * 该库主要包含以下功能：
 * 分页缓存、请求重复信息删除功能、 RecyclerView 适配器，会在用户滚动到已加载数据的末尾时自动请求数据、对 Kotlin 协程和 Flow 以及 LiveData、支持刷新和重试功能
 * 1. 配置Paging
 *    [在build.gradle中配置](https://developer.android.google.cn/topic/libraries/architecture/paging/v3-overview#setup)
 *    implementation "androidx.paging:paging-runtime:3.1.0"
 * 2. Paging架构
 *    ![架构图](https://developer.android.google.cn/static/topic/libraries/architecture/images/paging3-library-architecture.svg)
 *    2.1 Repository 存储层
 *        存储层组件是 PagingSource。每个 PagingSource 对象都定义了数据源(网络和本地)的检索方式，TODO 当数据源发生变化是要 重新生成PagingSource？？？
 *        RemoteMediator 对象会处理来自分层数据源（例如具有本地数据库缓存的网络数据源）的分页。
 *    2.2 ViewModel 数据持有层
 *        PagingData 对象是用于存放分页数据快照的容器。它会查询 PagingSource 对象并存储结果。
 *    2.3 UI
 *        PagingDataAdapter，它是一种处理分页数据的 RecyclerView 适配器。 此外，您也可以使用随附的 AsyncPagingDataDiffer 组件构建自己的自定义适配器。
 * 3. 定义PagingSource
 *    继承PagingSource<Key, Value> key 数据标识符，如果用Retrofit加载数据那么Key就是Int类型的分页页码，Value是数据类型
 *    查看RepoPagingSource 类获取更详细内容
 * TODO PagingConfig
 */
class PagingActivity : AppCompatActivity() {
    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }
    lateinit var binding: ActivityPagingBinding
    private val repoAdapter = RepoAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView<ActivityPagingBinding>(this, R.layout.activity_paging)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = repoAdapter
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
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.recyclerView.visibility = View.VISIBLE
                }
                is LoadState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.INVISIBLE
                }
                is LoadState.Error -> {
                    val state = it.refresh as LoadState.Error
                    binding.progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this, "Load Error: ${state.error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

private class SomeObject(@field:SerializedName("custom_naming") private val someField: String,
    private val someOtherField: String)