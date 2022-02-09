package com.example.jetpack.architecturecomponent.uilibs.lifecycle.livedata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Observer
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityLiveDataBinding

/**
 * 1. 使用LiveData优势  TODO 怎么和生命周期关联起来的？？？
 *    LiveData只通知生命周期处于STARTED和RESUMED的观察者，(ON_CREATE ON_RESUME ON_PAUSE)这点非常有用，Activity和Fragment实现LifecycleOwner接口，
 *    当生命周期处于 DESTROYED.时移除观察者避免内存泄漏
 *    1.1 确保界面符合数据状态
 *    1.2 不会发生内存泄漏
 *    1.3 不会因 Activity 停止而导致崩溃
 *    1.4 不再需要手动处理生命周期
 *    1.5 数据始终保持最新状态
 *    1.6 适当的配置更改:如果由于配置更改（如设备旋转）而重新创建了 Activity 或 Fragment，它会立即接收最新的可用数据。
 *    1.7 共享资源
 * 2. 使用LiveData  TODO observeForever(Observer) 探究这个用法
 *    2.1 创建LiveData  参考 NameViewModel 类 请确保用于更新界面的 LiveData 对象存储在 ViewModel 对象中，
 *    而不是将其存储在 Activity 或 Fragment 中，原因如下：
 *    避免 Activity 和 Fragment 过于庞大。现在，这些界面控制器负责显示数据，但不负责存储数据状态。
 *    将 LiveData 实例与特定的 Activity 或 Fragment 实例分离开，并使 LiveData 对象在配置更改后继续存在。
 *    2.2 观察LiveData ，大多数情况下，APP组件的onCreate()方法是开始观察LiveData对象的正确位置
 *    2.3 更新LiveData 查看下列代码
 *    2.4 LiveData与Room 一起使用 TODO
 *    2.5 LiveData与协程一起使用 TODO
 * 3. 应用架构中的LiveData
 *    3.1 参考 2
 *    3.2 LiveData 并不适合用于处理异步数据流，如果需要请使用kotlin Flow
 * 4. 扩展LiveData 查看代码 StockLiveData
 * 5. 转换LiveData
 *
 *
 *
 * 2. 如果生命周期变得不活跃，它将在再次激活时接收最新的数据,例如创建一个新的Fragment，新碎片会立即收到更新
 * 3. 使用 observeForever(Observer) 在这种情况下，观察者被认为总是处于活动状态，会一直更新数据。
 * 4. 确保将更新UI的LiveData对象存储在ViewModel对象中，而不是在活动或片段中，原因如下：
 *    ①避免Activity和Fragment代码膨胀。现在，这些UI控制器负责显示数据，但不保存数据状态
 *    ②将LiveData实例与特定的活动或片段实例分离，并允许LiveData对象在配置更改中生存下来。
 * 7. 更多示例在 LiveDataFragment.kt
 */
class LiveDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLiveDataBinding
    private val model: NameViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //2.2 观察LiveData
        model.currentName.observe(this, Observer {
            binding.text1.text = it
        })//注册号观察者后 LiveData会迅速传递最新值，如果设置的话
        //2.3 更新LiveData
        binding.button1.setOnClickListener {
            model.currentName.value = "在主线程更新"
//            model.currentName.postValue("在工作线程更新")
        }

        //5. 转换LiveData


        supportFragmentManager.commit {
            setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            addToBackStack("LiveDataFragment.kt")
            setReorderingAllowed(true)
            replace<LiveDataFragment>(R.id.liveDataContainer)
        }
        binding.liveDataContainer
    }
}