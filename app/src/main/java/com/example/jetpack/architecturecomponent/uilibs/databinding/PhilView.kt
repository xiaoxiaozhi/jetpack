package com.example.jetpack.architecturecomponent.uilibs.databinding

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.core.widget.NestedScrollView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener


class PhilView(context: Context, attributeSet: AttributeSet?) :
    NestedScrollView(context, attributeSet) {
    private val TAG = "调试"
    private var isRefreshing = false
    private var mInverseBindingListener: InverseBindingListener? = null

    @BindingAdapter(value = ["refreshing"], requireAll = false)
    fun setRefreshing(view: PhilView?, refreshing: Boolean) {
        isRefreshing = if (isRefreshing == refreshing) {
            //防止死循环
            Log.d(TAG, "重复设置")
            return
        } else {
            Log.d(TAG, "setRefreshing $refreshing")
            refreshing
        }
    }

    @InverseBindingAdapter(attribute = "refreshing", event = "refreshingAttrChanged")
    fun getRefreshing(view: PhilView?): Boolean {
        return isRefreshing
    }

    @BindingAdapter(value = ["refreshingAttrChanged"], requireAll = false)
    fun setRefreshingAttrChanged(view: PhilView, inverseBindingListener: InverseBindingListener?) {
        Log.d(TAG, "setRefreshingAttrChanged")
        if (inverseBindingListener == null) {
            view.setRefreshingListener(null)
        } else {
            mInverseBindingListener = inverseBindingListener
            view.setRefreshingListener(mOnRefreshingListener)
        }
    }

    override fun onScrollChanged(x: Int, y: Int, oldx: Int, oldy: Int) {
        super.onScrollChanged(x, y, oldx, oldy)
        if (y < oldy && y == 0) {
            if (isRefreshing) {
                Log.d(TAG, "正在刷新，请勿重复加载")
                return
            } else {
                longTimeTask()
            }
        }
    }

    fun setRefreshingListener(listener: OnRefreshingListener?) {
        mOnRefreshingListener = listener
    }

    inner abstract class OnRefreshingListener {
        open fun startRefreshing() {
            isRefreshing = true
            mInverseBindingListener?.onChange()
        }

        open fun stopRefreshing() {
            isRefreshing = false
            mInverseBindingListener?.onChange()
        }
    }

    private var mOnRefreshingListener: OnRefreshingListener? = object : OnRefreshingListener() {
        override fun startRefreshing() {
            super.startRefreshing()
        }

        override fun stopRefreshing() {
            super.stopRefreshing()
        }
    }

    private fun longTimeTask() {
        Thread {
            mOnRefreshingListener!!.startRefreshing()
            try {
                //假设这里作了一个长时间的耗时操做
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            mOnRefreshingListener!!.stopRefreshing()
        }.start()
    }
}