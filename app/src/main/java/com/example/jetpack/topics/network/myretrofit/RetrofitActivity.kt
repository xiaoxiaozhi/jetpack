package com.example.jetpack.topics.network

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityRetrofitBinding
import com.example.jetpack.topics.network.myretrofit.DataRepository
import com.example.jetpack.topics.network.myretrofit.NetworkInterface
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.databinding.DataBindingUtil.setContentView
import com.example.jetpack.topics.network.myretrofit.RetrofitActivityUiState
import com.example.jetpack.topics.network.myretrofit.RetrofitViewModel
import kotlinx.coroutines.flow.collect

/**
 * [cadn retrofit教程](https://blog.csdn.net/m0_37796683/article/details/90702095) 有可用的API，推荐
 * [retrofit 使用指南 来自官网]( https://square.github.io/retrofit/) 过于简单细节也没讲，不推荐
 * [第三方教程](https://www.digitalocean.com/community/tutorials/retrofit-android-example-tutorial) 有可用的api 推荐
 * 1. 添加 依赖
 *    implementation 'com.squareup.retrofit2:retrofit:2.9.0' 支持java8以及android API21+
 * 2. 每个方法都必须有一个 HTTP 注释，用于提供请求方法和相对 URL。@GET方法，"users/{user}/repos" 是URL，
 *    总共有8个注释HTTP GET POST PUT PATCH DELETE OPTIONS HEAD
 * 3. URL格式(自总结Retrofit官网没有)
 *    http url 格式 https://<host>:<port>/<path>?<query>#<frag> 例如 http://www.def456.com?id=5&userName=admin
 *    ?<query>：查询参数，以?开头多个查询参数这样写id=5&userName=admin
 *    #<frag>：片段标识符：以#开头 定位一个网页内部的一个位置的
 * 4. Retrofit从2.6.0开始支持挂起函数功能(即异步回调直接得到结果，不必再返回Call<T>)，这一部分内容官网没有介绍
 *    TODO 异常怎么处理
 *
 * attention:Retrofit不手动设置OkHttpClient也可以运行。(name)
 *
 * [数据层](https://developer.android.com/topic/architecture/data-layer)
 * 数据层由多个存储库组成，其中每个存储库都可以包含零到多个数据源
 * 1.数据层包括 Repository 和 DataSource
 * 2.Repository应该公开 数据流(flow)、可以挂起的增删改查的方法(suspend fun 增删改查() )
 * 3.多层次Repository 负责处理用户身份验证数据的存储库 UserRepository 可以依赖于其他存储库（例如 LoginRepository 和 RegistrationRepository）
 *   传统上，一些开发者将依赖于其他存储库类的存储库类称为 manager，例如称为 UserManager 而非 UserRepository。
 * 4.可信来源
 * 5.线程处理
 * 6.生命周期
 * 7.表示业务模式
 * 8.数据操作类型
 *   感觉这一节主要讲 Repository 什么操作对应什么生命周期
 *   8.1 界面操作：面向界面的操作仅在用户位于特定屏幕上时才相关，当用户离开相应屏幕时便会被取消。如果离开界面还需要继续操作应该使用面向应用的操作
 *   8.2 面向应用的操作：只要应用处于打开状态，面向应用的操作就一直相关。如果应用关闭或进程终止，这些操作将会被取消。例如，缓存网络请求结果
 *   8.3 面向业务操作：面向业务的操作无法取消。它们应该会在进程终止后继续执行。例如，完成上传用户想要发布到其个人资料的照片。使用WorkManager
 * 9.处理错误
 *   使用协程的错误处理、Flow的catch、try catch 块
 *10.常见任务
 *   10.1 网络任务
 *
 * XXXDataSource：每个数据源类应仅负责处理一个数据源，数据源可以是文件、网络来源或本地数据库。数据源类是应用与数据操作系统之间的桥梁。层次结构中的其他层绝不能直接访问数据源；数据层的入口点始终是存储库类。ViewModel绝不能将数据源作为直接依赖项。
 * attention:通常，如果存储库只包含单个数据源并且不依赖于其他存储库，开发者会将存储库和数据源的职责合并到存储库类中。这种情况下，在应用的更高版本中，如果存储库需要处理来自其他来源的数据，请不要忘记拆分这些功能。
 * TODO 该层公开的数据应该是不可变的,哪一层？？？
 */
@AndroidEntryPoint
class RetrofitActivity : AppCompatActivity() {
    lateinit var dataBinding: ActivityRetrofitBinding
    val viewModel: RetrofitViewModel by viewModels<RetrofitViewModel>()

    @Inject
    lateinit var network: NetworkInterface
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = setContentView<ActivityRetrofitBinding>(this, R.layout.activity_retrofit)
        dataBinding.button1.setOnClickListener {
            viewModel.getUiState()
        }


        lifecycleScope.launchWhenResumed {
            println("viewModel.uiState.hashCode()-----${viewModel.uiState.hashCode()}")
            viewModel.uiState.collect {
                println("launchWhenResumed---------------$it-----${Thread.currentThread()}")
                when (it) {
                    is RetrofitActivityUiState.Loading -> "Loading"
                    is RetrofitActivityUiState.Exception -> "${"异常------" + it.exception}"
                    is RetrofitActivityUiState.Success -> "正确"
                }
            }
        }
//        lifecycleScope.launch(Dispatchers.IO) {
//            network.doGetListResources()?.let {
//                println("MultipleResource-------------$it")
//            }
//        }

    }
}

