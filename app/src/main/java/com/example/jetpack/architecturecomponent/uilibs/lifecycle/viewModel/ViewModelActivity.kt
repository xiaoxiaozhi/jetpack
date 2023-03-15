package com.example.jetpack.architecturecomponent.uilibs.lifecycle.viewModel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.example.jetpack.R
import com.example.jetpack.architecturecomponent.uilibs.lifecycle.viewModel.UserViewModel.Companion.provideFactory
import com.example.jetpack.databinding.ActivityViewModelBinding
import kotlinx.coroutines.*

/**
 * 最小版本
 * androidx.activity:activity-ktx:1.5.0
 * androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.0
 * 屏幕翻转Activity界面相关的状态都会重新丢失，对于简单的数据，activity 可以使用 onSaveInstanceState() 方法从 onCreate() 中的捆绑包恢复其数据，
 * 但此方法仅适合可以序列化再反序列化的少量数据，而不适合数量可能较大的数据，如用户列表或位图。另一方面界面相关的状态数据如果发生异步调用可能需要一段时间才能返回给界面，这时候销毁界面这些异步调用就会存在内存泄露的风险
 * 所以从界面控制器逻辑中分离出数据获取和保存责任就尤为重要，架构组件为界面控制器提供了 ViewModel 辅助程序类，该类负责为界面准备数据
 * 1. 实现ViewModel
 *    如果 ViewModel不接受任何依赖项(参数)，或只将 SavedStateHandle 类型作为依赖项，您便无需为框架提供工厂来实例化该 ViewModel 类型的实例
 *    CretionExtras创建具有依赖的实例
 *    attention：ViewModel 绝不能引用视图、Lifecycle 或可能存储对 Activity 上下文的引用的任何类。
 *    如果 ViewModel 需要 Application 上下文（例如，为了查找系统服务），它可以扩展 AndroidViewModel 类并设置接收 Application 的构造函数
 * 2. 创建具有参数的ViewModel
 *    如果 ViewModel 不接受任何依赖项，或只将 SavedStateHandle 类型作为依赖项，您只需用扩展方法 ComponentActivity.viewModels。创建VIewModel
 *    使用Hilt注入依赖项(参数)TODO
 *    使用ViewModelProvider.Factory创建带参数的ViewModel  ； ViewModelProvider.Factory接口有两种创建ViewModel的方法(点进去就可以看见这两种方法)
 *        lifecycle-viewmodel-ktx2.5 之前 create(modelClass: Class<T>)不推荐，这种方式要把参数通过构造函数传入，不通用，每个ViewModel都要创建一个单独 的Factory 查看代码 UserViewModel
 *        TODO AndroidViewModelFactory（如果需要 Application 类）。AbstractSavedStateViewModelFactory 遇到再总结吧，现在海看不出来这俩有什么用
 *        lifecycle-viewmodel-ktx2.5 之后 create(modelClass: Class<T>, extras: CreationExtras)推荐这种做法 使用CreationExtras 创建ViewModel，不需要通过构造函数传递参数
 *        CreationExtras创建ViewModel步骤①创建ViewModelProvider.Factory ② 创建 MutableCreationExtras 作用是持有参数 ③ 属性委托调用 扩展函数viewModels(MutableCreationExtras,factory)
 *        CreationExtras是一个封闭类他不能被继承，我们通过子类MutableCreationExtras 创建CreationExtras。 MutableCreationExtras的构造函数需要传入一个CreationExtras。
 *        通常从当前 Activity 或者 Fragment 中获取的，查看 getDefaultViewModelCreationExtras() 方法，发现它已经设置了 Application 以及 Intent中的Bundle getIntent().getExtras().
 *        最好将 ViewModel 工厂放置在其 ViewModel 文件中，以便获得更好的上下文、可读性并更容易发现。多个 ViewModel 共用依赖项时，可以使用同一个 ViewModel 工厂 查看代码CreationViewModel
 *        attention： extras[Key]中的key必须是同一个实例才能取出来。
 *        [CreationExtras创建ViewModel](https://blog.csdn.net/vitaviva/article/details/123321254)
 * 3. ViewModel生命周期
 *    MyViewModel model = new ViewModelProvider(ViewModelStoreOwner).get(MyViewModel.class);
 *    ViewModel 对象存在的时间范围是获取 ViewModel 时传递给 ViewModelProvider 的 ViewModelStoreOwner(AppCompatActivity、Fragment是他的子类)
 *    ViewModel 将一直留在内存中，直到限定其存在时间范围的 Lifecycle 永久消失：对于 activity是在onDestroy()，对于fragment是在fragment分离时。这时候会调用ViewModel的 onCleared()方法清除数据
 *    [特例viewModel在屏幕旋转情况下(activity调用onDestroy)不会调用onCleared销毁数据](https://blog.csdn.net/jackzhouyu/article/details/109031202)
 *    attention:需要注意的是，无法从ViewModel中获取绑定组件的lifecycle，viewModel没有继承LifecycleOwner 它怎么就是生命周期感知组件了？？？
 *    attention:想要感知组组件的生命周期 需要DefaultLifecycleObserver 和 LifecycleOwner配合，也就是说，自定义前者，遇到实现了 LifecycleOwner的组件，lifecycle.addObserver(MyObserver())
 * 4. ViewModel的作用域
 *    4.1 限定作用域为当前组件(AppCompatActivity、Fragment、Navigation )调用viewModels() 扩展函数，点击去可以发现源码把当前组件传递进去
 *    4.2 限定作用域为指定组件
 *        官网说ComponentActivity.viewModels()也可以接收指定的 ViewModelStoreOwner，但是我查了源码并没有 这个参数。也没有给出这样的例子
 *        Fragment扩展函数viewModels指定Fragment：viewModels(ownerProducer = { requireParentFragment() })
 *        Fragment扩展函数viewModels指定AppCompatActivity：val viewModel: SharedViewModel by activityViewModels()
 *        Fragment扩展函数viewModels指定Navigation：navGraphViewModels(R.id.nav_graph) 或者 viewModels({ findNavController().getBackStackEntry(R.id.nav_graph) })
 *
 * 5. 两个Fragment之间通过viewModel共享数
 *    这两个fragment指定父activity作为作用域来处理此类通信，val viewModel: SharedViewModel by activityViewModels()
 *----------------------------------------------------------------------------------------------------------------------
 * 6. 将加载器替换为ViewModel
 *    用ViewModel+LiveData+Room， 替换之前加载数据更新界面的方式
 * 7. 界面横竖屏切换或者Activity被系统回收，再次重启都将得到一个崭新的Activity。然而用户希望界面状态与之前的一致。
 *    使用ViewModel和使用 onSaveInstanceState() 保存界面状态。
 *    [ViewModel与onSaveInstanceState()的对比](https://developer.android.google.cn/topic/libraries/architecture/saving-states#options)
 *    由上面对比可知如果系统回收Activity，ViewModel就无法保存状态，使用 onSaveInstanceState() 作为后备方法来处理系统发起的进程终止
 * 8. ViewModel + SavedStateHandle
 *    查看 SavedStateViewModel 类
 * 9. 保存非 Parcelable 类
 *    TODO
 * 10.ViewModelScope 生命周期感知
 *    ViewModel定义了扩展函数viewModelScope如果 ViewModel 已清除，则在此范围内启动的协程都会自动取消。
 *    vm.viewModelScope.launch { }
 */
