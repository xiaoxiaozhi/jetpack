package com.example.jetpack.topics.dependencyinjection

import javax.inject.Inject

class GasEngine @Inject constructor() : Engine {
    override fun start() {
        println("gas start")
    }

    override fun shutdown() {
        println("gas shutdown")
    }
}