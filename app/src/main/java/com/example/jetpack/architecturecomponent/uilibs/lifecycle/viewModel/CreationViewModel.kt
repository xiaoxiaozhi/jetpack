package com.example.jetpack.architecturecomponent.uilibs.lifecycle.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/**
 *
 */
class CreationViewModel(context: Application, val name: String) : ViewModel() {

    companion object {
        val STRING_KEY = object : CreationExtras.Key<String> {}
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return when (modelClass) {
                    CreationViewModel::class.java -> // 通过extras获取自定义参数
                    {
                        val params = extras[STRING_KEY]!!
                        // 通过extras获取application
                        extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]?.let {
                            // 创建 VM
                            CreationViewModel(it, params)
                        } ?: throw IllegalArgumentException("application of CreationViewModel is Unchecked")
                    }
                    else -> throw IllegalArgumentException("Unknown class $modelClass")
                } as T
            }
        }
    }
//    companion object {
//        val Factory: ViewModelProvider.Factory = viewModelFactory {
//            initializer {
//                val savedStateHandle = createSavedStateHandle()
//                val myRepository = (this[APPLICATION_KEY] as MyApplication).myRepository
//                MyViewModel(
//                    myRepository = myRepository, savedStateHandle = savedStateHandle
//                )
//            }
//        }
//    }
}