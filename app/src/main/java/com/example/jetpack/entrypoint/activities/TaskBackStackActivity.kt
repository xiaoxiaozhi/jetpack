package com.example.jetpack.entrypoint.activities

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.jetpack.databinding.ActivityTaskBackStackBinding

/**
 * 在 Android 11 下测试
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
 *    亲缘关系表示活动倾向于属于哪个任务。默认情况下，来自同一应用程序的所有活动都有亲缘关系(包名)。因此，同一应用程序中的所有活动都希望处于同一任务中。
 *    但是，可以修改活动的默认关联。在不同应用程序中定义的活动可以共享亲缘关系，或者在同一应用程序中定义的活动可以分配不同的任务亲缘关系
 *    4.3 使用 Intent flags定义launch mode
 *    一个app中可以有不同的亲缘关系，
 *    FLAG_ACTIVITY_NEW_TASK 通常 和 taskAffinity属性配合使用，
 *    FLAG_ACTIVITY_NEW_TASK和taskAffinity(默认值)创建新实例 、不创建新任务
 *    FLAG_ACTIVITY_NEW_TASK和taskAffinity(非默认值)创建新实例、创建新任务，如果任务存在则创建新实例加入到这个任务，
 *    如果实例已经存在任务中，则将任务提到前台(此时实例不一定在栈顶)，不调用onNewIntent。非默认taskAffinity的任务栈(A B C)从B或者C无法返回根活动A
 *    FLAG_ACTIVITY_NEW_TASK 和 FLAG_ACTIVITY_CLEAR_TOP  一起用实际测试发现栈顶的活动被清除，没有复用已经创建的实例，而是重新创建实例。不等同于 singleTask
 *    FLAG_ACTIVITY_SINGLE_TOP 行为同 singleTop
 *    4.4 跳转到其它APP的Activity
 *    被跳转的Activity要增加<action android:name="android.intent.action.MAIN" /> 否则会报错
 *
 *    通过Intent 包含FLAG_ACTIVITY_NEW_TASK标志 这种方式跳转到第三方APP的Activity，相当于把第三方的任务栈叠加在自己任务栈的上层，
 *    切换界面能明显看出任务栈切换动画，实际结果之上已经测试过
 *
 *    通过设置被调用Activity 的android:allowTaskReparenting="true"属性，如果第三方APP任务栈存在，任务切换界面流畅，（如果不存活则会报错，想不报错的话加FLAG_ACTIVITY_NEW_TASK标志但是这样切换不流畅）
 *    而且不会把整个第三方任务栈叠加在自己APP上。当回到桌面或者点击小方块。第三方Activity会回到自己的任务栈中（该功能在android9、10中实现不了）
 *
 *5. 任务栈相关属性
 *   5.1 alwaysRetainTaskState 设置在跟Activity 不管用户离开多久，任务栈都会得以保存，不会重启
 *   5.2 clearTaskOnLaunch 与 5.1 相反即使是马上离开又返回，任务栈都会从根Activity(不重置)开始
 *   5.3 finishOnTaskLaunch 在根活动的其它活动设置，效果类似5.2，但只对单个Activity起作用，马上离开再进入Activity也会被销毁
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
        Intent().extras
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
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
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
     * 2.singleTask  扔物线： 例如邮件APP有一个Activity作用是编辑邮件，其它APP有需求就调用这个Activity，之后这个Activity就存在于调用者APP的栈而与邮件APP栈无关系？？？测试发现结果同2.1
     *   2.1如果您启动了指定SingleTask启动模式的活动是其它APP的，该活动的实例存在于后台任务中，则整个任务将被带到前台，并且该活动之上的活动将被清空，如果实例不存则创建新实例和新堆栈
     *   2.2如果您启动了指定SingleTask启动模式的活动是自己APP的，该活动的实例存在于前台任务中，该活动将被提到栈顶，该活动之上的活动将被清空，该活动不存在实例则创建新实例，不创建新堆栈
     *   onCreate--->onStart--->onResume---onPause--->onStop--->onStart--->onNewIntent--->onResume
     *   https://developer.android.google.cn/images/fundamentals/diagram_backstack_singletask_multiactivity.png  图片更清晰
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("TaskBackStackActivity-----onNewIntent------")
    }
}