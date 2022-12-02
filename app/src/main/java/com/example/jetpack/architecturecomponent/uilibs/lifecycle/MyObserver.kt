package com.example.jetpack.architecturecomponent.uilibs.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

class MyObserver : DefaultLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        println("${this.javaClass.simpleName}-----onCreate")
        //1.3 currentState.isAtLeast
        println("onCreate---isAtLeast----${owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)}")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        println("${this.javaClass.simpleName}-----onStart")
        println("onStart---isAtLeast----${owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)}")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        println("${this.javaClass.simpleName}-----onResume")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        println("${this.javaClass.simpleName}-----onPause")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        println("${this.javaClass.simpleName}-----onStop")
    }

    //TODO 退出AppCompatActivity这个怎么不执行,我自己继承Activity就有 查看HandlerLifeCircle1Activity 怀疑是我自己在onDestroy中实现了？？？
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        println("${this.javaClass.simpleName}-----onDestroy")
    }
}