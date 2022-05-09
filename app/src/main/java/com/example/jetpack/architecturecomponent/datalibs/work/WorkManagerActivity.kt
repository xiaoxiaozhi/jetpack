package com.example.jetpack.architecturecomponent.datalibs.work

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.work.*
import com.example.jetpack.databinding.ActivityWorkManagerBinding
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

/**
 *https://mp.weixin.qq.com/s/MPZD9LbSbJYx1BLAolIbIg WorkManager 流程分析和源码解析
 * WorkManager 统一了对于 Android 后台任务的管理。
 * WorkManager强大的调度能力，已调度的工作存储在内部托管的 SQLite 数据库中，由 WorkManager 负责确保该工作持续进行，并在设备重新启动后重新调度。
 * 例如app启动一个工作，然后用kill+包名 杀死进城后 几秒之后 app会重新启动并执行工作。(工作链也是这样)
 * WorkManager 是适合用于持久性工作的推荐解决方案。如果工作始终要通过应用重启和系统重新启动来调度，便是永久性的工作。由于大多数后台处理操作都是通过持久性工作完成的，
 * 因此 WorkManager 是适用于后台处理操作的主要推荐 API。
 * 1. WorkerManager类型
 *   1.1 立即执行：必须立即开始且很快就完成的任务，可以加急。
 *       使用OneTimeWorkRequest，如需处理加急工作，请对 OneTimeWorkRequest 调用 setExpedited()
 *       加急工作具有以下特征：
 *       重要性：加急工作适用于对用户很重要或由用户启动的任务。速度：加急工作最适合那些立即启动并在几分钟内完成的简短任务。配额：限制前台执行时间的系统级配额决定了加急作业是否可以启动。电源管理：电源管理限制（如省电模式和低电耗模式）不太可能影响加急工作。
 *       延迟时间：系统立即执行加急工作，前提是系统的当前工作负载允许执行此操作。这意味着这些工作对延迟时间较为敏感，不能安排到以后执行。
 *       note：TODO 应用待机 [待机模式存储分区](https://developer.android.google.cn/topic/performance/appstandby)
 *       1.1.1 向后兼容
 *            Android 12 中引入了新的前台服务限制，当应用在后台时是无法启动前台服务的。因此从 Android 12 开始，调用 setForegroundAsync 方法会抛出 Foreground Service Start Not Allowed Exception (不允许启动前台服务) 异常
 *       note：不设置 setExpedited 则不需要覆写 getForegroundInfo 或者 getForegroundInfoAsync
 *   1.2 长时间运行：运行时间可能较长（有可能超过 10 分钟）的任务。
 *       任意 WorkRequest 或 Worker。在 doWork()中调用 setForeground() 来处理通知。长时间工作的关键点在于调用setForeground
 *   1.3 可延期执行：延期开始并且可以定期运行的预定任务。
 *       APP退出后仍然可以执行
 *       PeriodicWorkRequest 和 Worker。可以定义的最短重复间隔是 15 分钟
 *       repeatInterval    重复周期最短15分钟
 *       flexTimeInterval  弹性间隔最短5分钟  任务开始时间 = repeatInterval - flexTimeInterval  具体看下图
 *       ![](https://developer.android.google.cn/images/topic/libraries/architecture/workmanager/how-to/definework-flex-period.png)
 * 2. 工作约束
 *    如果是周期性任务，没有达到约束条件时，任务将跳过这个周期，直到符合约束
 *    如果在工作运行时不再满足某个约束，WorkManager 将停止工作器。系统将在满足所有约束后重试工作。
 * 3. 延迟工作
 *    可以将工作指定为在经过一段最短初始延迟时间后再启动(约束任务会在达到约束条件后延期执行)
 *     OneTimeWorkRequestBuilder<MyWork>().setInitialDelay(10, TimeUnit.MINUTES)
 * 4. 重试和退避策略
 *    工作器返回 Result.retry() 时用到的策略 重试工作前的最短等待时间是10S
 * 5. 标记工作
 *    该标识符可用于在以后标识该工作，以便取消工作或观察其进度。
 *    例如，WorkManager.cancelAllWorkByTag(String) 会取消带有特定标记的所有工作请求，WorkManager.getWorkInfosByTag(String) 会返回一个 WorkInfo 对象列表
 *    可以向单个工作请求添加多个标记。这些标记在内部以一组字符串的形式进行存储。您可以使用 WorkInfo.getTags() 获取与 WorkRequest 关联的标记集。
 * 6. 输入数据
 *    初始化时向Worker传递数据  OneTimeWorkRequestBuilder<UploadWork>().setInputData(workDataOf("IMAGE_URI" to "http://..."))
 *    在Worker内部 使用   inputData.getString("IMAGE_URI") 获取
 * 7. 工作状态
 *    7.1 一次性工作状态，
 *        工作的初始状态为 ENQUEUED。您的工作会在满足其 Constraints 和初始延迟计时要求后立即运行。接下来工作会转为RUNNING 状态，然后可能会根据工作的结果转为 SUCCEEDED、FAILED 状态；或者，如果结果是 retry，它可能会回到 ENQUEUED 状态。在此过程中，随时都可以取消工作，取消后工作将进入 CANCELLED 状态。
 *        SUCCEEDED、FAILED 和 CANCELLED 均表示此工作的终止状态。如果您的工作处于上述任何状态，WorkInfo.State.isFinished() 都将返回 true。
 *        ![查看图片](https://developer.android.google.cn/images/topic/libraries/architecture/workmanager/how-to/one-time-work-flow.png)
 *
 *        初始状态ENQUEUED-----满足约束或延时----->RUNNING----工作有结果---> SUCCEEDED或者FAILED
 *                                                                  如果retry------>ENQUEUED
 *       以上整个过程随时可以取消 取消后进入CANCELLED 状态
 *    7.2 定期工作状态
 *         ENQUEUED RUNNING CANCELLED 没有成功和失败状态
 *        ![定期工作状态图](https://developer.android.google.cn/images/topic/libraries/architecture/workmanager/how-to/periodic-work-states.png)
 *    7.3 BLOCKED 状态
 *        查看11.3
 * 8.管理工作
 *   8.1 唯一工作
 *       在将工作加入队列时请小心谨慎，以避免重复，即使作业只需运行一次，您最终也可能会多次将同一作业加入队列。为了实现此目标，您可以将工作调度为唯一工作。
 *       唯一工作既可用于一次性工作，也可用于定期工作。
 *       WorkManager.enqueueUniqueWork(“工作唯一标记”, ExistingWorkPolicy.KEEP,work)（用于一次性工作） 第二个参数，表示如果系统已经存在这个标记的工作 应该采取什么措施
 *       WorkManager.enqueueUniquePeriodicWork(同上)（用于定期工作）
 *   8.2 冲突解决策略
 *       REPLACE：用新工作替换现有工作。此选项将取消现有工作。
 *       KEEP：保留现有工作，并忽略新工作。
 *       APPEND：将新工作附加到现有工作的末尾。现有工作将成为新工作的先决条件，如果现有工作变为 CANCELLED 或 FAILED 状态，新工作也会变为 CANCELLED 或 FAILED。现有工作SUCCEEDED则新工作开始执行
 *       APPEND_OR_REPLACE: 跟APPEND相似，只不过现有工作是 CANCELLED 或 FAILED 新工作仍会执行 TODO 查看代码 WorkerActivity1
 *       ExistingWorkPolicy(一次性工作的冲突解决策略) ExistingPeriodicWorkPolicy(定期工作的冲突解决策略)
 *   8.3 观察工作
 *       WorkManager.getInstance().getWorkInfoById()        观察当前工作状态
 *       WorkManager.getInstance().getWorkInfoByIdLiveData  观察实时工作状态
 *   8.4 复杂的查询工作
 *       看下面代码
 * 9. 取消和停止工作
 *    正在运行的 Worker 可能会由于以下几种原因而停止运行：
 *    您明确要求取消它（例如，通过调用 WorkManager.cancelWorkById(UUID) 取消）。
 *    如果是唯一工作，您明确地将 ExistingWorkPolicy 为 REPLACE 的新 WorkRequest 加入到了队列中。旧的 WorkRequest 会立即被视为已取消。
 *    您的工作约束条件已不再满足。系统出于某种原因指示您的应用停止工作。如果超过 10 分钟的执行期限，可能会发生这种情况。该工作会调度为在稍后重试。
 *    NOTE: 继承自Worker的 工作在取消后，state = CANCELLED 但是 doWork()依然会执行完
 * 10. 更新工作器中的进度
 *    使用 ListenableWorker 或 Worker 的 Java 开发者，使用setProgressAsync() API 会返回，更新进度是异步过程，因为更新过程涉及将进度信息存储在数据库中
 *    使用 CoroutineWorker 对象的 setProgress() 扩展函数来更新进度信息
 * 11.工作链
 *    11.1 使用方法
 *         使用workManager.beginWith(OneTimeWorkRequest) 或 workManager.beginWith(List<OneTimeWorkRequest>)创建工作链 并返回 WorkContinuation
 *         然后可以使用 WorkContinuation 通过 then(OneTimeWorkRequest) 或 then(List<OneTimeWorkRequest>) 添加 工作。如果添加的是List<OneTimeWorkRequest> 这些工作会并行执行
 *         最后使用 WorkContinuation.enqueue() 执行工作链 beginUniqueWork 执行工作链唯一性
 *         WorkManager会根据每个任务的指定约束，按请求的顺序运行任务。如果有任务返回Result.failure()或者 取消 整个序列结束。在已经失败或者取消的工作链添加新工作，新工作的状态扔将是失败或者取消
 *         note：beginWith开始的工作链没有唯一性，执行几次就会产生几条链， 工作链在执行中被打断，重启APP后会重新执行。 根据现有代码观察到的结果：执行未结束关闭APP，再次打开app，工作链A创建并启动，上次未执行完的工作链B重新执行(id与之前一样)。能够观察到A和B两条工作链。
 *    11.2 输入合并器
 *         List<OneTimeWorkRequest> 的结果将 合并在一起传给下一个任务  下一个OneTimeWorkRequest设置 setInputMerger(),会处理输入的健冲突问题
 *         OverwritingInputMerger 默认方式 会尝试将所有输入中的所有键添加到输出中。如果发生冲突，它会覆盖先前设置的键。
 *         ArrayCreatingInputMerger 会尝试合并输入，并在必要时创建数组 有冲突会创建数组
 *         WorkerManager.combine(chain1,chain2) 并发执行两条任务链
 *         [具体情形查看图片](https://developer.android.google.cn/topic/libraries/architecture/workmanager/how-to/chain-work)
 *    11.3 工作链状态
 *         当第一个 OneTimeWorkRequest 被加入工作请求链队列时(ENQUEUED)，所有后续工作请求会被屏蔽(BLOCKED)。只有第一个工作顺利完成(SUCCEEDED),才会将下一个工作请求加入队列(ENQUEUED)
 *         如果在工作器处理工作请求时出现错误，您可以根据您定义的退避政策来重试该请求,并行运行的所有其他作业均不会受到影响。重试期间后续工作会处于BLOCKED，重试用尽之前仍没有SUCCEEDED该工作以及后续工作会被标记为FAILED
 *         [查看图片](https://developer.android.google.cn/topic/libraries/architecture/workmanager/how-to/chain-work#work-statuses)
 *    11.4 取消工作链
 *
 * 12. WorkerManager 配置
 *     查看代码WorkManagerActivity1
 *
 *
 *
 * 并且能自适应系统省电策略列入Doze。TODO Doze 学习这个模式
 */
