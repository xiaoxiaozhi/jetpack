package com.example.jetpack.architecturecomponent.uilibs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityLiveDataBinding

/**
 * 1. LiveData只通知生命周期处于STARTED和RESUMED的观察者，这点非常有用，Activity和Fragment实现LifecycleOwner接口，
 *    当生命周期处于 DESTROYED.时移除观察者避免内存泄漏
 * 2. 如果生命周期变得不活跃，它将在再次激活时接收最新的数据,例如创建一个新的Fragment，新碎片会立即收到更新
 * 3. 使用 observeForever(Observer) 在这种情况下，观察者被认为总是处于活动状态，会一直更新数据。
 * 4. 确保将更新UI的LiveData对象存储在ViewModel对象中，而不是在活动或片段中，原因如下：
 *    ①避免Activity和Fragment代码膨胀。现在，这些UI控制器负责显示数据，但不保存数据状态
 *    ②将LiveData实例与特定的活动或片段实例分离，并允许LiveData对象在配置更改中生存下来。
 * 5. 在大多数情况下，APP组件的onCreate()方法是开始观察LiveData对象的正确位置
 * 6. 使用setValue()在主线程中更新LiveData，postValue()在子线程中更新
 * 7. 更多示例在 LiveDataFragment.kt
 */
class LiveDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLiveDataBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            addToBackStack("LiveDataFragment.kt")
            setReorderingAllowed(true)
            replace<LiveDataFragment>(R.id.liveDataContainer)
        }
        binding.liveDataContainer
    }
}