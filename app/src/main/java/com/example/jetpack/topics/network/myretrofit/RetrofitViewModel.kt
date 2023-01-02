package com.example.jetpack.topics.network.myretrofit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpack.topics.network.myretrofit.model.MultipleResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import kotlin.Exception

/**
 * 1.界面状态
 *   [界面状态](https://developer.android.com/topic/architecture/ui-layer#define-ui-state)
 *   界面状态类的数据都是不可变的(数据类 参数用val修饰)因此，切勿直接在界面中修改界面状态
 *   界面状态命名规则 功能 + UiState。例如 NewsUiState和 NewsItemUiState
 * 2.ViewModel是推荐的状态容器
 *   界面与其 ViewModel 类之间的互动在很大程度上可以理解为事件输入及其随后的状态输出
 *   [ViewModel与界面状态关系图](https://developer.android.com/static/topic/libraries/architecture/images/mad-arch-ui-udf.png)
 * 3.逻辑类型
 *   业务逻辑：例如修改身份信息，获取首页新闻通常位于网域层或者数据层
 *   界面逻辑：点击按钮跳转、显示文本、消息弹框等，这类逻辑应该位于界面(View、Fragment、Activity )，如果界面逻辑过于负责应该创建一个类管理
 * 4.公开界面状态
 *   把不可修改的界面状态公开给UI元素，例如 创建一个RetrofitActivityUiState数据流，将可变数据流(MutableStateFlow)转为不可变数据流(StateFlow)进行公开, 可变数据流通过方法更新。
 *
 *
 *
 */
@HiltViewModel
class RetrofitViewModel @Inject constructor(private val repository: DataRepository,
    private val dataSource: NetworkDataSource) : ViewModel() {
    //4. 公开界面状态
    private val _uiState = MutableStateFlow<RetrofitActivityUiState>(RetrofitActivityUiState.Loading)

    // nowinandroid 省去了_uiState,而是从repository中直接获取流然后在转换成 stateFlow
    val uiState: StateFlow<RetrofitActivityUiState> = _uiState.asStateFlow()
    private var fetchJob: Job? = null
    fun getUiState() {//调用方法更新可修改状态流
        fetchJob?.cancel()
        println("uiState-----${uiState.hashCode()}")
        fetchJob = viewModelScope.launch {
            try {
                val newsItems = repository.getNews()
                println("newsItems-------------${newsItems}")
                _uiState.update {
//                    it.copy(newsItems = newsItems)
                    RetrofitActivityUiState.Success(newsItems)
                }
            } catch (ioe: IOException) {
                // Handle the error and notify the UI when appropriate.
                _uiState.update {
//                    val messages = getMessagesFromThrowable(ioe)
//                    it.copy(userMessages = messages)
                    RetrofitActivityUiState.Exception(ioe)
                }
            }
        }
    }

}

//1. 这个界面状态来自nowinandroid 比官网的更进一步。除了含有页面数据之外，还定义了 等待状态、正常状态、异常状态
sealed interface RetrofitActivityUiState {
    object Loading : RetrofitActivityUiState
    data class Success(val userData: MultipleResource) : RetrofitActivityUiState
    data class Exception(val exception: kotlin.Exception) : RetrofitActivityUiState
}