class WorkManagerActivity : AppCompatActivity() {
    lateinit var binding: ActivityWorkManagerBinding
    lateinit var workerManager: WorkManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        workerManager = WorkManager.getInstance(this)
        binding.button1.setOnClickListener {
            workerManager.cancelAllWork()
        }
        //---------------------简单应用---------------------------
        //1. 定义Worker，具体代码看 UploadWorker 类
        //2. 创建WorkRequest 调用Work
        val uploadWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<PeriodicWorker>()
                .build()
        //3. 向系统提 交WorkRequest
//        WorkManager.getInstance(this).enqueue(uploadWorkRequest)
        //--------------------定义WorkRequests详解-----------------
        //1. 调度一次性工作
        OneTimeWorkRequest.from(PeriodicWorker::class.java)//没有配置
        OneTimeWorkRequestBuilder<PeriodicWorker>()
            // Additional configuration
            .build()//有配置使用这个方法
        //1.1. 加急工作
        //WorkManager 2.7.0引入了加速工作的概念。协调系统资源，更快执行重要的工作。
        OneTimeWorkRequestBuilder<ExpeditedWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            //RUN_AS_NON_EXPEDITED_WORK_REQUEST   当应用程序没有任何加急工作配额时，加急工作请求将退回到常规工作请求
            //DROP_WORK_REQUEST                   当应用程序没有任何加急工作配额时，加急工作请求将被丢弃
            .build().apply {
//                WorkManager.getInstance(this@WorkManagerActivity).enqueue(this)// 没有使用冲突解决策略,会重复执行
                WorkManager.getInstance(this@WorkManagerActivity)
                    .enqueueUniqueWork(
                        "ExpeditedWorker",
                        ExistingWorkPolicy.KEEP,
                        this
                    )// enqueueUniqueWork 使用了冲突解决策略，避免重复开启任务
            }
//
        //1.1.1 加急工作向后兼容
        //低于 Android12(API 31)的设备执行快速服务的时候都会开启前台服务，并且必须实现getForegroundInfo或者getForegroundInfoAsync。 服务结束后通知也会消失。
        OneTimeWorkRequestBuilder<ExpeditedCoroutineWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build().apply {
                WorkManager.getInstance(this@WorkManagerActivity)
                    .enqueueUniqueWork("ExpeditedCoroutineWorker", ExistingWorkPolicy.KEEP, this)
            }
        //1.2 运行长时间任务(超过10分钟，官网没有)
        OneTimeWorkRequestBuilder<LongTimeWorker>()
            .build().apply {
                WorkManager.getInstance(this@WorkManagerActivity)
                    .enqueueUniqueWork("LongTimeWorker", ExistingWorkPolicy.KEEP, this)
            }

