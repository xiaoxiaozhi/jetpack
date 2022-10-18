package com.example.jetpack.bestpractice.mypractices

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jetpack.R
import com.example.jetpack.architecturecomponent.uilibs.paging.MainViewModel
import com.example.jetpack.architecturecomponent.uilibs.paging.RepoAdapter
import com.example.jetpack.databinding.ActivityPagingBinding
import com.example.jetpack.databinding.FragmentDefaultBinding
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.launch

/**
 * MD风格应用栏有常规和折叠两种
 * [MD应用栏官方指南](https://m3.material.io/components/top-app-bar/implementation)
 * 1. CoordinatorLayout: 协同布局
 *    个人认为 协同布局分为两个部分，AppBarLayout + 滑动控件 前者是观察者 根据滑动控件的滑动决定自己的动作；后者不一定是个滑动控件只要包含滑动控件就可以
 *    协同布局主要有两个用途，作为应用顶层布局提供容器给子view交互。协同布局根据子view的Behaviors确定行为。子view想要实现行为需要继承AttachedBehavior
 *    嵌套滑动机制，父View需要实现 NestedScrollingParent 接口,而子View需要实现 NestedScrollingChild 接口。CoordinatorLayout实现了NestedScrollingParent
 *    在xml中用  app:layout_behavior=@string/appbar_scrolling_view_behavior 设置Behaviors
 * 2. AppBarLayout
 *    是一个垂直的 LinearLayout，实现了MD设计风格中 应用栏 滚动概念。在协同布局中，跟随同级的滑动控件滑动。子元素通过设置 AppBarLayout.LayoutParams.setScrollFlags(int) 或者 app:layout_scrollFlags.设置滚动方式
 *    AppBarLayout必须作为CoordinatorLayout的子View才能实现滚动功能
 *    app:layout_scrollFlags 属性解析
 *    scroll：作为滑动控件的一部分，其它标志生效都要依赖他，必须同时指定
 *    noScroll：不会跟随滑动控件滑动
 *    exitUntilCollapsed：显示状态下，上滑离开屏幕时，会被折叠到最小高度 android:minHeight
 *    enterAlways:隐藏状态下，下拉就会显示
 *    enterAlwaysCollapsed:隐藏状态下，下拉先显示最小高度(android:minHeight)，继续拉显示全部高度
 *    snap:如果视图只剩25%可见，视图就会自动滑动至隐藏。如果视图已经显示75%，视图就会自动滑动至显示
 *    snapMargins:与snap一起使用，效果基本一致，显示或者隐藏会包含marginTop或者marginBottom的距离
 *    note:AppBarLayout根据实验发现多个子view,第一个子view最起码设置一个scroll否则整个APPBarLayout都不会跟随滑动。
 * 3. Behaviors
 *    Behavior的原理就是观察者模式的应用，被观察者就是事件源dependency，观察者就是做出改变的child。自定义Behavior 查看代码FollowBehavior
 *    注意RecycleView
 *    note：滑动控件RecycleView不加@string/appbar_scrolling_view_behavior 会显示在AppBarLayout下面。
 *         CollapsingToolbarLayout是ViewGroup子类，不是LinearLayout，这个behavior好像是让滑动控件处于AppBarLayout下面的标志
 * 4. CollapsingToolbarLayout
 *    个人认为CollapsingToolbarLayout 的目的是 如果 AppBarLayout 有好几个子View把他们都放到CollapsingToolbarLayout里面处理
 *    折叠应用栏布局，同样继承了NestedScrollingChild，实现了一个可折叠的应用程序栏。它被设计用作 AppBarLayout 的直接子级。CollapsingToolbarLayout 包含以下特性:
 *
 * TODO [状态栏和应用栏保持颜色一致](https://developer.android.com/develop/ui/views/layout/edge-to-edge)
 * TODO 应用栏的menu 没有上下居中是什么原因. 同样的menu移植到 sunflower就居中 为什么？？？？
 */
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DefaultFragment : Fragment() {
    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }
    private val repoAdapter = RepoAdapter()
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
        binding.recycler.layoutManager = LinearLayoutManager(activity)
        binding.recycler.adapter = repoAdapter
        lifecycleScope.launch {
            viewModel.getPagingData().collect {
                repoAdapter.submitData(it)
            }
        }
//        setHasOptionsMenu(true)
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