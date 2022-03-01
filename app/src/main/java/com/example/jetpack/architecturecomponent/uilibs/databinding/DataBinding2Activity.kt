package com.example.jetpack.architecturecomponent.uilibs.databinding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityDataBinding2Binding

/**
 * 1. 双向数据绑定
 *    1.1 @={} 表示法（其中重要的是包含“=”符号）可接收属性的数据更改并同时监听用户更新。
 *    1.2 使用自定义双向绑定@InverseBindingAdapter
 * 2. dataBinding与架构组件一起使用
 *    viewModel持有LiveData持有Student(其中的字段都是ObservableField类型)TODO 具体以后再补充
 *
 */
class DataBinding2Activity : AppCompatActivity() {
    val vm by lazy { viewModels<DataBindingViewModel>() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dataBinding = ActivityDataBinding2Binding.inflate(layoutInflater)
        setContentView(dataBinding.root)
        dataBinding.student = Student()
        dataBinding.student?.content?.set("456")
//        dataBinding.button1.setOnClickListener {
//            (it as MyButton).myName = "123"
//        }
        dataBinding.colorValue = 0
        //2.
        //databinding.vm = vm
        dataBinding.lifecycleOwner = this //databinding 和 架构组件一起使用需要关联生命周期
    }
}