package com.example.jetpack.architecturecomponent.uilibs.lifecycle.livedata

import android.icu.math.BigDecimal
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.delay

/**
 *  1. 网络连接类 通过回调刷新 LiveData。LiveData活跃的时候 StockManager 注册回调。 不活跃时 注销回调
 *
 */
class StockLiveData(symbol: String) : LiveData<BigDecimal>() {

    //    private val stockManager = StockManager(symbol)
    private val listener = { price: BigDecimal -> value = price }
    override fun onActive() {// 观察者的生命周期处于 STARTED 或 RESUMED 状态，则 LiveData 会认为该观察者处于活跃状态,触发onActive
//        stockManager.requestPriceUpdates(listener)
    }

    override fun onInactive() {//当 LiveData 对象没有任何活跃观察者时，会调用 onInactive() 方法
//        stockManager.removeUpdates(listener)
    }

    companion object {
        private lateinit var sInstance: StockLiveData

        @MainThread
        fun get(symbol: String): StockLiveData {
            sInstance =
                if (::sInstance.isInitialized) sInstance else StockLiveData(symbol)
            return sInstance
        }
    }
}