package com.example.jetpack.architecturecomponent.uilibs

import android.icu.math.BigDecimal
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import com.example.jetpack.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * 1. 对LiveData映射
 * 2. 根据LiveData依赖其它的LiveData获取数据，Transformations.switchMap
 * 3. MediatorLiveData 当我们页面需要多个不同的数据源的时候，如果我们都是单独的使用LiveData，会导致Activity中定义很多observe，
 *    出现很多多余的代码。MediatorLiveData就为解决这个问题的。它可以将多个LiveData合并在一起，只需要定义一次observe就可以。
 * 4. TODO 协程 flow
 */
class LiveDataFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_live_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //参数viewLifecycleOwner意味着观察者，与碎片生命周期绑定在一起
        StockLiveData.get("")
            .observe(viewLifecycleOwner, Observer<BigDecimal> { price: BigDecimal? ->
                // Update the UI.
            })
        //-------------------1.对LiveData映射-------------------------
        val liveData1: MutableLiveData<String> = MutableLiveData()
        val liveData2: LiveData<Int> = Transformations.map(liveData1) {
            println("liveData2----Transformations----$it")//懒计算，没有观察者情况下，转换不会被执行，
            it.length
        }
        liveData1.observe(viewLifecycleOwner, Observer {
            println("liveData1.observe()----${it}")
        })
        liveData2.observe(viewLifecycleOwner, Observer {
            println("liveData2.observe()----${it}")
        })
        liveData1.value = "liveData1"
        println("----${liveData2.value}----")//value1 赋值之后，观察者异步显示

        //-------------------2.根据LiveData1的值切换想要的LiveData，Transformations.switchMap-------------------------
        val liveData3: MutableLiveData<String> = MutableLiveData()
        val liveData4: MutableLiveData<String> = MutableLiveData()
        val liveDataSwitch: MutableLiveData<Boolean> = MutableLiveData()
        //
        val liveDataSwitchMap: LiveData<String> = Transformations.switchMap(liveDataSwitch) {
            if (it) liveData3 else liveData4
        }
        liveDataSwitchMap.observe(viewLifecycleOwner, Observer {
            println("liveDataSwitchMap-----$it")
        })

        liveDataSwitch.value = true
        liveData3.value = "true---value3"
        liveData4.value = "true---value4"

        liveDataSwitch.value = false
        liveData3.value = "false---value3"
        liveData4.value = "false---value4"
        //-------------------3.合并LiveData-------------------------
        val data1: MutableLiveData<String> = MutableLiveData()
        val data2: MutableLiveData<String> = MutableLiveData()
        val mediatorLiveData: MediatorLiveData<String> = MediatorLiveData<String>()
        mediatorLiveData.addSource(data1, Observer {
            println("addSource data1----${it}")
            mediatorLiveData.value = it// 调用这个通知mediatorLiveData更新数据
        })
        mediatorLiveData.addSource(data2, Observer {
            println("addSource data2----${it}")
            mediatorLiveData.value = it
        })
        mediatorLiveData.observe(viewLifecycleOwner, Observer {
            println("observe----${it}")
        })
        data1.postValue("data1")
        data2.value = "data2"

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LiveDataFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LiveDataFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}