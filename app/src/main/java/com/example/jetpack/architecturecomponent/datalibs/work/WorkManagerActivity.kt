package com.example.jetpack.architecturecomponent.datalibs.work

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.work.*
import com.example.jetpack.R
import java.util.concurrent.TimeUnit

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
        val uploadWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<UploadWorker>()
                .build()
        //3. 向系统提 交WorkRequest
//        WorkManager.getInstance(this).enqueue(uploadWorkRequest)
        //--------------------定义WorkRequests详解-----------------
        //1. 调度一次性工作
        OneTimeWorkRequest.from(UploadWorker::class.java)//没有配置
        OneTimeWorkRequestBuilder<UploadWorker>()
            // Additional configuration
            .build()//有配置使用这个方法
        //2. 快速工作
        //WorkManager 2.7.0引入了加速工作的概念。协调系统资源，更快执行重要的工作。
        OneTimeWorkRequestBuilder<ExpeditedCoroutineWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            //RUN_AS_NON_EXPEDITED_WORK_REQUEST   当APP没有块任务配额，快速任务将按照正常任务执行
            //DROP_WORK_REQUEST                   当APP没有快速任务配额任务将会取消
            .build().apply {
//                WorkManager.getInstance(this@WorkManagerActivity).enqueue(this)
            }
//
        //2.1 向前兼容
        //低于 Android12(API 31)的设备执行快速服务的时候都会开启前台服务，并且必须实现getForegroundInfo或者getForegroundInfoAsync。
        //在这两个方法内会实现一个通知，具体代码参考ExpeditedCoroutineWorker和 UploadWorker。服务结束后通知也会消失。
        //如果不实现这两个方法将会报错(即时不设置setExpedited 如果不实现这两个方法也有一定概率报错)

        //3. 调度周期工作
        println("----定期调度工作----")//TODO WorkManager 开机重启也会继续执行，我怎么终止呢？
        val saveRequest = PeriodicWorkRequestBuilder<UploadWorker>(
            15, TimeUnit.MINUTES,//最小间隔15分钟
            14, TimeUnit.MINUTES
        )//弹性间隔的位置总是在周期的末尾，比如本例15分钟的周期，弹性间隔14分钟，将在一分钟之后执行任务
            //
            .addTag("tag")
            .build()
        //弹性间隔最小时间5分钟PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS，重复间隔最小时间15分钟 PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
        WorkManager.getInstance(this).enqueue(saveRequest)//FIXME 例子实现效果不是重复间隔15分钟弹性间隔14分钟
        //3.1 约束条件对PeriodicWorkRequest 的影响
        //没有达到约束条件，任务将跳过这个周期，直到符合约束
        Constraints.Builder()//约束条件
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)//UNMETERED 好像是wifi  METERED 好像是移动网略
            .setRequiresBatteryNotLow(true)//设备处于低电量模式的时候 任务将不会被运行
            .setRequiresCharging(true)// 设备充电的时候 任务才能运行
//            .setRequiresDeviceIdle(true)//Android 6+ 设备处于限制状态，任务才能运行
            .setRequiresStorageNotLow(true)// 设备存储空间足够的时候才能运行任务
            .build()
            .apply {
                OneTimeWorkRequestBuilder<UploadWorker>().setConstraints(this).build()//设置约束条件
            }
        //4. 延迟工作
        OneTimeWorkRequestBuilder<UploadWorker>().setInitialDelay(10, TimeUnit.MINUTES).build()
        //也可以为 PeriodicWorkRequest 设置延迟时间，不过只在第一次起作用
        //5. 重试策略 TODO 不设置重试策略 Work返回Result.retry() 还会重试吗
        //如果Work返回 Result.retry()，系统将根据重试策略重新调度工作。
        OneTimeWorkRequestBuilder<UploadWorker>().setBackoffCriteria(
            BackoffPolicy.LINEAR,//LINEAR第一次重试间隔10S 第二次间隔20S，第三次30S以此类推。EXPONENTIAL 第一次10S 第二次20S 第三次40S 第四次80S
            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,//10S
            TimeUnit.MILLISECONDS
        ).build()//重试间隔 是不精确的会有几秒误差，但不会小于第二个参数设置的时间
        //6. 标记 Work
        //每个工作请求都有一个唯一标识符，该标识符可用于在以后标识该工作，以便取消工作或观察其进度。
        println("----标记Work----")
        WorkManager.getInstance(this).cancelAllWorkByTag("tag1")//取消这个标记的工作
        WorkManager.getInstance(this).getWorkInfosByTag("tag").get().forEach {
            println("Work:--- ${it.state.name}")
        }
        //7. 输入数据
        OneTimeWorkRequestBuilder<UploadWorker>().setInputData(workDataOf("IMAGE_URI" to "http://..."))
            .build()
        //读取数据参见 UploadWorker
        //8. Work 状态
        //Work被提交给系统最初是ENQUEUED(入队)状态，等延迟或者约束条件满足后转到running状态，这两种状态随时都可以被取消进入cancelled状态
        //然后根据执行结果转移到 Success或者Failed状态 。
        //Success、Failed、Cancelled状态都属于撞断状态，WorkInfo.State.isFinished() returns true.
        //![图片](https://developer.android.google.cn/images/topic/libraries/architecture/workmanager/how-to/one-time-work-flow.png)

        //9. 设置Work唯一性
        //Work可能会被提交给系统好几次为避免这种情况，调用 WorkManager.getInstance(this).enqueueUniquePeriodicWork或者
        //WorkManager.getInstance(this).enqueueUniqueWork()

        val sendLogsWorkRequest =
            PeriodicWorkRequestBuilder<UploadWorker>(24, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresCharging(true)
                        .build()
                )
                .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sendLogs",      //用于唯一标识工作请求的
            ExistingPeriodicWorkPolicy.KEEP, //如果已有使用该名称且尚未完成的唯一工作链，应执行什么操作(KEEP 继续之前的工作，忽略新工作 REPLACE 取消之前的工作，并用新工作替换)
            sendLogsWorkRequest              //要调度的 WorkRequest
        )
        //ExistingWorkPolicy 对于一次性工作解决冲突的策略有四种。
        //同ExistingPeriodicWorkPolicy.KEEP 和 ExistingPeriodicWorkPolicy.REPLACE
        ExistingWorkPolicy.APPEND//新工作连接到现有工作，现有工作变为 CANCELLED 或 FAILED 状态，新工作也会变为 CANCELLED 或 FAILED。如果您希望无论现有工作的状态如何都运行新工作，请改用 APPEND_OR_REPLACE
        ExistingWorkPolicy.APPEND_OR_REPLACE//


// by id
        WorkManager.getInstance(this)
            .getWorkInfoById(sendLogsWorkRequest.id) // ListenableFuture<WorkInfo>

// by name
        WorkManager.getInstance(this)
            .getWorkInfosForUniqueWork("sync") // ListenableFuture<List<WorkInfo>>

// by tag
        WorkManager.getInstance(this)
            .getWorkInfosByTag("syncTag") // ListenableFuture<List<WorkInfo>>

    }

}
