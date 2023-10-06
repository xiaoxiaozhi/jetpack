package com.example.router

interface RouterAnnotationHandler {
    fun register(url: String, target: String)
}