package com.example.jetpack.topics.userinterface.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.media.session.MediaSessionCompat
import android.widget.RemoteViews
import androidx.annotation.RestrictTo
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.*
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.app.TaskStackBuilder
import com.example.jetpack.*
import com.example.jetpack.databinding.ActivityNotifactionBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.media.app.NotificationCompat as MediaNotificationCompat

/**
 * https://developer.android.google.cn/guide/topics/ui/notifiers/notifications
 * 1. 通知显示方式：状态栏式通知栏、提醒式通知栏(android5.0及以上)、锁屏通知(android5.0及以上)、应用图标通知、穿戴设备通知
 *    - 状态栏通知: 发出通知后，通知会先以图标的形式显示在状态栏中,在状态栏向下滑动以打开抽屉式通知栏，并在其中查看更多详情及对通知执行操作
 *    - 提醒式通知：可以短暂地显示在浮动窗口中，例如小米手机收到短信
 * 2. 通知组成
 *    查看文档 ![image](https://developer.android.google.cn/images/ui/notifications/notification-callouts_2x.png)
 * 3. 从通知启动Activity
 *    3.1 常规Activity：这类 Activity 是应用的正常用户体验流程的一部分。因此，当用户从通知转到这类 Activity 时，新任务应包括完整的返回堆栈，
 *        以便用户可以按“返回”按钮并沿应用层次结构向上导航。使用 TaskStackBuilder 设置 PendingIntent 通过代码创建任务栈
 *        并在android:parentActivityName 设置上层Activity
 *        [效果实例](https://blog.csdn.net/simplebam/article/details/79381754)
 *    3.2
 * 4. PendingIntent
 *    3.1 PendingIntent 的关键点是其他应用在触发 intent 时是以您应用的名义。换而言之，其他应用会使用您应用的身份来触发 intent。
 *    3.2 对系统令牌的引用，该令牌描述用于检索它的原始数据，即时拥有它的进程消失，也能触发Intent  TODO 不太明白这个类是干嘛的，目前只知道notification跳转需要这个类
 *    3.3 相同代码创建的PendingIntent会引用相同的系统令牌，所以不要重复创建PendingIntent
 *    https://mp.weixin.qq.com/s/8xGvJXHmU0KN3tv3AWfKOA
 * 5. 通知渠道
 *    5.1 重要性 (NotificationManager.IMPORTANCE_*) 和优先级常量 (NotificationCompat.PRIORITY_*) 会映射到用户可见的重要性选项（如表 1 中所示）
 *        https://developer.android.google.cn/training/notify-user/channels
 *    5.2 读取通知渠道 getNotificationChannel() 或 getNotificationChannels() 来获取 NotificationChannel 对象。
 *    5.3 查询特定的渠道设置，例如 getVibrationPattern()、getSound() 和 getImportance() //TODO 根据重要性，来打开通知渠道设置
 *    5.4 删除通知渠道 notificationManager.deleteNotificationChannel(id)
 *    5.5 打开通知渠道设置
 *    5.6 TODO 创建通知渠道分组
 * 6. 原点通知
 *    从 8.0（API 级别 26）开始，launch的应用图标会显示原点通知。用户可以长按应用图标以显示通知
 *    默认情况下，您的应用无需执行任何操作，就会显示在支持圆点的启动器应用中，然而，在某些情况下，您可能不希望显示通知圆点，
 *    或者想要精确控制要在其中显示哪些通知。
 *    6.1  NotificationChannel 对象调用 setShowBadge(false)，针对每个渠道停用标志。
 *    6.2 设置通知数量  长按菜单上显示的数字会随着通知数量,也可以自定 NotificationCompat.Builder().setNumber()
 *    6.3 修改图标 .默认情况下显示大图标(如果有)也可以设置小图标NotificationCompat.Builder().setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
 * 7. TODO 此例的通知都被小米自动规划到不重要的通知，需要手动设置， 根据重要性，来打开通知渠道设置
 *
 */
