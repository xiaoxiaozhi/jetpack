package com.example.jetpack.architecturecomponent.datalibs.work

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.work.*
import com.example.jetpack.R

/**
 *https://mp.weixin.qq.com/s/MPZD9LbSbJYx1BLAolIbIg WorkManager 流程分析和源码解析
 * WorkManager 统一了对于 Android 后台任务的管理。
 * -----特点-----
 *    1. 带条件执行
 *    2. 强大的调度功能 WorkManager 有自己的数据库，每一个任务的信息与任务状态，都会保存在本地数据库中。
 *       所以即使程序没有在运行，或者在设备重启等情况下都会重新执行，并且能自适应系统省电策略列入Doze。TODO Doze 学习这个模式
 *    3. 快速工作：适合那些有用户发起需要快速完成的任务，受系统配额的限制（只有app在后台时才会收到限制），电池管理策略不会影响快速任务的执行
 *    4. 灵活的重试策略
 *    5. 链式调用
 *    6. 无缝接入RxJava与kotlin 协程
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
        //---------------------简单应用---------------------------
        //1. 定义Worker，具体代码看 UploadWorker 类
        //2. 创建WorkRequest 调用Work
        val uploadWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<UploadWorker>().build()
        //3. 向系统提 交WorkRequest
        WorkManager.getInstance(this).enqueue(uploadWorkRequest)
        //--------------------定义WorkRequests详解-----------------
        //1. 调度一次性工作
        OneTimeWorkRequest.from(UploadWorker::class.java)//没有配置
        OneTimeWorkRequestBuilder<UploadWorker>()
            // Additional configuration
            .build()//有配置使用这个方法
        //2. 快速工作
        //WorkManager 2.7.0引入了加速工作的概念。协调系统资源，更快执行重要的工作。
        OneTimeWorkRequestBuilder<UploadWorker>().setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            //RUN_AS_NON_EXPEDITED_WORK_REQUEST   当APP没有块任务配额，快速任务将按照正常任务执行
            //DROP_WORK_REQUEST                   当APP没有快速任务配额任务将会取消
            .build()
            .apply {
                WorkManager.getInstance(this@WorkManagerActivity).enqueue(this)
            }
        //2.1


    }

}