        //1.3. 调度周期工作
        println("----定期调度工作----")
        val saveRequest = PeriodicWorkRequestBuilder<PeriodicWorker>(
            15, TimeUnit.MINUTES,//最小间隔15分钟
            14, TimeUnit.MINUTES // 任务从 repeatInterval - flexTImeInterval 开始
        )//弹性间隔的位置总是在周期的末尾，比如本例15分钟的周期，弹性间隔14分钟，将在一分钟之后执行任务
            .build().apply {
                workerManager.enqueueUniquePeriodicWork(
                    "PeriodicWorker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    this
                )
            }
        workerManager.getWorkInfosForUniqueWorkLiveData("PeriodicWorker").observe(this) {
            if (it.isNotEmpty()) {
                println("PeriodicWorkerObserve --------${it[0]?.state}")
            }
        }
        //弹性间隔最小时间5分钟PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS，重复间隔最小时间15分钟 PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
//        WorkManager.getInstance(this).enqueue(saveRequest)//FIXME 例子实现效果不是重复间隔15分钟弹性间隔14分钟

        //2 约束条件
        Constraints.Builder()
            .setRequiresCharging(true)// 工作只能在设备充电时运行
            .setRequiredNetworkType(NetworkType.CONNECTED)//UNMETERED 好像是wifi  METERED 好像是移动网略
            .setRequiresBatteryNotLow(true)//设备处于低电量模式的时候 任务将不会被运行
            .setRequiresCharging(true)// 设备充电的时候 任务才能运行
//            .setRequiresDeviceIdle(true)//Android 6+ 设备处于空闲状态，任务才能运行
            .setRequiresStorageNotLow(true)// 设备存储空间足够的时候才能运行任务
            .build()
            .apply {
                OneTimeWorkRequestBuilder<ConstraintsWorker>().setConstraints(this).build()//设置约束条件
                    .apply {
//                        workerManager.enqueueUniqueWork(
//                            "ConstraintsWorker",
//                            ExistingWorkPolicy.KEEP,
//                            this
//                        )
                    }
            }
        //3. 延迟工作
        //也可以为 PeriodicWorkRequest 设置延迟时间，不过只在第一次起作用
        OneTimeWorkRequestBuilder<PeriodicWorker>().setInitialDelay(10, TimeUnit.MINUTES).build()
        //4. 重试和退避策略
        OneTimeWorkRequestBuilder<BackoffCriteriaWorker>().setBackoffCriteria(
            BackoffPolicy.LINEAR,//LINEAR第一次重试间隔10S 第二次间隔20S，第三次30S以此类推。EXPONENTIAL 第一次10S 第二次20S 第三次40S 第四次80S
            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,//10S
            TimeUnit.MILLISECONDS
        ).build()//重试间隔 是不精确的会有几秒误差，但不会小于第二个参数设置的时间
            .apply {
                workerManager.enqueueUniqueWork(
                    "BackoffCriteriaWorker",
                    ExistingWorkPolicy.KEEP,
                    this
                )
            }//如果不设置重试策略，doWork只执行一次
        //5. 标记 Work
        //每个工作请求都有一个唯一标识符，该标识符可用于在以后标识该工作，以便取消工作或观察其进度。
        println("----标记Work----")
        WorkManager.getInstance(this).cancelAllWorkByTag("tag1")//取消这个标记的工作
        WorkManager.getInstance(this).getWorkInfosByTag("tag").get().forEach {
            println("Work:--- ${it.state.name}")
        }
        val tagRequest = OneTimeWorkRequestBuilder<TagWorker>()
            .addTag("TagWorker1")
            .addTag("TagWorker2")
            .build()
//        tagRequest.tags // 获取该工作身上的标记 集合
        //6. 输入数据
        OneTimeWorkRequestBuilder<InputWorker>()
            .setInputData(workDataOf(InputWorker.DATANAME to 15.toDouble()))
            .build().apply {
                workerManager.enqueueUniqueWork("InputWorker", ExistingWorkPolicy.KEEP, this)
            }
        //8.1. 设置Work唯一性
        ExistingWorkPolicy.APPEND_OR_REPLACE//用在工作链中，如果前面的工作失败或者取消，后续工作仍会执行并成为工作链的开端
        //8.3 观察工作
        val observeRequest = OneTimeWorkRequestBuilder<ObserveWorker>()
            .build()
//        println("observeRequest.id = ${observeRequest.id}") //
//        workerManager.getWorkInfosForUniqueWork(observeRequest.id)// 通过request的id来监控状态，会出现一个问题，如果Activity重启，退避策略又设置成KEEP，activity重启后每次request的id都会不一样，导致监控不到
        workerManager.getWorkInfosForUniqueWorkLiveData("ObserveWorker")
//            .getWorkInfosByTagLiveData("ObserveWorker")//返回的是List<WorkerInfo> 返回一个列表,取列表第一个
            .observe(this) {
//                println("list<WorkInfo>.size = ${it?.size}")
                if (it.isNotEmpty()) {
                    println("ObserveWorker state = ${it[0]?.state}")
                }

            }
        workerManager.enqueueUniqueWork("ObserveWorker", ExistingWorkPolicy.KEEP, observeRequest)

