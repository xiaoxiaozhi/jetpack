package com.example.jetpack.architecturecomponent.uilibs.lifecycle.livedata

import androidx.lifecycle.*
import kotlinx.coroutines.delay

/**
 * 1. LiveData中使用协程
 *     当 LiveData 变为活动状态时，代码块开始执行；当 LiveData 变为非活动状态时，代码块会在可配置的超时过后自动取消。
 *     如果代码块在完成前取消，则会在 LiveData 再次变为活动状态后重启；如果在上次运行中成功完成，则不会重启。
 *     请注意，代码块只有在自动取消的情况下才会重启。如果代码块由于任何其他原因（例如，抛出 CancellationException）而取消，则不会重启。
 * note: AndroidViewModel 里面多了一个 application
 */
class NameViewModel : ViewModel() {

    //1. LiveData中使用协程
    val user: LiveData<User> = liveData {
        delay(10 * 1000)// 进行一些耗时操作，获取User
        println("LiveData协程赋值")
        emit(User("first", "last"))
        delay(5 * 1000)
        println("LiveData协程赋值1")
        emit(User("first1", "last1"))// 可以进行多次赋值
//        emitSource()TODO  未知用途，需要看Room
    }
    val currentName: MutableLiveData<String> by lazy { MutableLiveData<String>() }
}