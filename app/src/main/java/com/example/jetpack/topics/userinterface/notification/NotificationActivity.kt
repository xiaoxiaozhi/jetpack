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
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RestrictTo
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_NOTIFICATION_ID
import androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
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
 * 3. PendingIntent
 *    3.1 PendingIntent 的关键点是其他应用在触发 intent 时是以您应用的名义。换而言之，其他应用会使用您应用的身份来触发 intent。
 *    3.2 对系统令牌的引用，该令牌描述用于检索它的原始数据，即时拥有它的进程消失，也能触发Intent  TODO 不太明白这个类是干嘛的，目前只知道notification跳转需要这个类
 *    3.3 相同代码创建的PendingIntent会引用相同的系统令牌，所以不要重复创建PendingIntent
 *    https://mp.weixin.qq.com/s/8xGvJXHmU0KN3tv3AWfKOA
 * 4. TODO 此例的通知都被小米自动规划到不重要的通知，需要手动设置
 *
 */
class NotificationActivity : AppCompatActivity() {
    lateinit var binding: ActivityNotifactionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotifactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //1. 注册通知渠道
        createNotificationChannel()//android8+ 显示同之前需要先注册通知渠道，重复注册已有的渠道不会产生问题，建议放在程序启动阶段
        //2. 创建通知
        var builder =
            NotificationCompat.Builder(this, CHANNEL_ID)//渠道ID，android8.0 API 26及以上必须设置，旧版本忽略
                .setSmallIcon(R.drawable.notification_icon)//1. 小图标，必须提供
                .setContentTitle("Notification")
                .setContentText("默认情况下")//过长的文本内容会被截断放在一行，多余的内容利用setStyle()展开显示
                .setStyle(//setStyle 显示展开内容
                    NotificationCompat.BigTextStyle()//如需对文本添加格式（粗体、斜体、换行等等），您可以使用 HTML 标记添加样式。
                        //https://developer.android.google.cn/guide/topics/resources/string-resource#StylingWithHTML
                        .bigText("默认情况下，通知的文本内容会被截断以放在一行。如果您想要更长的通知，可以使用 setStyle() 添加样式模板来启用可展开的通知。例如，以下代码会创建更大的文本区域：")
                )//
                .setPriority(NotificationCompat.PRIORITY_HIGH)//优先级确定通知在 Android 7.1 和更低版本上的干扰程度
        //2. 设置通知点击操作
        // Create an explicit intent for an Activity in your app
        val intent = Intent(this, Notification1Activity::class.java).apply {
            //设置flag 有两种情况：
            //2.1 专用于响应通知的 Activity,因此会启动一个新任务，而不是添加到应用的现有任务和返回堆栈。
            //2.2 应用的常规应用流程中存在的 Activity,启动 Activity 时应创建返回堆栈，以便保留用户对返回和向上按钮的预期。TODO 还不晓得向上预期是什么。看导航
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        builder.setContentIntent(pendingIntent)
            .setAutoCancel(true)//点击通知后移除通知
        //3. 显示通知
        //请记得保存您传递到 NotificationManagerCompat.notify() 的通知 ID，因为如果之后您想要更新或移除通知，将需要使用这个 ID
        //从 Android 8.1（API 级别 27）开始，应用每秒最多只能发出一次通知提示音。如果应用在一秒内发出了多条通知，这些通知都会按预期显示，
        //但是每秒中只有第一条通知发出提示音。
        NotificationManagerCompat.from(this).notify(100, builder.build())
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
        //https://github.com/giorgosneokleous93/fullscreenintentexample TODO github 关于全屏通知的例子，这个例子弹了提醒式通知，但是普通通知也可以手动设置为提醒式通知
        // TODO (官方文档说系统界面可以选择在用户使用设备时显示提醒式通知，而不是启动全屏 intent。感觉弹提醒通知还是全屏是系统控制的)
        //如果用户设备被锁定，会显示全屏 Activity，覆盖锁屏。TODO 也没有显示全屏Activity
        //如果用户设备处于解锁状态，通知以展开形式显示，其中包含用于处理或关闭通知的选项。TODO 并没有以展开形式，也不包含处理或关闭通知的选项
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
                    .setFullScreenIntent(fullScreenPendingIntent, true)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setAutoCancel(true)
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


    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }// importance 此参数确定出现任何属于此渠道的通知时如何打断用户
            // 注册通知
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            //注册进度条通知
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_PROGRESS,
                    "显示进度条",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "用通知显示进度"
                })
            //注册全屏通知
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_FULL,
                    "全屏通知",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "显示全屏通知"
                })
            //注册即时通讯通知
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_MESSAGE,
                    "即时通讯",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "显示即时通讯"
                })
            //大图片
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_PICTURE,
                    "大图片",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "显示大图片"
                })
            //收件箱式通知
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_INBOX,
                    "收件箱样式通知",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "显示通知"
                })
            //多媒体通知
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_MEDIA,
                    "多媒体通知",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "显示多媒体通知"
                })

        }
    }


}