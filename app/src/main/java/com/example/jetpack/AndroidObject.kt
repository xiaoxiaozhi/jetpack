package com.example.jetpack

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object AndroidObject {
    val executorService: ExecutorService by lazy {
        Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
        )
    }
}