class NotificationActivity : AppCompatActivity() {
    lateinit var binding: ActivityNotifactionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotifactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //1. 注册通知渠道
        // createNotificationChannel()//android8+ 显示同之前需要先注册通知渠道，重复注册已有的渠道不会产生问题，建议放在程序启动阶段(已转义到myApplication)
        //2. 创建通知
        var builder =
            NotificationCompat.Builder(this, CHANNEL_ID)//渠道ID，android8.0 API 26及以上必须设置，旧版本忽略
                .setSmallIcon(R.drawable.notification_icon)//1. 小图标，必须提供
                .setContentTitle("启动通知")
                .setContentText("常规Activity")//过长的文本内容会被截断放在一行，多余的内容利用setStyle()展开显示
                .setStyle(//setStyle 显示展开内容
                    NotificationCompat.BigTextStyle()//如需对文本添加格式（粗体、斜体、换行等等），您可以使用 HTML 标记添加样式。
                        //https://developer.android.google.cn/guide/topics/resources/string-resource#StylingWithHTML
                        .bigText("默认情况下，通知的文本内容会被截断以放在一行。如果您想要更长的通知，可以使用 setStyle() 添加样式模板来启用可展开的通知。例如，以下代码会创建更大的文本区域：")
                )//
                .setGroup(NOTIFICATION_GROUP)//添加通知组
                .setVisibility(VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)//优先级确定通知在 Android 7.1 和更低版本上的干扰程度
        //2. 点击通知启动Activity
        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, Notification1Activity::class.java).apply {
            //设置flag 有两种情况：
            //2.1 专用于响应通知的 Activity,因此会启动一个新任务，而不是添加到应用的现有任务和返回堆栈。
            //2.2 应用的常规应用流程中存在的 Activity,启动 Activity 时应创建返回堆栈，以便保留用户对返回和向上按钮的预期。TODO 还不晓得向上预期是什么。看导航
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        //TaskStackBuilder + android:parentActivityName  作用
        //android:parentActivityName 的作用，就是为了左上角给子Activity加一个返回button
        //如果父 Activity 不在当前栈内（没有在当前栈有实例），这个属性不起效
        //如果父 Activity 在当前栈（在当前有实例），设置了该属性，父 Activity 会经历先销毁后创建的过程
        //如果设置父 Activity启动模式为 singleTask 或者 singleTop （两者效果一样的），那么父 Activity 就具有与 singTask 一样清理栈的 作用（清除在父 Activity 之上的那些Activity ），使得 父 Activity 得以重新独占设置与用于交互（可以走 onRsume 方法）
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(intent)
//            editIntentAt() //如果有需要 往intent里面添加参数
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        builder.setContentIntent(pendingIntent)
            .setAutoCancel(true)//点击通知后移除通知
        //3. 显示通知
        //请记得保存您传递到 NotificationManagerCompat.notify() 的通知 ID，因为如果之后您想要更新或移除通知，将需要使用这个 ID
        //从 Android 8.1（API 级别 27）开始，应用每秒最多只能发出一次通知提示音。如果应用在一秒内发出了多条通知，这些通知都会按预期显示，
        //但是每秒中只有第一条通知发出提示音。
        var builder1 =
            NotificationCompat.Builder(this, CHANNEL_ID)//渠道ID，android8.0 API 26及以上必须设置，旧版本忽略
                .setSmallIcon(R.drawable.notification_icon)//1. 小图标，必须提供
                .setContentTitle("启动通知")
                .setContentText("特殊Activity")//过长的文本内容会被截断放在一行，多余的内容利用setStyle()展开显示
                .setPriority(NotificationCompat.PRIORITY_HIGH)//优先级确定通知在 Android 7.1 和更低版本上的干扰程度
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this, ExcludeFromRecentsActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        },
                        0
                    )
                )
                .setGroup(NOTIFICATION_GROUP)//添加通知组
                .setAutoCancel(true)
        GlobalScope.launch {
            delay(8 * 1000)
            NotificationManagerCompat.from(this@NotificationActivity).apply {
                notify(99, builder1.build())
                notify(100, builder.build())
            }
        }

        //4. 添加通知按钮
        binding.button1.setOnClickListener {
            val snoozeIntent = Intent(this, NotificationBroadcast::class.java).apply {
                putExtra(EXTRA_NOTIFICATION_ID, 100)
            }
            //：在 Android 10（API 级别 29）和更高版本中，如果应用不提供自己的通知操作按钮，则平台会自动生成通知操作按钮。
            // 如果您不希望应用通知显示任何建议的回复或操作，可以使用 setAllowGeneratedReplies() 和 setAllowSystemGeneratedContextualActions()
            // 选择停用系统生成的回复和操作  TODO 使用android 11 测试发现系统并没有生成通知操作按钮
            val snoozePendingIntent: PendingIntent =
                PendingIntent.getBroadcast(this, 0, snoozeIntent, 0)
            builder.addAction(R.drawable.snooz, "打盹", snoozePendingIntent)//最多添加3个按钮，图标并没有显示
            NotificationManagerCompat.from(this).notify(100, builder.build())
        }
        //5. 直接回复
        //Android 7.0（API 级别 24）中引入的直接回复操作允许用户直接在通知中输入文本，然后会直接提交给应用，而不必打开 Activity
        binding.button2.setOnClickListener {

            var replyLabel: String = resources.getString(R.string.reply_label)
            var remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
                setLabel(replyLabel)
                build()
            }
            val replayIntent = Intent(this, NotificationBroadcast::class.java).apply {
                putExtra(EXTRA_NOTIFICATION_ID, 101)
            }
            // Build a PendingIntent for the reply action to trigger.
            var replyPendingIntent: PendingIntent =
                PendingIntent.getBroadcast(
                    applicationContext,
                    1,
                    replayIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            var action: NotificationCompat.Action =
                NotificationCompat.Action.Builder(
                    R.drawable.replay,
                    "回复1", replyPendingIntent
                )
                    .addRemoteInput(remoteInput)
                    .build()
            builder.addAction(action)
            NotificationManagerCompat.from(this).notify(100, builder.build())
            //处理文本的内容看NotificationBroadcast
        }
        //6. 添加进度条
        binding.button3.setOnClickListener {
            val snoozeIntent = Intent(this, NotificationBroadcast::class.java).apply {
                putExtra(EXTRA_NOTIFICATION_ID, 100)
            }
            val snoozePendingIntent: PendingIntent =
                PendingIntent.getBroadcast(this, 0, snoozeIntent, 0)
            var progressBuild = NotificationCompat.Builder(this, CHANNEL_PROGRESS).apply {
                setContentTitle("Picture Download")
                setContentText("Download in progress")
                setSmallIcon(R.drawable.download)
                priority = NotificationCompat.PRIORITY_LOW
                setAutoCancel(true)
                setContentIntent(snoozePendingIntent)//设置之后点击才会消失
                setOnlyAlertOnce(true)//设置后 更新通知不会再响
            }
            var progressCurrent = 0
            NotificationManagerCompat.from(this).apply {
                GlobalScope.launch {//FIXME 暂且这样写，协程的生命周期要和通知一致
                    while (progressCurrent <= 100) {
                        progressBuild.setProgress(100, progressCurrent++, false)//true 不确定性进度条，看不到进度
                        notify(101, progressBuild.build())
                        delay(100)
                    }// 下载文件使用  DownloadManager
                    progressBuild.setContentText("Download complete")
//                        .setProgress(0, 0, false)
                        .setAutoCancel(true)//要设置 setContentIntent()点击才会消失
                    notify(101, progressBuild.build())
                }
            }
        }
        //7. 设置系统通知类别
        //设置系统范围的类别,来决定当设备处于勿扰模式时是否显示通知。例如 CATEGORY_ALARM、CATEGORY_REMINDER、CATEGORY_EVENT 或 CATEGORY_CALL
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        //8. 显示紧急消息
        // Android 10+（API 级别 29），必须在应用清单文件中请求 USE_FULL_SCREEN_INTENT 权限，以便系统启动与时效性通知关联的全屏 Activity。
        // 但这个API在android11 失效被替换成提醒式通知
        //https://github.com/giorgosneokleous93/fullscreenintentexample  github 关于全屏通知的例子，这个例子在android10上运行结果和官方文档一致，在android11上弹了提醒式通知
        // (官方文档说系统界面可以选择在用户使用设备时显示提醒式通知，而不是启动全屏 intent。感觉弹提醒通知还是全屏是系统控制的)
        //如果用户设备被锁定，会显示全屏 Activity，覆盖锁屏。 只在android10有效
        //如果用户设备处于解锁状态，通知以展开形式显示，其中包含用于处理或关闭通知的选项。 只在android10有效
        //包含全屏 Intent 的通知有很强的干扰性，因此这类通知只能用于最紧急的时效性消息
        GlobalScope.launch {
            delay(10 * 1000)
            val fullScreenIntent =
                Intent(this@NotificationActivity, FullScreenActivity::class.java)
            val fullScreenPendingIntent = PendingIntent.getActivity(
                this@NotificationActivity, 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            var fullScreenBuilder =
                NotificationCompat.Builder(this@NotificationActivity, CHANNEL_FULL)
                    .setSmallIcon(R.drawable.full_screen)
                    .setContentTitle("全屏通知")
                    .setContentText("显示全屏通知")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setFullScreenIntent(
                        fullScreenPendingIntent,
                        true
                    )//FIXME 在android 6测试发现，全屏通知不能点击，需要添加setContentIntent
//                    .setContentIntent(fullScreenPendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setAutoCancel(true)
                    .setTimeoutAfter(5 * 1000)
            NotificationManagerCompat.from(this@NotificationActivity).apply {
                notify(102, fullScreenBuilder.build())
            }
        }
        //9. 锁定屏幕公开范围
        //setVisibility()
        //VISIBILITY_PUBLIC 显示通知的完整内容。
        //VISIBILITY_SECRET 不在锁定屏幕上显示该通知的任何部分。
        //VISIBILITY_PRIVATE 显示基本信息，例如通知图标和内容标题，但隐藏通知的完整内容。
        //10. 关闭通知
        NotificationManagerCompat.from(this).apply {
//            builder.setAutoCancel(true) //用户点击时关闭通知
//            cancel(1)                   //关闭指定id通知
//            cancelAll()                 //关闭此前发出的所有通知
//            builder.setTimeoutAfter(10*1000)// 一定时间后关闭通知
        }

        //11. 有关即时通讯的通知
        //TODO 要做
        //12. 添加大图片
        binding.button4.setOnClickListener {
            var notification =
                NotificationCompat.Builder(this@NotificationActivity, CHANNEL_PICTURE)
                    .setSmallIcon(R.drawable.big_picture)
                    .setContentTitle("大图通知")
                    .setContentText("显示大图片通知")
//                    .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)//默认情况下圆点通知会显示setLargeIcon，设置该属性后显示 setSmallIcon
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.dog))//未展开显示缩略图
                    .setStyle(//展开显示大图
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(BitmapFactory.decodeResource(resources, R.drawable.dog))
                            .bigLargeIcon(null)
                    )
                    .setAutoCancel(true)
                    .build()
            NotificationManagerCompat.from(this@NotificationActivity).apply {
                notify(103, notification)
            }
        }
        //13. 显示收件箱样式通知
        //每条文本均截断为一行，最多添加6行
        binding.button5.setOnClickListener {
            var notification =
                NotificationCompat.Builder(this@NotificationActivity, CHANNEL_INBOX)
                    .setSmallIcon(R.drawable.inbox)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setContentTitle("收件箱样式通知")
                    .setContentText("显示通知")

                    .setStyle(
                        NotificationCompat.InboxStyle()
                            .addLine("通知1")
                            .addLine("通知2")
                    )
                    .setAutoCancel(true)
                    .build()
            NotificationManagerCompat.from(this@NotificationActivity).apply {
                notify(104, notification)
            }
        }
        //14. 再通知中显示对话 TODO
        //15. 使用媒体控件创建通知
        //TODO 收紧状态并没有显示三个按钮，放开状态也只显示一个按钮，改成收紧扩展都显示三个按钮
        binding.button6.setOnClickListener {
            println("button6----click")
            val mediaSession = MediaSessionCompat(this@NotificationActivity, "MediaPlayer")
            var notification = NotificationCompat.Builder(this@NotificationActivity, CHANNEL_MEDIA)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.player)
                // Add media control buttons that invoke intents in your media service
                .addAction(R.drawable.previous, "Previous", null) // #0
                .addAction(R.drawable.pause, "Pause", null) // #1
                .addAction(R.drawable.next, "Next", null) // #2
                // Apply the media style template
                .setStyle(
                    MediaNotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1 /* #1: pause button \*/)
                        .setMediaSession(mediaSession.sessionToken)// TODO 需要看音视频开发

                )
                .setContentTitle("Wonderful music")
                .setContentText("My Awesome Band")
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.album2))
                .build()
            NotificationManagerCompat.from(this@NotificationActivity).apply {
                notify(105, notification)
            }
        }
        //16. 设置通知组
        binding.button7.setOnClickListener {
            //应用发出 4 条或更多条通知且未指定通知组，在 Android 7.0 及更高版本上，系统会自动将这些通知分为一组。
            //通过手动创建通知组，适配低版本系统，通过例如，如果您的应用针对收到的电子邮件显示通知，您应将所有通知放入同一个通知组，以便它们可以收起。
            //还可以添加摘要通知，该摘要通知会单独显示以总结所有单独的通知 - 通常最好使用收件箱样式的通知实现此目的。
            //FIXME 在android 6上面运行，先显示两个通知，再显示通知摘要，之前的两个通知消失，通知摘要不能点击。
            val summaryNotification =
                NotificationCompat.Builder(this@NotificationActivity, CHANNEL_ID)
                    .setContentTitle("通知组1")//设置了 setStyle 后，这个属性没用了
                    //set content text to support devices running API level < 24
                    .setContentText("Two new messages1")//设置了 setStyle 后，这个属性没用了
                    .setSmallIcon(R.drawable.notification_group)
                    //build summary info into InboxStyle template
                    .setStyle(
                        NotificationCompat.InboxStyle()
                            .addLine("Alex Faarborg Check this out")
                            .addLine("Jeff Chang Launch Party")
                            .setBigContentTitle("2 new messages2")
                            .setSummaryText("通知组2")
                    )
                    //specify which group this notification belongs to
                    .setGroup(NOTIFICATION_GROUP)
                    //set this notification as the summary for the group
                    .setGroupSummary(true)
                    .build()
            NotificationManagerCompat.from(this@NotificationActivity).apply {
                notify(106, summaryNotification)
            }
        }
        //17. 打开渠道设置
        binding.button8.setOnClickListener {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, "com.example.jetpack")
                putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_MEDIA)
            }
            startActivity(intent)
        }
        //18. 自定义布局通知
        binding.button9.setOnClickListener {
// Get the layouts to use in the custom notification
            println("packageName---$packageName")
            val notificationLayout = RemoteViews(packageName, R.layout.notification_small)
            val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_large)

            // Apply the layouts to the notification
            val customNotification =
                NotificationCompat.Builder(this@NotificationActivity, CHANNEL_ID)
                    .setSmallIcon(R.drawable.diy)
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())//如果要为媒体播放控件创建自定义通知，改用 NotificationCompat.DecoratedMediaCustomViewStyle 类。
                    .setCustomContentView(notificationLayout)
                    .setCustomBigContentView(notificationLayoutExpanded)//如果 展开内容太少，将不显示折叠内容，直接显示展开内容
                    .build()
            NotificationManagerCompat.from(this@NotificationActivity).apply {
                notify(107, customNotification)
            }
        }
    }


}