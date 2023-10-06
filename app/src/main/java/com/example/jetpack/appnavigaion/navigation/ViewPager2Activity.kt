package com.example.jetpack.appnavigaion.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R
import androidx.databinding.DataBindingUtil.setContentView
import com.example.jetpack.databinding.ActivityViewPager2Binding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * [更多操作看官方示例](https://github.com/android/views-widgets-samples/tree/master/ViewPager2)
 * 滑动视图允许您通过水平手指手势或滑动在同级屏幕(如标签)之间导航。这种导航模式也称为水平分页。
 * 1. 依赖
 *    在build.gradle 文件中添加  implementation "androidx.viewpager2:viewpager2:1.0.0"  当前时间2022/10/12
 * 2. 添加<ViewPager2>标签
 *    在xml布局文件中添加
 * 3. 创建适配器
 *    连接 ViewPager2 和 Fragment子布局。 继承FragmentStateAdapter
 * 4. 添加选项卡
 *    TabLayout 提供了一种水平显示选项卡的方法，在 xml布局文件中添加 <TabLayout> 标签
 * 标签样式官方没有提供修改方案，以下为网上搜索
 *    [Android TabLayout 使用以及自定义样式](https://juejin.cn/post/7141944030752931876)
 * 5. 更改指示器样式
 *    自定义Indicator样式可以使用layer-list，修改TabItem的字体可以使用Theme，简单的样式修改可以通过自带的属性进行完成。
 *    app:tabIndicatorFullWidth="false" 指示器宽度与文字相同，要想设置指示器宽度就不能再设置这个属性
 *    app:tabIndicator="@drawable/" 设置指示器形状,实验发现即便在shape中设置颜色也没有作用，想要设置颜色只能用tabIndicatorColor
 *                                  shape和layer-list都可以设置形状，但是后者还能设置指示器与文字的间距、指示器宽度、指示器相对位置 更多设置查看layer-list
 *    app:tabIndicatorColor="@color/album_indicator_color" 指示器颜色，只是颜色
 *    app:tabIndicatorGravity 指示器相对文字的位置，文字之上、文字之下。。。
 * 6. 标签样式
 *    app:tabTextAppearance="@style/TabLayoutTextAppearance" 正常状态下的文字样式，字体颜色起作用
 *                                                           正常样式 TabLayout的item默认大写，可以通过这里设置去除<item name="android:textAllCaps">false</item>
 *    app:tabSelectedTextAppearance="@style/TabSelectedTextAppearance" 选中样式 还可以改变字体大小(字体作用，选中颜色、大小不起作用) 查看源码 选中传进去的字体大小没有用到
 *    [这篇文章解决了选中改变字体大小的问题](https://blog.csdn.net/qq_34895720/article/details/107999808)
 *    [这个demo实现了选中字体变大的功能](https://github.com/loper7/tablayout-ext)
 *    app:tabSelectedTextColor 选中颜色，这个起作用
 *    网上说要在tabLayout.addOnTabSelectedListener对选中的修改 不起作用
 * 7. TabLayout item背景
 *    app:tabIndicator="@drawable/album_indicator" 设置选中状态和未选中状态的背景
 *    app:tabRippleColor="@android:color/transparent" 去除点击水波纹效果
 *    注意：TabLayout 父布局如果是ConstraintLayout， TabLayout的标签将无法点击，换成LinerLayout就可以点。但是行车记录仪项目就没出现这个问题，很是奇怪
 */
class ViewPager2Activity : AppCompatActivity() {
    lateinit var binding: ActivityViewPager2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_view_pager2)
        val adapter = DemoCollectionAdapter(this)
        //3. 创建适配器
        binding.viewPager.adapter = adapter
        //4. 添加选项卡
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = "OBJECT ${(position + 1)}"
//            tab.setIcon(getTabIcon(position))//标签图标
        }
            //attach 将 TabLayout 和 ViewPager2连接在一起，必须在ViewPager2设置适配器之后调用。TabLayoutMediator更换实例 或者 更改适配器之后也要调用
            .attach()
//        binding.tabLayout.addOnTabSelectedListener()
    }
}