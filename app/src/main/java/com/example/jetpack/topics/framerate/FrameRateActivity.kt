package com.example.jetpack.topics.framerate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.Printer
import android.view.FrameMetrics
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityFrameRateBinding

/**
 * 帧率检测
 *
 */
class FrameRateActivity : AppCompatActivity() {
    lateinit var binding: ActivityFrameRateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_frame_rate)
        this.window.addOnFrameMetricsAvailableListener(//向window注册监听
            metricsAvailableListener, Handler(mainLooper)
        )
        mainLooper.setMessageLogging(LogMonitor())
    }

    private val metricsAvailableListener =
        Window.OnFrameMetricsAvailableListener { window, frameMetrics, dropCountSinceLastInvocation ->
            val intent = frameMetrics?.getMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP) ?: 0
            val vsync = frameMetrics?.getMetric(FrameMetrics.VSYNC_TIMESTAMP) ?: 0
            val animation = frameMetrics?.getMetric(FrameMetrics.ANIMATION_DURATION) ?: 0
            val vsyncTotal = frameMetrics?.getMetric(FrameMetrics.TOTAL_DURATION) ?: 0
            val measureCost = frameMetrics?.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION) ?: 0
            //计算帧率
        }

    companion object {
        const val TAG = "FrameRateActivity"
    }

    fun sendAMsg() {
        Thread.sleep(1000)
        object : Handler(mainLooper) {
            override fun handleMessage(msg: Message) {

            }
        }.sendEmptyMessage(123)
    }

}