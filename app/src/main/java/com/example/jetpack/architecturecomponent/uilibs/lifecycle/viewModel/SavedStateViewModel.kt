package com.example.jetpack.architecturecomponent.uilibs.lifecycle.viewModel

import androidx.lifecycle.*

/**
 * 验证 ：开发者模式设为后台不保留活动
 * 1. val filteredData = MutableLiveData<Int>()  置于后台后再开启 没有保留数据
 * 2. 下面这种方式，置于后台后再开启，保留数据
 * 结论 ：效果显著
 * SavedStateHandle 能够保留的数据
 * https://developer.android.google.cn/topic/libraries/architecture/viewmodel-savedstate#types
 */
class SavedStateViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val filteredData: MutableLiveData<Int>
        get() {
            if (!savedStateHandle.contains("query")) {
                savedStateHandle.set("query", 0) //如果从未 设置过，这里要初始化，否则filteredData.value 为空
            }
            return savedStateHandle.getLiveData("query")
        }

    fun add() {
        println("add----value = ${filteredData.value}")
        println("add----value = ${filteredData.value}")
        filteredData.value = filteredData.value?.plus(1)
        println("add----value = ${filteredData.value}")
    }

    fun del() {
        filteredData.apply {
            value = if ((value?.minus(1))!! < 0) {
                0
            } else {
                value!!.minus(1)
            }
        }
    }

}