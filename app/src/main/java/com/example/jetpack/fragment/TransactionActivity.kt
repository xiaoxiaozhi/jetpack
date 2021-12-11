package com.example.jetpack.fragment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace

import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityTransactionBinding
import com.example.jetpack.lifecycle.LifeCycleFragment

/**
 * 事务
 * 1. FragmentTransaction可以使用片段添加、删除、替换和执行其他操作，以响应用户交互。
 *    您提交的每一组片段更改都称为事务，可以将多个操作分组到单个事务中
 * 2. 每个FragmentTransaction应该使用setReorderingAllowed(True)
 * 3. https://blog.csdn.net/cqkxzsxy/article/details/78475784 事务每个操作看这里
 * 4. 动画分为 碎片进出动画，和共享元素动画
 */
class TransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //1. 创建事务
        supportFragmentManager.beginTransaction()
        //2. commit 是异步的不会马上在主线程上执行，如果需要马上执行，调用commitNow,但是它和addToBackStack不兼容
        supportFragmentManager.beginTransaction().commitNow()
        supportFragmentManager.executePendingTransactions()//执行所有尚未运行的COMMIT(),该方法与addToBackStack兼容
        //3. 设置动画，体现setReorderingAllowed()方法, 还是验证不了，似乎没什么用处。建议使用过度动画
        binding.performance.setOnClickListener {
            supportFragmentManager.commit {
                setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                add<LifeCycleFragment>(R.id.transactionContainer)//蓝
//                supportFragmentManager.findFragmentById(R.id.transactionContainer)
//                    ?.let { remove(it) }
//                setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                replace<ManagerFragment2>(R.id.transactionContainer)//红
                addToBackStack(null)
                setReorderingAllowed(true)//暂时看不出来有什么用处
//            }
            }
        }
        //4. 共享元素过渡动画
        //本例中实现对于图片的过渡，FragmentA的一张图片，在FragmentB中全屏显示
        //在FragmentA中  ViewCompat.setTransitionName(控件1, "过渡名称1")，
        //在FragmentB中设置一个全屏控件接收ViewCompat.setTransitionName(控件2, “过渡名称2”)
        //事务调用addSharedElement(itemImageView, "item_image")
        binding.add.setOnClickListener {
            println("add---------------OnClickListener")
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter,
                    R.anim.exit,
                    R.anim.pop_enter,
                    R.anim.pop_exit
                )
                .replace<ManagerFragment1>(R.id.transactionContainer)
                .addToBackStack(null)
                .setReorderingAllowed(true)
                .commit()
        }

    }

}