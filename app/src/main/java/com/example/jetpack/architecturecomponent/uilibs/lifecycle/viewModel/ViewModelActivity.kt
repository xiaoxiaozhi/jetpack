package com.example.jetpack.architecturecomponent.uilibs.lifecycle.viewModel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.*
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityViewModelBinding
import kotlinx.coroutines.*

/**
 * 1. 实现ViewModel
 *    viewModel 在屏幕旋转情况下保存数据
 *    ViewModel 绝不能引用视图、Lifecycle 或可能存储对 Activity 上下文的引用的任何类。
 *    如果有需要使用 AndroidViewModel 类并设置用于接收 Application 的构造函数，
 * 2. ViewModel生命周期
 *    ViewModel 对象存在的时间范围是获取 ViewModel 时传递给 ViewModelProvider 的 Lifecycle
 *    MyViewModel model = new ViewModelProvider(this).get(MyViewModel.class);
 *    ViewModel 将一直留在内存中，直到限定其存在时间范围的 Lifecycle 永久消失：对于 activity，是在 activity onDestroy()；
 *    而对于 fragment，是在 fragment 分离时。
 * 3. 两个Fragment之间通过viewModel共享数
 *    [这一节直接看更快](https://developer.android.google.cn/topic/libraries/architecture/viewmodel#sharing)
 * 4. 将加载器替换为ViewModel
 *    用ViewModel+LiveData+Room， 替换之前加载数据更新界面的方式
 * 5. 界面横竖屏切换或者Activity被系统回收，再次重启都将得到一个崭新的Activity。然而用户希望界面状态与之前的一致。
 *    使用ViewModel和使用 onSaveInstanceState() 保存界面状态。
 *    [ViewModel与onSaveInstanceState()的对比](https://developer.android.google.cn/topic/libraries/architecture/saving-states#options)
 *    由上面对比可知如果系统回收Activity，ViewModel就无法保存状态，使用 onSaveInstanceState() 作为后备方法来处理系统发起的进程终止
 * 6. ViewModel + SavedStateHandle
 *    查看 SavedStateViewModel 类
 * 7. 保存非 Parcelable 类
 *    TODO
 * 8. ViewModelScope 生命周期感知
 *    如果 ViewModel 已清除，则在此范围内启动的协程都会自动取消。
 *    viewModelScope.launch { }
 *
 */
class ViewModelActivity : AppCompatActivity(), LifecycleOwner {
    private lateinit var binding: ActivityViewModelBinding
    private val vm by viewModels<SavedStateViewModel>()
    private val model: MyViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewModelBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        vm.filteredData.observe(this, Observer<Int> {
            binding.display.text = it.toString()
        })
        binding.add.setOnClickListener {
            vm.add()
        }
        binding.del.setOnClickListener {
            vm.del()
        }
        //-----------------2.两个Fragment之间通过viewModel共享数据--------------------------
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            replace<ViewModelFragment>(R.id.viewModelContainer)
            addToBackStack("ViewModelFragment")
            setReorderingAllowed(true)
        }
    }
}