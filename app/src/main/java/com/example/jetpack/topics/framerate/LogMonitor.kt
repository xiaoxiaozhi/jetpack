package com.example.jetpack.topics.framerate

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Printer

class LogMonitor : Printer {
    //    private val mStackSampler: StackSampler? = null
    private var mStartTimestamp: Long = 0

    // 卡顿阈值
    private val mBlockThresholdMillis = (5 * 16.66).toLong()

    //采样频率
    private val mSampleInterval: Long = 1000

    private val mLogHandler: Handler? = null
    private var isPre = true
    override fun println(x: String?) {
        val sb = StringBuilder()
        if (isPre) {
            // 是在message执行前打印，那么接下来就要开始执行message了，可以开始dump主线程堆栈了
            val stackTrace = Looper.getMainLooper().thread.stackTrace
            for (s in stackTrace) {
                sb.append(s.toString()).append("\n")
            }
//            Log.i(TAG, "sb---${sb.toString()}")
//            Log.i(TAG, "what----${x?.split(":")?.takeIf { it.isNotEmpty() }?.last()}")
            Log.i(TAG, "X---$x")

            //记录开始时间
            mStartTimestamp = System.currentTimeMillis()
            isPre = true
        } else {
            // 在message执行后的打印，可以停止dump线程的堆栈了
            val endTime = System.currentTimeMillis()
            //出现卡顿
            //出现卡顿
            if (isBlock(endTime)) {
                Log.i(TAG, "卡顿----${sb.toString()}")
            }

        }
        sb.clear()
        // 执行一次就改变值，本次是在message执行前，下次肯定是在执行后；本次是执行后，下次肯定是在执行前
        isPre = !isPre
    }

    private fun isBlock(endTime: Long): Boolean {
        return endTime - mStartTimestamp > mBlockThresholdMillis
    }

    companion object {
        const val TAG = "LogMonitor"
    }
}