package com.example.jetpack.topics.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * 1. 如果组件通过调用 startService() 启动服务，则服务会一直运行，直到其使用 stopSelf() 自行停止运行，
 *    或由其他组件通过调用 stopService() 将其停止为止。
 * TODO 查看onStartCommand的注释发现要和StopSelfResult一起使用，那么StopSelfResult是什么
 */
class StartService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        println("${this.javaClass.simpleName}------onCreate-----hashCode---${this?.javaClass.hashCode()}")
    }

    /**
     * startId 如果服务同时处理多个对 onStartCommand() 的请求，每次启动服务startId都会+1(初始值1)则您不应在处理完一个启动请求之后停止服务，
     * 因为您可能已收到新的启动请求（在第一个请求结束时停止服务会终止第二个请求）。
     * 为避免此问题，您可以使用 stopSelf(int) 确保服务停止请求始终基于最近的启动请求。
     *
     * 个人认为可以通过intent的携带的数据控制service的行为
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("startId----$startId-----hashCode---${this?.javaClass.hashCode()}")
        return START_STICKY
    }

    override fun onDestroy() {
        println("${this.javaClass.simpleName}------onDestroy-----hashCode---${this?.javaClass.hashCode()}")
        super.onDestroy()

    }
}