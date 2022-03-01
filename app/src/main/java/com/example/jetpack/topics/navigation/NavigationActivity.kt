package com.example.jetpack.topics.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityNavigationBinding

/**
 * 1. 导航组件由以下三个关键部分组成：
 *    1.1 导航图，包含所有导航相关信息的 XML 资源
 *        创建导航图右键点击 res 目录，然后依次选择 New > Android Resource File。在 File name 字段中输入名称，例如“nav_graph”。从 Resource type 下拉列表中选择 Navigation，然后点击 OK。
 *    1.2 目的容器，NavHost
 *        向Activity添加NavHost,查看布局文件R.layout.activity_navigation。android:name 属性实现 NavHost的类名称；app:defaultNavHost="true" 表示NavHostFragment 会拦截系统返回按钮
 *        获取NavHost三种方式
 *        Fragment.findNavController()    View.findNavController()    Activity.findNavController(viewId: Int)
 *    1.3 导航控制器，NavController 在容器中管理导航对象
 * 2. 添加Navigation 支持
 *    查看代码app/build.gradle
 * 3. 使用safe Args 传递数据 TODO 不太明白怎么使用
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
 *     8.1 使用Safe Args，该插件会为目的地自动生成一个 目的地名+Directions 的类，其中只有一个方法 会返回NavDirections的派生类
 *         最后调用findNavController().navigate(NavDirections)实现导航
 *     8.2 通过actionID导航 view.findNavController().navigate(R.id.viewTransactionsAction)
 *     8.3 Navigation.createNavigateOnClickListener(actionId，Bundle)//返回一个点击事件，点击按钮导航时用到
 *     8.4 为action提供导航配置。action标签编译后会得到NavAction类 ，其中包括，目的地ID、传递数据(Fragment下的argument标签)、以及NavOption类(就是action标签中的app属性)
 *         动画属性解读：从A->B ：enterAnim是B的入场动画 、exitAnim是A的出场动画、然后按back返回，popEnterAnim是A的入场动画、popExitAnim是B的出场动画
 *         例如A、B、C三个目的地，从A--->B--->C这时候堆栈中有ABC三个目的地。这时候再从C--->A C的action设置为 app:popUpTo="A" 导航前弹出B和C。
 *         再设置app:popUpToInclusive="true" 让之前的A也弹出。最后只剩下C。实现了A->B->C->A 的循环一轮循环结束后所有目的地都弹出。第二轮开始时堆栈只有A
 *     8.5 DeepLinkRequest导航 TODO 待看
 *         深度跳转，从web也启动应用
 *     8.6 条件导航，用SavedStateHandle保存信息，即使回收也不会去丢失，根据保存的信息来判断导航到哪个目的地 TODO 例子待实现
 * 9. 在目的地之间传递参数
 *
 *
 */
class NavigationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNavigationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
//        ScrollingFragmentDirections.actionScrollingFragmentToBlankFragment()
        setContentView(binding.root)

    }
}