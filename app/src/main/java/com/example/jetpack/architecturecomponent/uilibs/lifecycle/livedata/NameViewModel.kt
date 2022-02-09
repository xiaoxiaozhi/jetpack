package com.example.jetpack.architecturecomponent.uilibs.lifecycle.livedata

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NameViewModel : ViewModel() {
    val currentName: MutableLiveData<String> by lazy { MutableLiveData<String>() }
}