class ViewModelActivity : AppCompatActivity() {
    //1.无参数ViewModel
    val s = ViewModelProvider(this).get(MyViewModel::class.java)
    private val model: MyViewModel by viewModels()//无参数ViewModel

    //2.创建具有参数的ViewModel
    private val vm by viewModels<SavedStateViewModel>()//参数是SavedStateHandle的ViewModel,要加入泛型才可以 TODO 有什么不一样呢
    private val userViewModel: UserViewModel by viewModels {//2.5之前使用ViewModelProvider.Factory创建带参数的ViewModel
//        UserViewModel.provideFactory("123")
        UserViewModel.factory("456")
    }
    private val creationViewModel: CreationViewModel by viewModels(extrasProducer = {
        MutableCreationExtras(defaultViewModelCreationExtras).apply {
            set(CreationViewModel.STRING_KEY, "1234")// 如果这里的Key和ViewModel不是同一个实例，那么这里存入的1234将在ViewModel中取不出来
        }
    }, factoryProducer = { CreationViewModel.factory })//2.5 之后用 CreationExtras创建ViewModel

    // private val viewModel: MyViewModel by viewModels { MyViewModel.Factory } //2.5 之后用 CreationExtras创建ViewModel，参数 是application或者SavedStateHandle，只传Factory就可以
    private lateinit var binding: ActivityViewModelBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewModelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println("2.5之前-------${userViewModel.type}")
        println("2.5之后-------${creationViewModel.name}")

        println("ViewModelActivity-----${this.hashCode()}")
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
        lifecycle.addObserver(LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
            println("Lifecycle---------------------${event}")
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
        //10
        vm.viewModelScope.launch {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}