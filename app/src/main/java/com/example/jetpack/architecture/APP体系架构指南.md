[官方文档](https://developer.android.google.cn/jetpack/guide?hl=en)
> 移动设备也是资源受限的，因此在任何时候，操作系统可能会扼杀一些应用程序进程，为新的应用程序腾出空间。进程的销毁不在用户的控制范围之内，所以不要在四大组件内保存数据
> 并且组件之间不要相互依赖

#### 1. 一般构建原则

1. 从Activity和Fragment生命周期回调方法中分离代码逻辑
2. 通过数据持久化从UI的生命周期中分离数据，这样做及时后台回收你的APP数据仍然不会丢失，当网络连接不稳定的时候，程序仍然能够继续执行

#### 2. 常见的架构原则

1. 分离关注点：Activity或Fragment这些基于界面的类应仅包含处理界面和操作系统交互的逻辑。操作系统可能会根据用户互动或因内存不足等系统条件随时销毁它们，最好尽量减少对它们的依赖
2. 通过数据模型驱动界面。数据模型代表应用的数据。它们独立于应用中的界面元素和其他组件

#### 3. 推荐使用的APP架构

&emsp;&emsp;本页面中提到的构建原则应用于广泛的应用程序，以使它们能够扩展、提高质量和健壮性，并使它们更易于测试，并且视为指导方针，并根据需要调整它们以适应您的需求

1. 一个应用程序最少有三层组成，UI层显示数据；数据层包含APP的业务逻辑并控制数据；domain层充当前者的交互  
   ![一个典型的三层架构APP](https://developer.android.google.cn/topic/libraries/architecture/images/mad-arch-overview.png)

1.1 UI layer 每当数据由于用户交互(例如按下按钮)或外部输入(例如网络响应)而发生变化时，更新UI以反映更改。UI层包括UI元素和UI state。后者依靠&ViewModel实现、提供数据、处理逻辑

```kotlin 
 class NewsViewModel(...) : ViewModel() {
   private val _uiState = MutableStateFlow(NewsUiState())
   val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow() //在 ViewModel中保存 UI state，并作为不可变数据公开，防止界面修改 
}
```

1.2 Domain layer 域层负责封装复杂的业务逻辑或重用业务逻辑。例如创建GPSInfo类处理有关GPS的一切事务
1.3 Data layer 包含了应用程序的业务逻辑，由数据类和数据源(数据库、网络服务器)
组成。数据层由Repository组成，每个Repository可以包含零到多个数据源。每个数据源类应该只负责处理一个数据源，这个数据源可以是文件、网络源或本地数据库。数据源类是应用程序和系统之间进行数据操作的桥梁。

``` kotlin
class ExampleRepository(
    private val exampleRemoteDataSource: ExampleRemoteDataSource, // network 数据源
    private val exampleLocalDataSource: ExampleLocalDataSource // database 数据源
) {   
   val data: Flow<Example> = ...
   suspend fun modifyData(example: Example) { ... } 
   }//这个层公开的数据应该是不可变的，这样它就不会被其他类篡改
```

存储库类是根据它们负责的数据命名的:数据类型 + 存储库。例如: NewsRepository、 MoviesRepository 或 PaymentsRepository。  
数据源类以它们负责的数据和它们使用的源命名。公约内容如下:数据类型 + 源类型 + 数据源。对于数据类型，请使用 Remote 或 Local 使其更通用，因为实现可能会更改。例如: NewsRemoteDataSource 或
NewsLocalDataSource。
数据源需要公开一个用于返回最新新闻（ArticleHeadline 实例的列表）的函数。数据源需要提供一种具有主线程安全性的方式，以便从网络获取最新新闻。为此，它需要依赖于 CoroutineDispatcher 或 Executor
来运行任务。
发出网络请求是由新的 fetchLatestNews() 方法处理的一次性调用：

``` kotlin
class NewsRemoteDataSource(
  private val newsApi: NewsApi,
  private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Fetches the latest news from the network and returns the result.
     * This executes on an IO-optimized thread pool, the function is main-safe.
     */
    suspend fun fetchLatestNews(): List<ArticleHeadline> =
        // Move the execution to an IO-optimized thread since the ApiService
        // doesn't support coroutines and makes synchronous requests.
        withContext(ioDispatcher) {
            newsApi.fetchLatestNews()
        }
    }
}

// Makes news-related network synchronous requests.
interface NewsApi {
    fun fetchLatestNews(): List<ArticleHeadline> //NewsApi接口由 Retrofit实现
}
```

2. 管理组件之间的依赖

- 建议使用 Hilt library 构建依赖

3. 最佳实践

- 不要在四大组件中存储数据
- 减少类之间的依赖，app应该只依赖Android 提供的API 例如 Toast、Context。
- 明确模块之间的功能
- 不要暴露模块内部实现细节。
- 尽量让APP的每个功能都可以独立测试
- 尽可能多的数据持久化 即时在离线状态下用户也能够使用程序

4. MAD

- MAD 可以指导开发者更高效地开发出优秀的移动应用 [ MAD 最佳实践](https://mp.weixin.qq.com/s/Fq6AA2IWpDzjtiRkQZFIwA)

[解决Stack Overflow访问卡慢](https://github.com/justjavac/ReplaceGoogleCDN)

5. 谷歌推荐例子  
   [遵从mvi的6个例子](https://developer.android.google.cn/topic/architecture#samples)

attention:谷歌推荐的架构应该叫什么官网没说，我查询到应该叫[mvi](https://mp.weixin.qq.com/s/KEQZjarMjKaggkxkIjUuxQ)

  
 