        //8.4 复杂查询
        val workQuery = WorkQuery.Builder
            .fromTags(listOf("syncTag"))// 从标记查询
            .addStates(listOf(WorkInfo.State.FAILED, WorkInfo.State.CANCELLED))//从状态查询
            .addUniqueWorkNames(
                listOf("preProcess", "sync")//从任务名称查询
            )
            .build()

        val workInfos: ListenableFuture<List<WorkInfo>> = workerManager.getWorkInfos(workQuery)

        //9. 取消和停止工作
//        workerManager.cancelWorkById(id)
        workerManager.cancelUniqueWork("sync")
        workerManager.cancelAllWorkByTag("syncTag")
//        workerManager.cancelAllWork() 停止所有工作
        // 停止继承Worker的工作 。结论 执行cancel之后doWork依然完全执行。
        OneTimeWorkRequestBuilder<StoppedWorker>()
            .build().apply {
                workerManager.getWorkInfosForUniqueWorkLiveData("StoppedWorker")
                    .observe(this@WorkManagerActivity) {
                        if (it.isNotEmpty()) {
                            println("StoppedWorker observe ------- ${it[0]?.state}")
                        }
                    }
                workerManager.enqueueUniqueWork("StoppedWorker", ExistingWorkPolicy.KEEP, this)
                GlobalScope.launch(Dispatchers.Default) {
                    delay(100)
                    workerManager.cancelUniqueWork("StoppedWorker")// 取消后doWork依然会得到执行
//                    workerManager.cancelAllWork()
                }
            }
        // 停止继承CoroutineWorker的工作 。结论 执行cancel之后doWork依然完全执行。捕获到了异常但是要等到doWork执行完了才能捕获。
        OneTimeWorkRequestBuilder<StoppedCoroutineWorker>()// 停止继承Worker的工作 。结论 执行cancel之后doWork依然完全执行。
            .build().apply {
                workerManager.getWorkInfosForUniqueWorkLiveData("StoppedCoroutineWorker")
                    .observe(this@WorkManagerActivity) {
                        if (it.isNotEmpty()) {
                            println("StoppedCoroutineWorker observe ------- ${it[0]?.state}")
                            if (it[0]?.state == WorkInfo.State.RUNNING) {
                                workerManager.cancelUniqueWork("StoppedCoroutineWorker")
                            }
                        }
                    }
                workerManager.enqueueUniqueWork(
                    "StoppedCoroutineWorker",
                    ExistingWorkPolicy.KEEP,
                    this
                )
            }

