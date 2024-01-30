package com.example.jetpack.bestpractice.performance

import android.os.Bundle
import android.os.Looper
import android.system.Os
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityPerformanceBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import java.io.File

/**
 * [Android修炼系列（33），内存监控技术方案（上）](https://juejin.cn/post/7080461351474167844)
 * 1. FD 数量
 *    FD 即 File Descriptor (文件描述符)，对于 Android 来说，一个进程能使用的 FD 资源是有限的，在 Android9 前，最多限制 1024，Android9 及以上，最多 3w 余个。
 *    而 FD 达到上限后，没资源了就会产生各种问题，跟 OOM 一样，很难被定位到，因为 crash 后的堆栈信息可能并没指向“始作俑者”。所以 FD 泄漏的监控是很有必要的。
 *    那什么操作会占用 FD 资源呢？常见的：文件读写、Socket 通信、创建 java 线程、启用 HandlerThread、创建 Window 、数据库操作等。
 *    文件描述符在Linux系统中是文件的索引，Android继承了Linux的文件系统
 *    Os.readlink(it.absolutePath)} 文件描述符关联的文件
 *    方案：直接开个线程，每 10s 周期检查一次当前进程 FD 数量，当 FD 数量达到阈值时（如90%），
 *    就抓取一次当前进程的 FD 信息、线程信息、内存快照信息。
 * 2. 线程数量
 *    threadGroup.activeCount()通过这个方法获得线程数量靠谱吗？还不太懂线程组，看完jvm再来看这个
 *    每个线程都对应着一个栈内存，在 Android 中，一个 java 线程大概占用 1M 栈内存，如果是 native 线程，
 *    可以通过 pthread_atta_t 来指定栈大小，如果不加限制的创建线程，就会导致 OOM crash。
 *    配置文件 /proc/sys/kernel/threads-max 指定了系统范围内的最大线程数量 实际测试k30pro没有这个文件，那么怎么获取进程最大线程数呢？
 *    监控线程泄漏要 在c++层面Hook，不知道怎么实现，待学
 *    [Java线程和操作系统线程的关系](https://zhuanlan.zhihu.com/p/133275094)
 * 3. 虚拟内存
 *    对于虚拟内存的使用状态，我们可以通过 /process/pid/status 的 VmSize 字段获
 *    对status文件的解释 https://blog.csdn.net/beckdon/article/details/48491909
 *    Java 堆的大小是系统为应用程序设置的，我们可通过设置 AndroidManifest 中的 application.largeHeap 属性来获取更大的堆空间限制。
 *    而且我们能直接通过 Runtime 接口来获取一些堆内存状态，来配合内存快照排查问题:
 *    TODO Runtime.getRuntime().maxMemory() 获取内存和读state文件获取到的是一个吗 VmHWM
 * java 堆
 *
 * Native 内存
 */
@AndroidEntryPoint
class PerformanceActivity : AppCompatActivity() {
    lateinit var binding: ActivityPerformanceBinding
    private val viewModel by viewModels<PerformanceModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_performance)
        binding.vm = viewModel
        binding.lifecycleOwner = this
        binding.handler = this

        //1.0 FD 监控
//        getFdInfo()
        //2. 获取线程数量
//        getThreadInfo()
    }

//    fun getFdInfo() {
//        //        /proc/pid/limits
//        File(String.format("/proc/%s/limits", android.os.Process.myPid())).takeIf { it.exists() }?.apply {
//            this.reader().use { reader ->
//                reader.readLines().filter {
//                    it.contains("Max open files")
//                }.takeIf { it.isNotEmpty() }?.first()?.split(" ")?.takeIf { it.isNotEmpty() }
//                    ?.filter { it.isNotEmpty() }?.also {
//                        Log.i(TAG, "最大---${it[3]}")
//                    }
//                //分割空格 https://blog.csdn.net/yezonghui/article/details/106455940
//            }
//        } ?: Log.i(TAG, " /proc/%s/limits 没有该文件")
//        File(String.format("/proc/%s/fd", android.os.Process.myPid())).takeIf { it.exists() }?.apply {
//            Log.i(TAG, "fd数量---${listFiles()?.takeIf { it.isNotEmpty() }?.size}")
////            listFiles().forEach {
////                Log.i(TAG, "${it.name}\t ${Os.readlink(it.absolutePath)}")
////            }
//        } ?: Log.i(TAG, " /proc/%s/fd 没有该文件")
//    }
//
//    fun getThreadInfo() {
//        //我们可以读取 /proc/[ pid ]/status 中的 Threads 字段的值; 实际测试k30pro没有这个字段
//        File(String.format("/proc/%s/status", android.os.Process.myPid())).takeIf { it.exists() }?.apply {
//            this.reader().use { reader ->
//                Log.i(TAG, "${reader.readText()}")
//                //分割空格 https://blog.csdn.net/yezonghui/article/details/106455940
//            }
//        } ?: Log.i(TAG, "/proc/%s/status 没有该文件")
//
//        File(String.format("/proc/%s/task", android.os.Process.myPid())).takeIf { it.exists() }?.apply {
//            listFiles().forEach {
//                Log.i(TAG, "${it.name}\t ${it.absolutePath}   是文件${it.isFile}")
//            }
//        } ?: Log.i(TAG, "/proc/%s/task  没有该文件")
//        val threadGroup: ThreadGroup = Looper.getMainLooper().thread.threadGroup
//        val threadList = arrayOfNulls<Thread>(threadGroup.activeCount() * 2)
//        val size = threadGroup.enumerate(threadList);//复制实际线程到这个数组，返回实际线程数量
//        threadList.forEach {
//            Log.i(TAG, "线程---${it?.name}  线程状态---${it?.state}")
//        }
//        Log.i(TAG, "线程数量---$size")
//    }

    companion object {
        const val TAG = "PerformanceActivity"
    }
}