package com.example.jetpack.appnavigaion.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.jetpack.R
import com.example.jetpack.databinding.FragmentScrollingBinding

/**
 * 1. 跳转到目的地
 *    1.1 使用Safe Args，该插件会为目的地自动生成一个 目的地名+Directions 的类，其中只有一个方法 会返回NavDirections的派生类
 *        最后调用findNavController().navigate(NavDirections)实现导航
 *    1.2 通过actionID导航 view.findNavController().navigate(R.id.viewTransactionsAction)
 *    1.3 Navigation.createNavigateOnClickListener(fragmentId，Bundle)//返回一个点击事件，点击按钮导航时用到
 *    1.4 为action提供导航配置。action标签编译后会得到NavAction类 ，其中包括，目的地ID、传递数据(Fragment下的argument标签)、以及NavOption类(就是action标签中的app属性)
 *         动画属性解读：从A->B ：enterAnim是B的入场动画 、exitAnim是A的出场动画、然后按back返回，popEnterAnim是A的入场动画、popExitAnim是B的出场动画
 *         例如A、B、C三个目的地，从A--->B--->C这时候堆栈中有ABC三个目的地。这时候再从C--->A C的action设置为 app:popUpTo="A" 导航前弹出B和C。
 *         再设置app:popUpToInclusive="true" 让之前的A也弹出。实现了A->B->C->A 的循环一轮循环结束后所有目的地都弹出。第二轮开始时堆栈只有A
 *     1.5 隐式深层链接DeepLinkRequest导航 查看代码 DeepLinkFragment
 *         深度跳转，从web页启动应用
 *         1.5.1 第一步在导航图中为目的地添加 deepLink标签<deepLink app:uri="http://YourWebsit/{params}"/>
 *         1.5.2 第二步在AndroidManifest.xml找到导航图所在的Activity添加<nav-graph android:value="@navigation/nav_graph"/>
 *         1.5.3 使用adb命令测试 adb shell am start -a android.intent.action.VIEW -d "http://YourWebsite/get_data.xml"
 *         1.5.4 在目的地Fragment中使用 arguments?.getString("params") 接收
 *     1.6 显示深层链接
 *         当你的应用程序收到某个通知推送，你希望用户在点击该通知时，能够直接跳转到展示该通知内容的页面，那么就可以通过PendingIntent来完成此操作。
 *         当用户通过显式深层链接打开您的应用时，原有的任务堆栈会被清除，并被替换为相应的深层链接目的地。起始目的地，也会添加到相应堆栈中，返回按钮时，他们会返回到相应的导航堆栈，就像从入口点进入您的应用一样。TODO 并没有实现这个效果
 *         [当Activity的启动模式不是standard，手动调用 navController.handleDeepLink(intent)](https://developer.android.google.cn/guide/navigation/navigation-deep-link#handle)
 *     1.7 NavController.navigateUp()和 NavController.popBackStack() 返回上一个目的地，例如A->B ;前者还保存有A，后者是出栈的形式A 被销毁
 *         代码查看 BlankFragment.kt
 *     1.8 条件导航(感觉值得关注的是回传) 查看代码ProfileFragment和LoginFragment
 *         使用场景：在ProfileFragment中查看用户状态，前提是已经登录，如果没有登录则跳转到LoginFragment登录，
 *         在LoginFragment确定登录状态后设置值同时 ProfileFragment通过LiveData观察数据，同步得到状态
 *
 * 2. 在目的地之间传递参数
 *    findNavController导航组件可以通过 Bundles 传递数据，但是我们建议使用 SafeArgs
 *    2.1 定义目的地参数，在导航图的<Fragment>标签中 添加<argument>标签 [支持的参数类型](https://developer.android.google.cn/guide/navigation/navigation-pass-data#supported_argument_types)
 *        需要数据的目标界面是对话框 BlankFragment，它需要知道所需显示的对象的信息。
 *    2.2 接收目的参数：在 BlankFragment使用 val arg: BlankFragmentArgs by navArgs<BlankFragmentArgs>()
 * 3. Fragment共享元素过渡
 *     navigate之前先创建val extras = FragmentNavigatorExtras(binding.img to "hero_image")
 *    在接收端    sharedElementEnterTransition = MaterialContainerTransform(requireContext(), true) TODO 试着用Slide()发现没用，为什么一定要这个呢
 *
 *
 *
 */

class ScrollingFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentScrollingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("${this.javaClass.simpleName}-------onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("${this.javaClass.simpleName}-------onCreateView")
        binding = FragmentScrollingBinding.inflate(inflater, container, false)
        //1.1
        binding.button1.setOnClickListener {
            //2.1
            val action = ScrollingFragmentDirections.actionScrollingFragmentToBlankFragment("joker")
            findNavController().navigate(action)
        }
        binding.button2.setOnClickListener(this)
        //1.3
        binding.button3.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.blankFragment))
        binding.button4.setOnClickListener(this)
        binding.button5.setOnClickListener(this)
        binding.button6.setOnClickListener(this)
        binding.button7.setOnClickListener(this)
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            //1.2
            R.id.button2 -> findNavController().navigate(R.id.action_scrollingFragment_to_blankFragment)
            //1.4
            R.id.button4 -> {
                val avOptions = NavOptions.Builder()
                    //从A->B ：
                    .setEnterAnim(R.anim.enter)//enterAnim是B的入场动画
                    .setExitAnim(R.anim.exit)//exitAnim是A的出场动画
                    //按返回键
                    .setPopEnterAnim(R.anim.pop_enter)//A入场动画
                    .setPopExitAnim(R.anim.pop_exit)//B出场动画
//                  .setPopUpTo()                  //该属性在BlankFragment展示
                    .build()
                val bundle = Bundle().apply {
                    putString("arg", "来自scrollingFragment")
                }
                findNavController().navigate(
                    R.id.action_scrollingFragment_to_blankFragment,
                    bundle,
                    avOptions,
                )
            }
            R.id.button5 -> findNavController().navigate(R.id.profileFragment)
            R.id.button6 -> deepLinkBuild()
            R.id.button7 -> {
                val extras = FragmentNavigatorExtras(binding.img to "hero_image")
                findNavController().navigate(
                    R.id.action_scrollingFragment_to_blankFragment,
                    null, // Bundle of args
                    null, // NavOptions
                    extras
                )
                //导航到Activity 共享元素
//                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
//                   Pair(view1, "hero_image"))
//                val extras = ActivityNavigatorExtras(options)
            }

        }
    }

    fun deepLinkBuild() {
        activity?.let {
            val pendingIntent =
                NavDeepLinkBuilder(it)
//                findNavController().createDeepLink() //也可以通过NAVController创建NavDeepLinkBuilder
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.thirdFragment)
                    .setArguments(null)
//                .setComponentName(DestinationActivity::class.java)//默认情况下 会将显式深层链接启动到应用清单中声明的默认启动 Activity。如果您的 NavHost 在其他 activity 中，则您必须在创建深层链接建立工具时指定其组件名称：
                    .createPendingIntent()
            Notifier.postNotification(it, pendingIntent)
        }
    }
}