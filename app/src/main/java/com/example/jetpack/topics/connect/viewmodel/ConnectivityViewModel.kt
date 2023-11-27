package com.example.jetpack.topics.connect.viewmodel

import androidx.lifecycle.ViewModel
import com.example.jetpack.topics.connect.util.ConnectivityUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConnectivityViewModel @Inject constructor(private val connectivityUtil: ConnectivityUtil) : ViewModel() {


}