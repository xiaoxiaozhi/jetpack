package com.example.jetpack.lifecycle

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import com.example.jetpack.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * 从TransactionActivity进入
 * https://weixin.sogou.com/link?url=dn9a_-gY295K0Rci_xozVXfdMkSQTLW6cwJThYulHEtVjXrGTiVgS3Xx67fvY3sWeoJZArRzh5PWHfRPoiav5VqXa8Fplpd99Myuwc-fTwakTRkq2N8luBmUezwGu6xZSS276QaYFJVdxwYURW91e3iMDzjJ5Ol8934QhF1H-zfG0d9U4e9onr7WR7uJD2mFf-67_isiSq2UbXgIsENt94OktJSiit2J4ffACHEDFdqkDIkwTr2W89UMVxnTMs599w8nvYRIahBQ_LeJW-Rhtg..&type=2&query=Lifecycle&token=180ACF550353568C383CEB4DFF3F38B03923E6DC61AF5EE9&k=85&h=a
 * 生命周期
 * 1. Lifecycle使用两个主要枚举来跟踪其关联组件的生命周期状态：Event 和 State
 * 2. Event 类用于抽象 Activity/Fragment 的生命周期事件发生变化时所触发的事件，例如，当 Activity 的每个生命周期事件回调函数（onCreate、onStart 等）被触发时都会被抽象为相应的 ON_CREATE、ON_START 两个 Event。
 * 3. State 类用于标记 Lifecycle 的当前生命周期状态。
 * 4. 生命周期回调函数执行完毕，才会切换到下一个状态否则状态不变
 * 5. 生命周期像上移 先通知Fragment或者Activity的回调，像下移先通知生命周期注册者
 * 4. Event和State 对照看这张图片 https://developer.android.google.cn/images/topic/libraries/architecture/lifecycle-states.svg
 * 5. 自定义组建可以通过继承 DefaultLifecycleObserver和LifecycleEventObserver 的形式得到Activity和Fragment的生命周期
 * 6. 继承LifecycleOwner 获取生命周期  Lifecycle对象，通过该对象监控生命周期。
 * 7. getViewLifecycleOwner() 和  getViewLifecycleOwnerLiveData() 对于只在片段的视图存在时执行工作的组件非常有用
 * 8. 当我们有些类没有实现AppCompatActivity的时候我们也想使用Lifecycle，那么就需要自定义了，也就是需要我们自己实现LifecycleOwner接口。MyObserver类不变，MainActivity类改变如下：
 */
class LifeCycleFragment : Fragment(), LifecycleOwner {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        println("${this.javaClass.simpleName}-------onAttach()-------------${lifecycle.currentState}")

    }

    /**
     *savedInstanceState 调用的是onSaveInstanceState 保存的值
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("${this.javaClass.simpleName}------onCreate-------${lifecycle.currentState}")
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                println("observer------${source.lifecycle.currentState}----${lifecycle.currentState}--------Lifecycle.Event---${event}")
            }
        })
        lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                println("Observer-----onStart")
                GlobalScope.launch {
                    delay(2000)
                    println("-------${owner.lifecycle.currentState}-------")
                    if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        println("---------isAtLeast1-----")
                    } else {
                        println("---------isAtLeast2-----")
                    }
                    println("---------isAtLeast3-----")
                }

            }

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                println("Observer-----onStop")
            }
        })
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("${this.javaClass.simpleName}------onCreateView-------${lifecycle.currentState}")
        return inflater.inflate(R.layout.fragment_lifecycle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("${this.javaClass.simpleName}------onViewCreated-------${lifecycle.currentState}")
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        println("${this.javaClass.simpleName}------onViewStateRestored-------${lifecycle.currentState}")
    }

    override fun onStart() {
        super.onStart()
        println("${this.javaClass.simpleName}------onStart-------${lifecycle.currentState}")
    }

    override fun onResume() {
        super.onResume()
        println("${this.javaClass.simpleName}------onResume-------${lifecycle.currentState}")
    }

    override fun onPause() {
        super.onPause()
        println("${this.javaClass.simpleName}------onPause-------${lifecycle.currentState}")
    }

    override fun onStop() {
        super.onStop()
        println("${this.javaClass.simpleName}------onStop-------${lifecycle.currentState}")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        println("${this.javaClass.simpleName}------onSaveInstanceState-------${lifecycle.currentState}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        println("${this.javaClass.simpleName}------onDestroyView-------${lifecycle.currentState}")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("${this.javaClass.simpleName}------onDestroy-------${lifecycle.currentState}")
    }

    override fun onDetach() {
        super.onDetach()
        println("${this.javaClass.simpleName}-------onDetach()--------${lifecycle.currentState}")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LifeCycleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}