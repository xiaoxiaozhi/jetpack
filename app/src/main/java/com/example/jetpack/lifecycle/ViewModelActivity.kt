package com.example.jetpack.lifecycle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ConditionVariable
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.*
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityViewModelBinding
import kotlinx.coroutines.*

/**
 * 1. viewModel 在屏幕旋转情况下保存数据
 * 2. 两个Fragment之间通过viewModel共享数据
 * 3. LiveData只通知生命周期处于STARTED和RESUMED的观察者，这点非常有用，Activity和Fragment实现LifecycleOwner接口，
 *    当生命周期处于 DESTROYED.时移除观察者避免内存泄漏
 * 4. 如果生命周期变得不活跃，它将在再次激活时接收最新的数据,例如创建一个新的Fragment，新碎片会立即收到更新
 * 
 */
class ViewModelActivity : AppCompatActivity(), LifecycleOwner {
    private lateinit var binding: ActivityViewModelBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewModelBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val model: MyViewModel by viewModels()
        //----------------1.viewModel 在屏幕旋转情况下保存数据------------------------
        model.users.observe(this, Observer<String> {
            println("-------liveData change-------------${it}")
            binding.text1.setText(it)
        })
        binding.button1.setOnClickListener {
            GlobalScope.launch {
                val result = async<String> {
                    delay(2000)
                    "反转后显示"
                }
                runBlocking(Dispatchers.Main) {
                    model.users?.value = result.await()
                }
            }
//            println("----onClick----")
//            model.users.value = "反转后显示"
        }
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                println("Lifecycle---------------------${event}")
            }
        })
        //-----------------2.两个Fragment之间通过viewModel共享数据--------------------------
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            replace<ViewModelFragment>(R.id.viewModelContainer)
            addToBackStack("ViewModelFragment")
            setReorderingAllowed(true)
        }
    }
}