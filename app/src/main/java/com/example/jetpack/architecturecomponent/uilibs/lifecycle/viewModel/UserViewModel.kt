package com.example.jetpack.architecturecomponent.uilibs.lifecycle.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * 2.5 之前用ViewModelProvider.Factory 创建带参数的ViewModel
 * 1. 带参数的lambda表达式，返回ViewModelProvider.Factory
 * 2. 函数返回ViewModelProvider.Factory
 */
class UserViewModel(val type: String) : ViewModel() {

    companion object {
        //1. 带参数的lambda表达式
        val factory: (String) -> ViewModelProvider.Factory = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        //A.isAssignableFrom(B) 判断 A和B是不是同一个类型或者 A是B的父类
//        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST") return UserViewModel(type) as T
//        }else{
//                    throw IllegalArgumentException("Unknown ViewModel class")
//                }
                    //上面的方法不通用，改成这个
                    return modelClass.getConstructor(String::class.java).newInstance(it)
                }
            }
        }

        //2. 函数返回
        fun provideFactory(type: String): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return modelClass.getConstructor(String::class.java).newInstance(type)
                }
            }
        }
    }

}