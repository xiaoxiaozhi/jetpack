package com.example.jetpack.bestpractice.dependencyinjection

import javax.inject.Inject

class AnalyticsServiceImpl  @Inject constructor() : AnalyticsService {
    override fun analyticsMethods() {
        println("AnalyticsServiceImpl---->analyticsMethods")
    }
}