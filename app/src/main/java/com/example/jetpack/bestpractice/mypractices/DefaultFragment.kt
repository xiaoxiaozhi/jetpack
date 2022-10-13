package com.example.jetpack.bestpractice.mypractices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.jetpack.R
import com.example.jetpack.databinding.FragmentDefaultBinding
import com.google.android.material.appbar.AppBarLayout

/**
 * MD风格应用栏有常规和折叠两种
 * [MD应用栏官方指南](https://m3.material.io/components/top-app-bar/implementation)
 * 1. CoordinatorLayout: 协同布局
 *    协同布局主要有两个用途，作为应用顶层布局提供容器给子view交互。协同布局根据子view的Behaviors确定行为。子view想要实现行为需要继承AttachedBehavior
 *    嵌套滑动机制，父View需要实现 NestedScrollingParent 接口,而子View需要实现 NestedScrollingChild 接口。CoordinatorLayout实现了NestedScrollingParent
 *    在xml中用  app:layout_behavior=@string/appbar_scrolling_view_behavior 设置Behaviors
 * 2. AppBarLayout
 *    是一个垂直的 LinearLayout，实现了MD设计风格中 应用栏 滚动概念。子元素通过设置 AppBarLayout.LayoutParams.setScrollFlags(int) 或者 app:layout_scrollFlags.设置滚动方式
 *    AppBarLayout必须作为CoordinatorLayout的子View才能实现滚动功能
 * TODO 必须要实现 Theme.Material3.Light.NoActionBar 才能使用 滑动应用栏？？？[](https://m3.material.io/components/top-app-bar/implementation)
 * TODO [状态栏和应用栏保持颜色一致](https://developer.android.com/develop/ui/views/layout/edge-to-edge)
 */
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DefaultFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentDefaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = DefaultFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}