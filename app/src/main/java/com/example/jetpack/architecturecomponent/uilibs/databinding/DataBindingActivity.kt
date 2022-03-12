package com.example.jetpack.architecturecomponent.uilibs.databinding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityDataBindingBinding
import com.example.jetpack.databinding.FragmentBlankBinding

/**
 * TODO kotlin能直接通过id引用控件而不再需要findViewById(),为什么还要ViewBinding呢，我认为这些databinding和viewbinding更适合java、不支持中文、两个属性拼接要通过字符串格式化的方式。
 * TODO 感觉用处不大
 * 1. 增加DataBinding支持
 *    在app/build.gradle 中增加对databinding的支持
 *    android {
 *          dataBinding {
 *                enabled = true
 *                      }
 *            }
 * 2. 数据绑定类
 *    数据绑定布局文件略有不同，以根标记 layout 开头，后跟 data 元素和 view 根元素 查看 R.layout.activity_data_binding
 *    系统会为每个布局文件生成一个绑定类,以布局文件名开头+Binding结尾 例如 ActivityDataBindingBinding,也可以在data标签中设置  例如<data class="MyDataBinding">
 *    只要绑定值发生更改，生成的绑定类就必须使用绑定表达式在视图上调用 setter 方法，例如对于名为 example 的属性，DataBinding自动尝试查找接受该属性参数的方法 setExample(arg)
 *    如果属性没有找到set方法或者有set没有属性也可以指定方法或者属性
 *    2.1 指定自定义方法
 *        查看代码 Relation
 *    2.2 属性值转换
 *        查看代码Relation
 *
 *    Note：绑定类 中定义的变量不会自己生成需要传进去
 *    TODO [在RecyclerView. 中使用dataBinding 没看懂](https://blog.csdn.net/zhangphil/article/details/77367432)
 *    2.1
 * 3. 数据绑定表达式
 *    [表达式运算符](https://developer.android.google.cn/topic/libraries/data-binding/expressions#expression_language)
 *    ??运算符，效果等同于java的 ?:三元运算符
 *    控件引用：android:text="@{exampleText.text}" exampleText是控件id
 *    表达式有占位符功能，default 属性只在预览界面时有用android:text="@{student.firstName,default=默认值}"
 *    字符串格式化：查看布局文件查看id= text3的代码。在String资源中传入参数格式化
 *    note：两个属性拼接要通过字符串格式化的方式、字符串格式化传递的参数如果是"@{@string/collection(myMap[`sd`])}"或者 `@{@string/collection(myMap["sd"])}` 注意是`不是‘
 *          如果表达式结果null，dataBinding自动转化为字符null
 * 4. 事件处理
 *    4.1 方法引用：类::onDiyClick(View) 根据android的架构说明，点击界面产生的事件操作最好放置在ViewModel里面
 *    4.2 lambda表达式方式绑定监听器：用的最多的 点击事件放在viewModel里面
 * 5. 导入、变量、包含
 *    导入在布局文件中引用类： <import type="android.graphics.drawable.Drawable"/> 使用类型别名 <import type="com.example.real.estate.View" alias="Vista"/>
 *    变量
 *    包含 include  <include layout="@layout/name" bind:user="@{user}"/> 把顶层布局的变量绑定到底层include布局
 * 6. 单向绑定  查看代码 Student
 *    当其中一个可观察数据对象绑定到界面并且该数据对象的属性发生更改时，界面会自动更新。通过继承Observable 接口实现可观察对象
 *    6.1 可观察字段
 *        有时候一个类只有一两个字段则可以直接 声明类型ObservableXXX的属性例如 ObservableBoolean、ObservableByte、ObservableParcelable......来避免继承通过继承Observable
 *        声明方式 val firstName = ObservableField<String>()
 *        可观察集合ObservableArrayMap<String, Any>(). put("firstName", "Google")
 *        6.1.1  在<data ><variable></data> 声明的变量是 可观察变量
 *
 *    6.2 可观察对象
 *    note:BR类需要手动导入 import  包名.BR ；
 *         'dataBinding.XXX' is a mutable property that could have been changed by this
 *         dataBinding.student = Student() 之后直接操作 dataBinding.student.age 会报错，这是因为student在dataBinding中是一个可空类型。
 *         多线程修改时可能是一个空值，索要调用student属性的时候要加上安全调用 ?.  dataBinding.student?.age = 10
 *    6.3
 * 7. 双向绑定 查看 DataBinding2Activity
 *    例如在EditText  android:text="@={user.content}" 在TextView  android:text="@={user.content}"
 *    当EditText文字发生变化的时候TextView也会发生变化 (使用控件引用也能实现这个效果)
 * 8. DataBinding 与 架构组件一起使用
 *    查看 DataBinding2Activity TODO
 */
class DataBindingActivity : AppCompatActivity() {
    private val viewModel: DataBindingViewModel by viewModels<DataBindingViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //2. 数据绑定类 加载布局的几种方式
        //2.1 知道要绑定的布局
//        val dataBinding = ActivityDataBindingBinding.inflate(layoutInflater)
//        setContentView(dataBinding.root)
        //2.2
//        val binding: MyLayoutBinding = ActivityDataBindingBinding.bind(viewRoot)
        //2.3
        val dataBinding = DataBindingUtil.setContentView<ActivityDataBindingBinding>(
            this,
            R.layout.activity_data_binding
        )
        //2.4
        //在Fragment中使用DataBinding
        //val dataBinding = DataBindingUtil.inflate<FragmentBlankBinding>(layoutInflater, R.layout.fragment_blank, container, false);
        //7.
        dataBinding.lifecycleOwner = this
        //2.4
//        DataBindingUtil.bind(viewRoot)
        //2.5
//        DataBindingUtil.inflate<>()
        dataBinding.myMap = mapOf("sd" to "321")
        dataBinding.vm = viewModel
        dataBinding.arg1 = "include"
        val st = Student()
//        dataBinding.student = st
        dataBinding.student = Student()
        dataBinding.student?.photo?.set(R.drawable.dog) //student 的属性操作的时候一定要用安全调用 ?. 否则会报错
        dataBinding.button4.setOnClickListener {
            dataBinding.student?.age = 10
        }
        dataBinding.button5.setOnClickListener {
            dataBinding.student?.name = "sd"
        }
        dataBinding.button6.setOnClickListener {
            dataBinding.student?.height?.set(190)
            println("${dataBinding.student?.height?.get()}---------------------------------")
        }
    }


}