package com.example.jetpack.topics.appstartup

import android.content.Context
import androidx.startup.Initializer
import com.hi.dhl.startup.library.LibraryA
import com.hi.dhl.startup.library.LibraryB
import java.util.*

class InitializerB : Initializer<LibraryB> {
    override fun create(context: Context): LibraryB {
        println("InitializerB---------create")
        return LibraryB()
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        println("InitializerB---------dependencies")
        return mutableListOf()
    }
}