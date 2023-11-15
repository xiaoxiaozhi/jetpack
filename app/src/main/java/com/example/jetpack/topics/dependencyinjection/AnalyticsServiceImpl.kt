package com.example.jetpack.topics.dependencyinjection

import com.example.jetpack.bestpractice.dependencyinjection.AnalyticsService
import javax.inject.Inject

class AnalyticsServiceImpl  @Inject constructor() : AnalyticsService {
    override fun analyticsMethods() {
        println("AnalyticsServiceImpl---->analyticsMethods")
    }
}