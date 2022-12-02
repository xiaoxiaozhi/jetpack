package com.example.jetpack.topics.userinterface.collapsinglayout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.*
import com.example.jetpack.R
import androidx.databinding.DataBindingUtil.setContentView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.jetpack.databinding.ActivityCollapsingToolbarLayoutBinding

/**
 * MD风格应用栏有常规和折叠两种
 * [MD应用栏官方指南](https://m3.material.io/components/top-app-bar/implementation)
 * 1. CoordinatorLayout: 协同布局
 *    个人认为 协同布局分为两个部分，AppBarLayout + 滑动控件 前者是观察者 根据滑动控件的滑动决定自己的动作；后者不一定是个滑动控件只要包含滑动控件就可以
 *    协同布局主要有两个用途，作为应用顶层布局提供容器给子view交互。协同布局根据子view的Behaviors确定行为。子view想要实现行为需要继承AttachedBehavior
 *    嵌套滑动机制，父View需要实现 NestedScrollingParent 接口,而子View需要实现 NestedScrollingChild 接口。CoordinatorLayout实现了NestedScrollingParent
 *    在xml中用  app:layout_behavior=@string/appbar_scrolling_view_behavior 设置Behaviors
 * 2. AppBarLayout
 *    是一个垂直的 LinearLayout，实现了MD设计风格中 应用栏 滚动概念。在协同布局中，跟随同级的滑动控件滑动。子view通过设置 AppBarLayout.LayoutParams.setScrollFlags(int) 或者 app:layout_scrollFlags.设置滚动方式
 *    AppBarLayout必须作为CoordinatorLayout的子View才能实现滚动功能,给
 *    app:layout_scrollFlags 属性解析
 *    scroll：作为滑动控件的一部分，其它标志生效都要依赖他，必须同时指定
 *    noScroll：不会跟随滑动控件滑动
 *    exitUntilCollapsed：显示状态下，上滑离开屏幕时，不会完全离开，会留下最小高度 android:minHeight。如果不设置 minHeight 就会AppbarLayout就会滑走
 *                        子view是Toolbar设置minHeight后，标题的底部基准线和菜单项基准线不一致，滑出屏幕最小高度起作用，不设置minHeight最小高度就是toolbar的高度
 *                        子View是CollapsingToolbarLayout设置minHeight，滑出屏幕最小高度不起作用而是Toolbar的高度。不设置minHeight最小高度就是toolbar的高度
 *                        子View是其它，滑出屏幕最小高度起作用
 *    enterAlways:隐藏状态下，下拉就会显示。这个属性有什么用？？？
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
 *    折叠应用栏是对toolbar的包装，比如AppbarLayout下面两个子view，滚动后会发现他们进出屏幕高度不变，而折叠应用栏不一样。滚动的时候能发现明显收缩和伸展的效果，具体看例子。折叠应用栏继承了NestedScrollingChild，实现了一个可折叠的应用程序栏。它被设计用作 AppBarLayout 的直接子级。
 *    CollapsingToolbarLayout 包含以下特性
 *    title：应用栏可见时标题显示最大，不可见时显示最小。setTitle()设置标题。collapsedTextAppearance 和 expandedTextAppearance .控制折叠和显示时标题的大小，titleEnabled = true 时覆盖toolbar设置的title
 *    contentScrim：折叠背景 setContentScrim (Drawable) setContentScrimColor (int color) setContentScrimResource (int resId) 等等 折叠到一定程度(我测算大概三分之一)折叠布局背景被设置成该颜色或者图片
 *                  如果Toolbar未设置背景。toolbar的背景也会换成setContentScrim设置的背景，如果设置将保持不变
 *    layout_collapseMode：折叠模式有三个值 none 向上滑动多少就折叠多少(隐藏多少)；  pin 固定不动不跟随折叠；parallax 折叠效果，与layout_collapseParallaxMultiplier共同使用，等于1.0 固定不动效果等于pin，等于0.0 效果等于none
 *    statusBarScrim:折叠时状态栏的颜色，只在android5.0上面管用，并且根布局设置了  android:fitsSystemWindows="false" 。 fitSystemWindows 的作用 Activity布局整体下移留下stataBar的空间，此时管用。但是增加了 这个ViewCompat.setOnApplyWindowInsetsListener不知道为什么不管用了
 *    attention: Toolbar 高度设置wrap_content 标题将不可见，但是菜单可见
 *
 *
 * TODO [状态栏和应用栏保持颜色一致](https://developer.android.com/develop/ui/views/layout/edge-to-edge)
 */
class CollapsingToolbarLayoutActivity : AppCompatActivity() {
    lateinit var binding: ActivityCollapsingToolbarLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding =
            setContentView<ActivityCollapsingToolbarLayoutBinding>(this, R.layout.activity_collapsing_toolbar_layout)
        //// 在这里获取系统状态栏高度后，处理insets重叠以免遮挡。(toolbar 默认太高了看起来不顺眼，布局文件里设置40dp)
        ViewCompat.setOnApplyWindowInsetsListener(binding.appbar) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                (binding.toolbar.layoutParams as ViewGroup.MarginLayoutParams).topMargin = insets.top
            }
            println("pianyi${insets.top}  ${insets.bottom}")
            // Return CONSUMED if you don't want want the window insets to keep being passed down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onStart() {
        super.onStart()

    }
}