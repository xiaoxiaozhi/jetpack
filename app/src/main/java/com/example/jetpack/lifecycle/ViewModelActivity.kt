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
 * 2. 两个Fragment之间通过viewModel共享数
 * 3. 只有Activity真正Finish的时ViewModel才会被清除。(查看ViewModel生命周期图)
 * 4. 用于保存状态的组件必须实现SavedStateRegistr.SavedStateProvider，
 *    它定义了一个名为SaveState()的方法。SaveState()方法允许组件返回包含应该从该组件中保存的任何状态的Bundle。
 *    SavedStateRegistry在UI控制器生命周期的保存状态阶段调用此方法
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