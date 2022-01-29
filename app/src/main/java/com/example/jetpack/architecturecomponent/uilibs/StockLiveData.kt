package com.example.jetpack.architecturecomponent.uilibs

import android.icu.math.BigDecimal
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import java.net.SocketAddress

class StockLiveData(symbol: String) : LiveData<BigDecimal>() {
    private var stockManager: SocketAddress? = null

    private val listener = { price: BigDecimal ->
        value = price
    }

    //当LiveData对象具有活动观察者时，将调用onActive()方法。
    override fun onActive() {
//        stockManager.requestPriceUpdates(listener)
    }

    //
    override fun onInactive() {
//        stockManager.removeUpdates(listener)
    }

    companion object {
        private lateinit var sInstance: StockLiveData

        @MainThread
        fun get(symbol: String): StockLiveData {
            //TODO 什么语法
            sInstance = if (Companion::sInstance.isInitialized) sInstance else StockLiveData(symbol)
            return sInstance
        }
    }
}