package com.example.jetpack.bestpractice.performance

import android.os.Looper
import android.os.Process
import android.util.Log
import com.example.jetpack.DigitUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class PerformanceMonitor {

    init {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                Log.i(TAG, "进程号---${android.os.Process.myPid()}")
                getFdInfo()
                getThreadInfo()
                getHeapMemory()
                delay(10 * 1000)
            }
        }
    }

    fun getFdInfo() {

        //        /proc/pid/limits
        File(String.format("/proc/%s/limits", Process.myPid())).takeIf { it.exists() }?.apply {
            this.reader().use { reader ->
                reader.readLines().filter {
                    it.contains("Max open files")
                }.takeIf { it.isNotEmpty() }?.first()?.split(" ")?.takeIf { it.isNotEmpty() }
                    ?.filter { it.isNotEmpty() }?.also {
                        Log.i(TAG, "最大---${it[3]}")
                    }
                //分割空格 https://blog.csdn.net/yezonghui/article/details/106455940
            }
        } ?: Log.i(TAG, " /proc/%s/limits 没有该文件")
        File(String.format("/proc/%s/fd", Process.myPid())).takeIf { it.exists() }?.apply {
            Log.i(TAG, "fd数量---${listFiles()?.takeIf { it.isNotEmpty() }?.size}")
//            listFiles().forEach {
//                Log.i(TAG, "${it.name}\t ${Os.readlink(it.absolutePath)}")
//            }
        } ?: Log.i(TAG, " /proc/%s/fd 没有该文件")
    }

    fun getThreadInfo() {
        //我们可以读取 /proc/[ pid ]/status 中的 Threads 字段的值; 实际测试k30pro没有这个字段
        File(String.format("/proc/%s/status", Process.myPid())).takeIf { it.exists() }?.apply {
            this.reader().use { reader ->
                Log.i(TAG, "${reader.readText()}")
                //分割空格 https://blog.csdn.net/yezonghui/article/details/106455940
            }
        } ?: Log.i(TAG, "/proc/%s/status 没有该文件")

        File(String.format("/proc/%s/task", Process.myPid())).takeIf { it.exists() }?.apply {
            listFiles().forEach {
                Log.i(TAG, "${it.name}\t ${it.absolutePath}   是文件${it.isFile}")
            }
        } ?: Log.i(TAG, "/proc/%s/task  没有该文件")
        val threadGroup: ThreadGroup = Looper.getMainLooper().thread.threadGroup
        val threadList = arrayOfNulls<Thread>(threadGroup.activeCount() * 2)
        val size = threadGroup.enumerate(threadList);//复制实际线程到这个数组，返回实际线程数量
        threadList.take(size).forEach {
            Log.i(TAG, "线程---${it?.name}  线程状态---${it?.state}")
        }
        Log.i(TAG, "线程数量---$size")
    }

    fun getHeapMemory() {
        MemoryUtil(
            Runtime.getRuntime().maxMemory(), Runtime.getRuntime().totalMemory(), Runtime.getRuntime().freeMemory()
        )
    }

    companion object {
        const val TAG = "PerformanceMonitor"
    }

    class MemoryUtil(private val _max: Long, private val _total: Long, private val _free: Long) {

        private val max: Float
            get() = DigitUtils.m1(1.0 * _max / 1024 / 1024).toFloat()
        private val total: Float
            get() = DigitUtils.m1(1.0 * _total / 1024 / 1024).toFloat()
        private val free: Float
            get() = DigitUtils.m1(1.0 * _free / 1024 / 1024).toFloat()
        private val used: Float
            get() = DigitUtils.m1(1.0 * (_total - _free) / 1024 / 1024).toFloat()
        private val rate: Float
            get() = DigitUtils.m1(1.0 * used / total).toFloat()

        init {

            Log.i(TAG, "max---${max}MB \ntotal---${total}MB \nfree---${free}MB \nused---${used}MB \nrate---$rate")
        }
    }
}
//[Linux下进程信息/proc/pid/status的深入分析](https://blog.csdn.net/beckdon/article/details/48491909)
//Name:	example.jetpack
//Umask:	0077
//State:	S (sleeping)
//Tgid:	27105  Tgid是线程组的ID,一个线程一定属于一个线程组(进程组).
//Ngid:	0
//Pid:	27105
//PPid:	877
//TracerPid:	0
//Uid:	10820	10820	10820	10820
//Gid:	10820	10820	10820	10820
//FDSize:	128
//Groups:	3001 3002 3003 9997 20820 50820
//VmPeak:	 6767696 kB 这里的VmPeak代表当前进程运行过程中占用内存的峰值. k30pro物理内存正好是6G
//VmSize:	 6541476 kB VmSize代表进程现在正在占用的内存
//VmLck:	       0 kB VmLck代表进程已经锁住的物理内存的大小.锁住的物理内存不能交换到硬盘.
//VmPin:	       0 kB
//VmHWM:	  141368 kB VmHWM是程序得到分配到物理内存的峰值.
//VmRSS:	  137828 kB VmRSS是程序现在使用的物理内存.
//RssAnon:	   61208 kB
//RssFile:	   75204 kB
//RssShmem:	    1416 kB
//VmData:	 1297648 kB 表示进程数据段的大小.
//VmStk:	    8192 kB 表示进程堆栈段的大小.
//VmExe:	      16 kB 表示进程代码的大小.
//VmLib:	  161000 kB 表示进程所使用LIB库的大小.
//VmPTE:	    1140 kB
//VmSwap:	   24452 kB
//CoreDumping:	0
//Threads:	24
//SigQ:	0/20891
//SigPnd:	0000000000000000
//ShdPnd:	0000000000000000
//SigBlk:	0000000080001204
//SigIgn:	0000000000000001
//SigCgt:	0000006e400084f8
//CapInh:	0000000000000000
//CapPrm:	0000000000000000
//CapEff:	0000000000000000
//CapBnd:	0000000000000000
//CapAmb:	0000000000000000
//NoNewPrivs:	0
//Seccomp:	2
//Speculation_Store_Bypass:	thread vulnerable
//Cpus_allowed:	7f
//Cpus_allowed_list:	0-6
//Mems_allowed:	1
//Mems_allowed_list:	0
//voluntary_ctxt_switches:	151
//nonvoluntary_ctxt_switches:	169