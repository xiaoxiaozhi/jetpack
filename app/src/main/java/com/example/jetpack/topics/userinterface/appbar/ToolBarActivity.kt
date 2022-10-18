package com.example.jetpack.topics.userinterface.appbar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityToolBarBinding

/**
 * 默认主题背景的Activity均使用ActionBar作为应用栏,不同版本原生ActionBar的行为会有所不同，相比之下Toolbar拥有所有功能，在任何版本中都能使用。建议使用Toolbar
 * 1. 添加Toolbar
 *    1.1 在应用清单中，将<application>元素设置为使用 NoActionBar 主题背景  android:theme="@style/Theme.AppCompat.Light.NoActionBar"。 该例在清单中的<activity>标签中使用了无actionbar主题
 *    1.2 向 Activity 的布局添加一个 Toolbar
 *    1.3 在 Activity 的 onCreate() 方法中，调用 Activity 的 setSupportActionBar()
 *    默认情况下，操作栏只包含应用的名称和一个菜单
 * 应用栏高度 Material Design [规范](https://material.io/design/components/app-bars-bottom.html)。
 * 2. 添加菜单按钮
 *    作为app的toolbar(执行setSupportActionBar)添加菜单 在onCreateOptionsMenu中添加菜单
 *    作为单独控件(不执行setSupportActionBar)toolbar.inflateMenu(R.menu.menu_main)
 *    菜单栏点击回调方法 onOptionsItemSelected
 *    note:在Fragment中添加 需要调用
 *          (activity as AppCompatActivity).setSupportActionBar(binding.topAppBar)
 *          setHasOptionsMenu(true)
 *          复写 onCreateOptionsMenu
 * 3. 返回app主屏幕操作
 *    在 Activity 中支持向上功能。 添加后会在toolbar 左边增加 返回按钮. 在activity标签中  android:parentActivityName =父activity 其次在onCreate中调用supportActionBar?.setDisplayHomeAsUpEnabled(true)
 *    该例请看ToolBar1Activity
 *    note 这和导航组件是不同的导航方式
 * 4. toolbar 操作视图
 *    在toolbar中提供丰富功能的操作。例如“搜索”操作视图
 *    4.1 添加操作视图
 *        在menu中添加一个item <item app:actionViewClass:操作视图  app:actionLayout：描述操作组件的布局资源;如果用户未与此微件互动，应用会将此微件显示为 android:icon 指定的图标
 *        获取 actionView
 *        添加 展开监听事件   TODO 这两个有什么用？？？
 *    4.2 添加操作提供器
 *         <item app:actionProviderClass="android.support.v7.widget.ShareActionProvider" 不必为此添加操作视图，因为 ShareActionProvider 提供了自己的图形
 *         [自定义ActionProvider](https://blog.csdn.net/yanzhenjie1003/article/details/51902796) 查看该博客之后发现，actionProvider作用其实是提供视图的。
 * TODO  SearchView 需要提供provider吗？如果要怎么提供
 */
class ToolBarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityToolBarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToolBarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //1.3
        setSupportActionBar(binding.toolbar)


        binding.button1.setOnClickListener {
            startActivity(Intent(this, ToolBar1Activity::class.java))
        }
    }

    //2. 添加菜单
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        // 搜索回调
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //文字提交的时候哦回调，newText是最后提交搜索的文字
                return false;
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //在文字改变的时候回调，query是改变之后的文字
                return false
            }

        })
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                return true
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_favorite -> {
            println("action_favorite-----------------")
            true
        }
        R.id.action_settings -> {
            println("action_settings-----------------")
            true
        }
        else -> false
    }

}