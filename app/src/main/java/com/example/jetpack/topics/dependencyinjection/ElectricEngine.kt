package com.example.jetpack.topics.dependencyinjection

import javax.inject.Inject

class ElectricEngine @Inject constructor() : Engine {
    override fun start() {
        println("ElectricEngine-----start")
    }

    override fun shutdown() {
        println("ElectricEngine-----shutdown741")
    }
}