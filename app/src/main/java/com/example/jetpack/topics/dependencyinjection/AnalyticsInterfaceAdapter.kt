package com.example.jetpack.bestpractice.dependencyinjection

import javax.inject.Inject

class AnalyticsInterfaceAdapter @Inject constructor(
    private val service: AnalyticsService
) {
    fun hash() {
        println("service hash----${service.hashCode()}")
    }
}