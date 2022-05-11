package com.example.jetpack.topics.appstartup

import android.content.Context
import androidx.startup.Initializer
import com.hi.dhl.startup.library.LibraryA
import java.util.*

class InitializerA : Initializer<LibraryA> {
    override fun create(context: Context): LibraryA {
        println("InitializerA--------------------create")
        return LibraryA()
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        println("InitializerA-------------------dependencies")
        return mutableListOf(InitializerB::class.java)
    }
}