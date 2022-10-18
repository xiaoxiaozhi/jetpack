package com.example.jetpack.appnavigaion.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityNavigationBinding

/**

 * 1. 导航组件由以下三个关键部分组成：
 *    1.1 导航图，包含所有导航相关信息的 XML 资源
 *        创建导航图右键点击 res 目录，然后依次选择 New > Android Resource File。在 File name 字段中输入名称，例如“nav_graph”。从 Resource type 下拉列表中选择 Navigation，然后点击 OK。
 *        [导航图创建步骤](https://developer.android.google.cn/guide/navigation/navigation-getting-started)
 *    1.2 目的容器，NavHost
 *        向Activity添加NavHost,查看布局文件R.layout.activity_navigation。android:name 属性实现 NavHost的类名称；app:defaultNavHost="true" 表示NavHostFragment 会拦截系统返回按钮
 *    1.3 导航控制器，NavController 在容器中管理导航对象
 *        获取NavHost三种方式
 *        Fragment.findNavController()    View.findNavController()    Activity.findNavController(viewId: Int)
 * 2. 添加Navigation 支持
 *    查看代码app/build.gradle
 * 3. 使用safe Args 传递数据  查看ScrollingFragment
 *    3.1 在project/build.gradle 中添加  classpath "androidx.navigation:navigation-safe-args-gradle-plugin:2.4.1" 插件
 *    3.1 在app/build.gradle 中添加 id 'androidx.navigation.safeargs'(java 或 java kotlin混合)  id 'androidx.navigation.safeargs.kotlin'(仅支持kotlin)
 * 4. 创建目的地
 *    4.1 从现有的Activity和Fragment创建目的地
 *    4.2 创建新的Fragment目的地，从DialogFragment创建目的地 https://developer.android.google.cn/guide/navigation/navigation-create-destinations#create-dialog
 *    4.3 创建Activity目的地。如果用户导航到其他 Activity，则当前的导航图不再位于作用域内。这意味着，Activity 目的地应被视为导航图中的一个端点。
 *        相当于 startActivity(Intent(context, DestinationActivity::class.java))
 *        TODO Activity的目的还可以添加动态参数，或者根据 xml中Activity的清单来传送数据，待学
 * 5. 设计导航图
 *    嵌套图，![嵌套图](https://developer.android.google.cn/images/topic/libraries/architecture/navigation-design-graph-nested.png)
 *    将 match 屏幕设置为顶级导航图的起始目的地，并将 title 屏幕和 register 屏幕移至嵌套图中，用户以后启动应用时，检查是否有注册用户。如果用户未注册，您可以将其转到注册屏幕
 * 6. 嵌套图
 *    嵌套图对于整理和重复使用应用界面的各个部分（例如独立的登录流程）非常有用。
 *    嵌套图可以封装其目的地。与根图一样，嵌套图必须具有标识为起始目的地的目的地。TODO 嵌套图之外的目的地（例如根图上的目的地）只能通过其起始目的地访问嵌套图。???
 *    在导航图上创建嵌套图：右键点击，目的地以打开上下文菜单，然后依次选择 Move to Nested Graph > New Graph
 *
 *    通过<include>标签引入嵌套图 <include app:graph="@navigation/included_graph"/>
 *    然后在 <action  app:destination="@id/second_graph" /> second_graph 是嵌套图 <navigation>标签的id
 * 7. 全局操作
 *    您可能想要不同目的地中导航到同一目的地。创建全局action。要导航的目的地、右键--->add action--->Global
 *    然后每个 目的地都能调用 view.findNavController().navigate(全局actionId) 导航到这一目的地
 * 8. 导航到目的地
 *    查看ScrollingFragment 代码中有导航方式
 * 9. 使用NavigationUI
 *    9.1 应用栏
 *        利用 NavigationUI 包含的方法，您可以在用户浏览应用的过程中自动更新顶部应用栏中的内容。在根据导航图<Fragment label="标签名称" 自动填充toolbar 的标题
 *        NavigationUI支持以下三种应用栏 Toolbar、CollapsingToolbarLayout、ActionBar
 *        AppBarConfiguration 管理应用栏导航按钮的行为。导航按钮的行为会根据用户是否位于顶层目的地而变化。顶层Fragment不显示 返回箭头 ← 其他显示。如果顶层Fragment使用了 DrawerLayout，导航按钮会变为抽屉式导航栏图标。
 *                            同级屏幕存在导航关系 把导航图传递给构造函数 AppBarConfiguration(navController.graph)
 *                            如需将导航按钮配置为在所有目的地都显示为向上按钮，那么在构建您的 AppBarConfiguration 时，请为顶层目的地传递一组空白目的地 ID。AppBarConfiguration( setOf(), ::onSupportNavigateUp)
 *                            如果同级屏幕可能彼此之间并不存在层次关系，您可以改为将一组目的地 ID 传递给构造函数 AppBarConfiguration(setOf(R.id.main, R.id.profile))
 *        对CollapsingToolbarLayout应用栏的支持 collapsingToolbarLayout.setupWithNavController(toolbar, navController, appBarConfiguration)
 *        对Toolbar的支持 toolbar.setupWithNavController(NavController, appBarConfiguration) 把应用栏和导航关联起来
 *        对ActionBar的支持  AppCompatActivity.setupActionBarWithNavController(navController, appBarConfiguration)
 *        支持每个Fragment都有自己的应用栏 如果顶部应用栏在不同目的地之间有很大变化，请考虑从 activity 中移除顶部应用栏，并改为在每个目的地 fragment 中进行定义
 *                                      在Fragment的布局文件中添加 Toolbar ，然后调用  toolbar.setupWithNavController(navController, appBarConfiguration)把应用栏与导航关联起来
 *    9.2 Toolbar菜单关联导航
 *        通过onCreateOptionsMenu为Toolbar添加菜单，在菜单的点击回调事件中 onOptionsItemSelected {调用 menuItem.onNavDestinationSelected()} 代码在下面
 *    9.3 抽屉导航栏
 *        TODO
 *    9.4 底部导航栏BottomNavigationView
 *        menu中的id 是导航图中Fragment的id。使用 HideBottomViewOnScrollBehavior在滚动中消失
 *        [底部导航栏掘掘金文章](https://juejin.cn/post/6854573222156107783)
 *        [Fragment会在切换时重新创建](https://stackoverflow.com/questions/50485988/is-there-a-way-to-keep-fragment-alive-when-using-bottomnavigationview-with-new-n?r=SearchResults)
 *        [该问题的代码](https://github.com/STAR-ZERO/navigation-keep-fragment-sample)
 *    9.5 监听导航事件
 *        NavController 提供 OnDestinationChangedListener 接口，该接口在 NavController 的当前目的地或其参数发生更改时调用 代码在下面
 */
class NavigationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNavigationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
//        ScrollingFragmentDirections.actionScrollingFragmentToBlankFragment("11")
        setContentView(binding.root)

    }


    override fun onStart() {
        super.onStart()
        //1.3获取 controller
//        findNavController(R.id.nav_host_fragment)// 放在onCreate中会报错，是因为NavController 没有构建
        //
        findNavController(R.id.nav_host_fragment).apply {
            //9.1 应用栏
            val appBarConfiguration = AppBarConfiguration(this.graph)//按照导航图的顺序，顶层没有箭头，其它有返回箭头
//        val appBarConfiguration = AppBarConfiguration(setOf(R.id.main, R.id.profile))// 定义多个顶层Fragment，没有返回箭头
//        AppBarConfiguration(  topLevelDestinationIds = setOf(), fallbackOnNavigateUpListener = ::onSupportNavigateUp)// 所有层级都有返回按钮
            binding.toolbar.setupWithNavController(this, appBarConfiguration)
            //9.4 底部导航栏
            binding.bottomNavigation.setupWithNavController(this)//
            //9.5 监听导航事件
            addOnDestinationChangedListener { _, destination, arguments ->
                println("当前标签-----${destination.displayName}")
                println("arguments--------${arguments?.getString("name")}")
                //根据Fragment的切换 控制FragmentContainerView 之外的控件隐藏还是显示
//                if(destination.id == R.id.full_screen_destination) {
//                    toolbar.visibility = View.GONE
//                    bottomNavigationView.visibility = View.GONE
//                } else {
//                    toolbar.visibility = View.VISIBLE
//                    bottomNavigationView.visibility = View.VISIBLE
//                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    //9.2 菜单关联导航
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(
            findNavController(R.id.nav_host_fragment)
        ) || super.onOptionsItemSelected(item)
    }
    //Activity将它的 back键点击事件的委托出去，如果当前并非栈中顶部的Fragment, 那么点击back键，返回上一个Fragment。
    //app:defaultNavHost="true" 该属性貌似已经实现了这个功能
//    override fun onSupportNavigateUp(): Boolean {
//        findNavController(R.id.nav_host_fragment)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
}