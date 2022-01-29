package com.example.jetpack.architecturecomponent.datalibs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.work.*
import com.example.jetpack.R

/**
 *https://mp.weixin.qq.com/s/MPZD9LbSbJYx1BLAolIbIg WorkManager 流程分析和源码解析
 * WorkManager 统一了对于 Android 后台任务的管理。
 * -----特点-----
 *    1. 保证任务一定会被执行
 *    WorkManager 有自己的数据库，每一个任务的信息与任务状态，都会保存在本地数据库中。
 *    所以即使程序没有在运行，或者在设备重启等情况下，WorkManager 依然可以保证任务的执行，只是不保证任务立即被执行。
 *    2. 合理使用设备资源
 *    在执行很多周期性或非立即执行的任务时，WorkManager 提供我们 API，帮助我们合理利用设备资源，避免不必要的内存，流量，电量等消耗。
 *    3. 带条件执行
 *-----适用场景-----
 *    1. 可延迟进行的任务
 *       a.满足某些条件才执行的任务，如需要在充电时才执行的任务。
 *       b.用户无感知或可延迟感知的任务，如同步配置信息，同步资源，同步通讯录等。
 *    2. 定期重复性任务，但时效性要求不高的，如定期 log 上传，数据备份等。(PeriodicWorkRequest and Worker.)
 *    3. 退出应用后还应继续执行的未完成任务。
 *    4. 必须立即开始并很快完成的任务 (OneTimeWorkRequest and Worker. )
 *    5. 长时间运行的任务，(超过10分钟  WorkRequest or Worker)
 *-----使用方法------
 *    1.通过继承Worker(appContext, workerParams)的方式定义Worker
 *    2.使用 WorkRequest 调用Worker
 *    3.加急工作
 *      特点：对用户重要、几分钟之内就可以完成的工作、受系统配额影响，不足时加急工作就不能顺利执行、电池管理策略以及Doze模式不会加急工作执行、那些不允许延迟的工作不适合加急工作
 *      备注：当您的应用程序处于前台时，配额不会限制快速工作的执行。执行时间配额只适用于您的应用程序在后台，或者当您的应用程序移动到后台时，
 *    4.
 */
class WorkManagerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_manager)
//        //1. 一次执行，使用OneTimeWorkRequestBuilder执行Worker
//        val uploadWorkRequest: WorkRequest =
//            OneTimeWorkRequestBuilder<UploadWorker>().build()//build() 之前增加配置条件
////        val myWorkRequest = OneTimeWorkRequest.from(MyWork::class.java) //对于不想额外配置直接使用from
//        WorkManager.getInstance(this).enqueue(uploadWorkRequest)
//        //2. 自定义WorkRequest

        //1. 快速执行
        val request = OneTimeWorkRequestBuilder<UploadWorker>()
            //从WorkManager2.7开始，应用程序可以调用setExpedited()来声明WorkRequest使用快速作业尽快运行
            //不过在 Android12之前的版本上运行，它使用前台服务而不是快速工作，同时会报错。为了应对这种情况需要在Worker中实现
            //getForegroundInfoAsync() 或 getForegroundInfo()
//            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(this).enqueue(request)
    }

}
