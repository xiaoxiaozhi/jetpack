package com.example.jetpack.lifecycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * 1. ViewModel绝不能引用视图、生命周期或任何可能包含对活动上下文的引用的类。
 * 2. ViewModel生命周期长于Activity和Fragment，只有当Activity，finish()的时候，viewModel才会被销毁 onCleared
 */
class MyViewModel : ViewModel() {
    val users: MutableLiveData<String> by lazy {
        MutableLiveData<String>()

    }

    class User {

    }

    private fun loadUsers() {
        // Do an asynchronous operation to fetch users.
    }
}