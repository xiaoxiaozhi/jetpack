package com.example.compiler.annotation
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class Function(val name: String)