        //10 观察工作进度
        OneTimeWorkRequestBuilder<ProgressWorker>().build()
            .apply {
                workerManager.getWorkInfoByIdLiveData(id).observe(this@WorkManagerActivity) {
                    println("ProgressWorker进度-------${it?.progress}")// 运行之后会发现，打印的是1 ，10 最后一个progress 20 没有打印，可能是因为 Worker已经是成功状态，progress不再打印，工作中收到success状态直接判定为百分之百即可
                }
                workerManager.enqueueUniqueWork(
                    "ProgressWorker",
                    ExistingWorkPolicy.KEEP,
                    this
                )
            }
        //11 工作链
        workerManager
            //11.1
            .beginUniqueWork(
                "beginUniqueWork", ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<ChainWorker>()
                    .build()
            )
            .then(
                listOf(
                    OneTimeWorkRequest.from(ChainWorker1::class.java),
                    OneTimeWorkRequest.from(ChainWorker2::class.java)
                )
            )
            .then(
                //11.2
                OneTimeWorkRequestBuilder<ChainWorker3>().setInputMerger(ArrayCreatingInputMerger::class.java)
                    .build()
            ).apply {
                workInfosLiveData.observe(this@WorkManagerActivity) {
                    it.forEach { info ->
                        if (info.state == WorkInfo.State.RUNNING) {
                            //11.4 取消工作链
                            workerManager.cancelUniqueWork("beginUniqueWork")
                        }
                    }

                }
                enqueue()
            }
    }

}
