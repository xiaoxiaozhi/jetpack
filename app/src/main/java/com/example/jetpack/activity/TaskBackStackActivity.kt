package com.example.jetpack.activity

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityTaskBackStackBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * https://developer.android.google.cn/guide/components/activities/tasks-and-back-stack?hl=en#Starting
 * 任务与Activity堆栈
 * 1. 当用户按back键从 launch Activity 返回到home。不同版本的android将采取不同的操作。
 *    android11 结束活动，android12及以上，类似用户按home键返回home，而不是销毁activity
 * 2. 当应用程序在多窗口环境中同时运行时，在Android7.0(API 24级)和更高版本中支持的情况下，系统分别管理每个窗口的任务；每个窗口可能有多个任务。
 * 3. 虽然活动是在新任务中开始的，但后退按钮和手势仍然会使用户返回到之前的活动。
 * 4. 应用程序中的某个活动在启动时开始一个新任务而不是放在当前任务中，使用manifest的<Activity>元素以及传递给startActivity()的标志来完成这些工作。
 *    4.1 <Activity>定义不同的启动模式
 *    在<Activity>中定义的启动模式 standard:一个任务中Activity有多个实例 singleTop:看onNewIntent的注释 。singleTask:看onNewIntent的注释
 *    singleTask:每次都创建新堆栈和新实例，堆栈中只能有这一个实例
 *    4.2 taskAffinity 亲缘关系
 *    亲缘关系表示活动倾向于属于哪个任务。默认情况下，来自同一应用程序的所有活动都有亲缘关系。因此，同一应用程序中的所有活动都希望处于同一任务中。
 *    但是，可以修改活动的默认关联。在不同应用程序中定义的活动可以共享亲缘关系，或者在同一应用程序中定义的活动可以分配不同的任务亲缘关系
 *    4.3 使用 Intent flags定义launch mode
 *    FLAG_ACTIVITY_NEW_TASK 如果启动的活动是APP自己的,因为亲缘关系则创建新实例不会创建新任务，不是自己的，会把后台任务移到前台
 *
 *    FLAG_ACTIVITY_NEW_TASK 和  FLAG_ACTIVITY_CLEAR_TOP  一起用的效果 等同于 singleTask
 *    FLAG_ACTIVITY_NEW_TASK 如果正在启动的活动
 *    FLAG_ACTIVITY_CLEAR_TOP，如果正在启动的活动已经存在，则销毁该活动之上的其他活动使它位于栈顶，startActivity传递的Intent会在onNewIntent()回调
 *    FLAG_ACTIVITY_SINGLE_TOP 行为同 singleTop
 *
 *
 */
class TaskBackStackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskBackStackBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBackStackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button1.setOnClickListener {
//            startActivity(
//                Intent().setClass(
//                    this@TaskBackStackActivity,
//                    SingleTopActivity::class.java
//                )
//            )
//            startActivity(Intent(this@TaskBackStackActivity, SingleTaskActivity::class.java))
//            startActivity(Intent(this@TaskBackStackActivity, SingleInstanceActivity::class.java))
            startActivity(Intent().apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                component = ComponentName.createRelative("com.example.myapplication","com.example.myapplication.MainActivity")
                setClass(this@TaskBackStackActivity, FlagNewTaskActivity::class.java)
            })
        }
        with(getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager) {
            println(
                "TaskBackStackActivity---id---${getRunningTasks(1).get(0)?.taskId}----top---${
                    getRunningTasks(
                        1
                    ).get(0).topActivity?.className
                }"
            )
            println(
                "TaskBackStackActivity---id---${getRunningTasks(1).get(0)?.taskId}----base--${
                    getRunningTasks(
                        1
                    ).get(0).baseActivity?.className
                }"
            )
        }
    }

    /**
     * 1.当Activity的启动模式设置为singleTop 的时候触发
     *   当活动A处于栈顶，再次startActivity 活动A，不会创建新的实例，而是复用本来存在的。活动A的生命周期
     *   onCreate--->onStart--->onResume---onPause--->onNewIntent--->onResume
     *   如果不在栈顶，则会创建实例
     * 2.singleTask
     *   如果您启动了指定SingleTask启动模式的活动是其它APP的，该活动的实例存在于后台任务中，则整个任务将被带到前台，并且该活动之上的活动将被清空，如果实例不存则创建新实例和新堆栈
     *   如果您启动了指定SingleTask启动模式的活动是自己APP的，该活动的实例存在于前台任务中，该活动将被提到栈顶，该活动之上的活动将被清空，该活动不存在实例则创建新实例，不创建新堆栈
     *   onCreate--->onStart--->onResume---onPause--->onStop--->onStart--->onNewIntent--->onResume
     *   https://developer.android.google.cn/images/fundamentals/diagram_backstack_singletask_multiactivity.png  图片更清晰
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("TaskBackStackActivity-----onNewIntent------")
    }
}