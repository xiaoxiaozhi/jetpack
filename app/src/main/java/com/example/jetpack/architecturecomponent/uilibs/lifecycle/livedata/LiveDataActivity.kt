package com.example.jetpack.architecturecomponent.uilibs.lifecycle.livedata

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityLiveDataBinding

/**
 * 1. 使用LiveData优势
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
 *    2.2 观察LiveData ，大多数情况下，APP组件的onCreate()方法是开始观察LiveData对象的正确位置，在这里LiveData和组建生命周期关联起来
 *    2.3 更新LiveData 查看下列代码
 *    2.4 LiveData与Room 一起使用 TODO
 *    2.5 LiveData与协程一起使用  查看代码 StockLiveData 类
 * 3. 应用架构中的LiveData
 *    3.1 参考 2
 *    3.2 LiveData 并不适合用于处理异步数据流，如果需要请使用kotlin Flow [LiveData和Flow的区别](https://www.jianshu.com/p/41a9fc2c7d97)
 * 4. 扩展LiveData 查看代码 StockLiveData
 * 5. 转换LiveData TODO
 * 6. 合并多个 LiveData 源 TODO
 *
 */
class LiveDataActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLiveDataBinding
    private val nameViewModel: NameViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //2.2 观察LiveData
        nameViewModel.currentName.observe(this, Observer {
            binding.text1.text = it
        })//注册号观察者后 LiveData会迅速传递最新值，如果设置的话
        //2.3 更新LiveData
        binding.button1.setOnClickListener {
            nameViewModel.currentName.value = "在主线程更新"
//            model.currentName.postValue("在工作线程更新")
        }
        //2.5 LiveData与协程一起使用
        binding.button2.setOnClickListener(this)
        nameViewModel.user.observe(this, Observer {
            println("user---${nameViewModel.user.value?.toString()}")
        })//订阅后执行


        //5. 转换LiveData
        //   5.1 Transformations.map 与集合的map类似
        val userLiveData: MutableLiveData<User> = MutableLiveData<User>()
        userLiveData.value = User("firstName", "lastName")
        val userName: LiveData<String> =
            Transformations.map(userLiveData) { user -> "${user.name}.${user.lastName}" }
        println(userName)
        //   5.2 Transformations.switchMap()
//        Transformations.switchMap(userLiveData){
//
//        }
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            addToBackStack("LiveDataFragment.kt")
            setReorderingAllowed(true)
            replace<LiveDataFragment>(R.id.liveDataContainer)
        }
        binding.liveDataContainer
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button2 -> startActivity(Intent(this, TestActivity::class.java))
        }
    }
}