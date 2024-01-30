package com.example.jetpack.bestpractice.performance

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PerformanceModel @Inject constructor(val pm: PerformanceMonitor) : ViewModel() {

}
