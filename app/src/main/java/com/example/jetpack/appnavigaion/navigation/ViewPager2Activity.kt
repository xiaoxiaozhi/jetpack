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
 * TODO 标签样式官方没有提供更改方法
 */
class ViewPager2Activity : AppCompatActivity() {
    lateinit var binding: ActivityViewPager2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView<ActivityViewPager2Binding>(this, R.layout.activity_view_pager2)
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
    }

}