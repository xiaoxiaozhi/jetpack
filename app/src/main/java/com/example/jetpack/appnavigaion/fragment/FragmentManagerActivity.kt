package com.example.jetpack.appnavigaion.fragment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityFragmentManagerBinding

/**
 * https://developer.android.google.cn/guide/fragments/fragmentmanager
 * 1. FragmentManager负责处理Fragment 包括添加、删除、替换
 * https://blog.csdn.net/cqkxzsxy/article/details/78475784 每个操作看这里
 * 2. 如何访问FragmentManager ?
 * 3. FragmentManager在活动和片段中扮演什么角色?
 * 4. 使用FragmentManager管理后台堆栈以及向片段提供数据和依赖关系
 * --------在Activity中和Fragment引用FragmentManager--------
 * 1. 碎片也能管理碎片，通过getChildFragmentManager()获得对FragmentManager的引用。如果您需要父碎片的的FragmentManager，可以使用getParentFragmentManager()。
 * 2.  在 FragmentActivity及其子类中通过  getSupportFragmentManager()  获取FragmentManager引用
 * 3. 示例图片https://developer.android.google.cn/images/guide/fragments/manager-mappings.png
 * ------------------使用FragmentManager--------------------
 * 1. 当用户按下设备上的后退按钮或调用FragmentManager.popBackStack()时，
 *    最上面的片段事务就会从堆栈中弹出。如果堆栈上没有更多的片段事务，那么就会调用Activity中的back。
 * 2.
 * -----------------执行事务FragmentTransaction------------------
 * 1. 添加Fragment到Activity
 * 2. 查找Fragment
 * -----------------一个屏幕显示多个同级碎片---------------------------
 * 1. 当同时显示两个或多个片段时，其中只有一个可以是主导航片段。将片段设置为主导航片段将从前一个片段中移除指定。
 *    使用前面的示例，如果将详细片段设置为主导航片段，则删除主片段的指定。
 * 2. supportFragmentManager.primaryNavigationFragment 设置主导航碎片
 * ------------------支持多个堆栈--------------------------------------(没看懂)
 * 1. SaveBackStack()的工作方式类似于使用可选的name参数调用popBackStack()：
 *    指定的事务以及堆栈上的所有事务都会弹出。不同之处在于，SaveBackStack()保存了弹出事务中所有片段的状态。
 * 2. 您可以使用相同的名称参数调用RESToreBackStack()来恢复所有弹出事务和所有保存的片段状态：
 * ------------------Activity销毁重加载、FragmentFactory----------------------------
 * 1. 稍后在看
 *
 */
class FragmentManagerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFragmentManagerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFragmentManagerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //----------执行事务-----------
        //1. 添加Fragment到Activity
        supportFragmentManager.commit {//第一种添加方式,通过泛型方式
//            add<LifeCycleFragment>(R.id.fragmentContainer)//强烈建议使用泛型形式，而不是通过实例添加到Activity
            setReorderingAllowed(true)//删除冗余操作，例如在提交前，两个事务一起执行，一个添加片段a，另一个替换为片段B，则操作将取消，只添加片段B，这意味着片段A可能不会经历创建/销毁生命周期
            addToBackStack("ManagerFragment") //调用addToBackStack()将事务提交给后台堆栈。
            // 用户稍后可以通过按Back按钮来反转事务并带回前一个片段。如果不设置这个方法，按返回键直接退出Activity
        }
        supportFragmentManager.commit {//第二种添加方式,通过实例
            // Instantiate a new instance before adding
            val myFragment = ManagerFragment1()
            replace(R.id.fragmentContainer, myFragment)//
            setReorderingAllowed(true)//看不出来这是干嘛的，实验结果跟文档解释不同
            addToBackStack("ManagerFragment1")
        }
        supportFragmentManager.commit {
            replace<ManagerFragment2>(R.id.fragmentContainer, "Fragment2")//replace = remove所有 + add
            setReorderingAllowed(true)
            addToBackStack("ManagerFragment2")
        }

        binding.button1.setOnClickListener {
            //1.其中name是addToBackStack(String tag)中的tag，回滚到这一层，
            //2.flags有两个取值：0或FragmentManager.POP_BACK_STACK_INCLUSIVE。当取值0时，
            // 表示除了参数指定这一层之上的所有层都退出栈，指定的这一层为栈顶层；
            // 当取值POP_BACK_STACK_INCLUSIVE时，表示连着参数指定的这一层一起退出栈。
            supportFragmentManager.popBackStack("ManagerFragment", 0)//
        }
        binding.button2.setOnClickListener {
            supportFragmentManager.popBackStack("ManagerFragment1", 0)
        }
        binding.button3.setOnClickListener {
//            supportFragmentManager.findFragmentByTag("Fragment2")?.let {
//                supportFragmentManager.beginTransaction().detach(it).remove(it).commit()
//            }//效果同下列代码
            supportFragmentManager.findFragmentById(R.id.fragmentContainer)?.let {
                supportFragmentManager.beginTransaction().detach(it).remove(it).commit()
            }//findFragmentById 静态xml里面的fragment标签id，如果传容器id将返回当前的Fragment
        }

        //2. 查找Fragment
        supportFragmentManager.findFragmentById(R.id.fragmentContainer) //查找当前碎片
        supportFragmentManager.findFragmentByTag("ManagerFragment")//查找指定碎片
        //3. 支持多个堆栈
//        supportFragmentManager.saveBackStack()
    }
}