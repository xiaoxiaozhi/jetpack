package com.example.jetpack.architecturecomponent.datalibs.work

import kotlinx.coroutines.*

fun main() {
    runBlocking {
        val lau = launch {
            try {
                delay(5 * 1000)
                println("try")
            } catch (e: CancellationException) {
                println("CancellationException")
            } finally {
                println("finally")
            }
        }
        delay(1000)
        lau.cancelAndJoin()
        println("end")
    }
}