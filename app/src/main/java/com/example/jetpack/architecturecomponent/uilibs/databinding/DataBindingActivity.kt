package com.example.jetpack.architecturecomponent.uilibs.databinding

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityDataBindingBinding

/**
 * [Jetpack - DataBinding 学习 和 踩坑](https://juejin.cn/post/6946878518970023949)
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
 *    Note：绑定类 中定义的变量不会自己生成需要传进去
 *    TODO [在RecyclerView. 中使用dataBinding 没看懂](https://blog.csdn.net/zhangphil/article/details/77367432)
 * 3. 数据绑定表达式
 *    [表达式运算符](https://developer.android.google.cn/topic/libraries/data-binding/expressions#expression_language)
 *    ??运算符，效果等同于java的 ?:三元运算符
 *    控件引用：android:text="@{exampleText.text}" exampleText是控件id
 *    表达式有占位符功能，default 属性只在预览界面时有用android:text="@{student.firstName,default=默认值}"
 *    字符串格式化：查看布局文件查看id= text3的代码。在String资源中传入参数格式化
 *    note：两个属性拼接要通过字符串格式化的方式、字符串格式化传递的参数如果是"@{@string/collection(myMap[`sd`])}"或者 `@{@string/collection(myMap["sd"])}` 注意是`不是‘
 *          如果表达式结果null，dataBinding自动转化为字符null
 *    字符串拼接比较麻烦 android:text="@{@string/live_data_str(vm.dataInt.toString())}" 或者 @{`dsd`+vm.dataInt} 注意是反单引号 ` ` 实际表明返单引号报错
 * 4. 事件处理
 *    4.1 方法引用：在方法引用中，方法的参数必须与事件监听器的参数匹配，感觉不如监听器绑定灵活
 *    4.2 监听器绑定: 方法引用和监听器绑定主要区别在于，前者在编译时就已经绑定，后者在事件发生时才会创建并且绑定，后者只在乎返回值与监听器保持一致，lambda可以不设置参数
 *                  参数匹配不匹配没有关系例如 onclick的参数是 view， 我设定的lambda表达式
 *                  可以是android:onClick="@{()->handler.onClickBind(isTrue)}"    可以没有
 *                  也可以是android:onClick="@{(v)->handler.onClickBind(isTrue)}" 但是要设置参数就要和onClick参数一致
 * 5. 导入、变量、包含
 *    导入在布局文件中引用类： <import type="android.graphics.drawable.Drawable"/> 使用类型别名 <import type="com.example.real.estate.View" alias="Vista"/>
 *    变量
 *    包含 include  <include layout="@layout/name" bind:user="@{user}"/> 把顶层布局的变量绑定到底层include布局，user是子布局的变量名
 * 6. 使用可观察的数据对象 查看代码 Student
 *    当其中一个可观察数据对象绑定到界面并且该数据对象的属性发生更改时，界面会自动更新。通过继承Observable 接口实现可观察对象
 *    6.1 可观察字段
 *        有时候一个类只有一两个字段则可以直接 声明类型ObservableXXX的属性例如 ObservableBoolean、ObservableByte、ObservableParcelable......来避免继承通过继承Observable
 *        声明方式 val firstName = ObservableField<String>()
 *        可观察集合ObservableArrayMap<String, Any>(). put("firstName", "Google")
 *        6.1.1  在<data ><variable></data> 声明的变量是 可观察变量
 *    6.2 可观察对象
 *         note:BR类需要手动导入 import  包名.BR ；
 *         'dataBinding.XXX' is a mutable property that could have been changed by this
 *         dataBinding.student = Student() 之后直接操作 dataBinding.student.age 会报错，这是因为student在dataBinding中是一个可空类型。
 *         多线程修改时可能是一个空值，索要调用student属性的时候要加上安全调用 ?.  dataBinding.student?.age = 10
 *         可观察对象中 通过添加@get:Bindable 和  notifyPropertyChanged(BR.XXX) 把普通属性变成可观察属性
 *    6.3 使用LiveData
 *        dataBinding.lifecycleOwner = this//指定生命周期所有者，否则不会发生变化
 *        DataBinding变量--->普通变量--->修改             界面不变
 *        DataBinding变量--->可观察属性--->修改           界面改变
 *        DataBinding变量--->可观察对象--->修改           界面改变
 *        DataBinding变量--->LiveData变量--->修改        界面改变
 *    总结使用LiveData更加方便，可观察属性和可观察对象不便
 * 7. 绑定适配器
 *    7.1 @BindingMethods 指定方法名称
 *        [这篇文章讲解得很好](https://juejin.cn/post/6946878518970023949#heading-22)
 *        DataBinding 会自动在源码中尝试查找属性对应的 setter 方法。比如 android:text="" 对应的 setter 方法为 setText() 方法
 *       查找过程中 DataBinding 不会考虑属性的命名空间 不在意是android: 还是 app:
 *       对于具有不符合规则的 setter 方法的属性需要使用 @BindingMethods注解手动,例如 ImageView 的tint属性对应的是setImageTintList，这时候就需要自己添加绑定方法
 *       @BindingMethods(value = [
 *        BindingMethod(
 *            type = android.widget.ImageView::class,
 *            attribute = "android:tint", 换成 app:tint  实测发现android:tint不报错怀疑Databinding库已经给他添加上了，可以用app:tint验证该功能
 *            method = "setImageTintList")])
 *        不过大部分时候Databinding已经帮我们做好了，想要知道系统有没有给做，可以查看 TextViewBindingAdapter 等绑定类
 *    7.2 @BindingAdapter 给没有setter的属性提供方法
 *        某些属性需要自定义绑定逻辑。例如，android:paddingLeft 属性没有关联的setter，但是提供了setPadding(left, top, right, bottom)方法我们可以把它管理起来
 *        @BindingAdapter("android:paddingLeft")
 *        @JvmStatic   注意一定要静态方法
 *        fun setPaddingLeft(view: View, padding: Int) {
 *                 view.setPadding(padding,
 *                 view.getPaddingTop(),
 *                 view.getPaddingRight(),
 *                 view.getPaddingBottom())
 *        }
 *        7.1和7.2合并是否可以给自定义view的属性指定方法？？？或者给已存在View增加属性？？？
 *        给app空间设置属性 @BindingAdapter("imageUrl", "error")或者@BindingAdapter("app:imageUrl", "app:error")
 *    7.3 对象转换
 *        当绑定表达式的返回值类型和设置属性的方法参数不一致时，可以通过 @BindingConversion 注解自定义转换。
 *        比如android:background="@{isTrue?@string/gray:@string/blue}"background显然不能接受字符串，
 *        注意表达式里的值要保持类型一致， 实践发现 只要输入输出值类型一样，方法名无所谓，不知道为什么该方法不能写在DataBindingActivity
 *        如果写在这个Activity，DataBinding2Activity就会报错难道是半生类不是静态类？？？到底什么原因呢？
 *    上面例子在最下面，不管是class还是object 只要添加了@BindingMethods 就都起作用
 * 8. 双向绑定 查看 DataBinding2Activity
 *    例如在EditText  android:text="@={user.content}" 在TextView  android:text="@={user.content}"
 *    当EditText文字发生变化的时候TextView也会发生变化 (使用控件引用也能实现这个效果)
 * 9. DataBinding 与 架构组件一起使用
 *    addOnPropertyChangedCallback() 和 removeOnPropertyChangedCallback() 目前还没看到这么做有什么好处，等待日后发觉
 *    使用 Observable ViewModel 更好地控制绑定适配器，就是数据变化后 界面可能还需要一些其它条件参会变化，这是才需要用到
 *    查看 DataBinding2Activity TODO
 * 10.双向数据绑定
 *    Android 已为部分常用属性实现了双向数据绑定操作。比如 TextViewBindingAdapter。
 *    如果想对自定义属性添加双向数据绑定则需要 @InverseBindingAdapter 和 @InverseBindingMethod 注解实现
 *
 *
 *  TODO DataBinding layout  在Fragment中的使用
 */
class DataBindingActivity : AppCompatActivity() {
    private val viewModel: DataBindingViewModel by viewModels<DataBindingViewModel>()
    lateinit var dataBinding: ActivityDataBindingBinding
    var sdsada = "123244"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //2. 数据绑定类 加载布局的几种方式
        //2.1 知道要绑定的布局
//        val dataBinding = ActivityDataBindingBinding.inflate(layoutInflater)
//        setContentView(dataBinding.root)
        //2.2
//        val viewRoot = LayoutInflater.from(this).inflate(R.layout.activity_data_binding, parent, true)
//        val binding: MyLayoutBinding = ActivityDataBindingBinding.bind(viewRoot)
        //2.3
        dataBinding = DataBindingUtil.setContentView<ActivityDataBindingBinding>(
            this, R.layout.activity_data_binding
        )
        //2.4
        //在Fragment中使用DataBinding
        //val dataBinding = DataBindingUtil.inflate<FragmentBlankBinding>(layoutInflater, R.layout.fragment_blank, container, false);
        //或者
        //val dataBinding = ResultProfileBinding.inflate(inflater, container, false)
//        DataBindingUtil.bind(viewRoot)
        //2.5
//        DataBindingUtil.inflate<>()
        //6.1使用可观察的数据字段
        val firstName = ObservableField<String>()
        firstName.set("可观察字段")
        dataBinding.of = firstName
        //6.2使用可观察对象
        val st: Student = Student()
        dataBinding.student = st
        //6.3使用LiveData
        dataBinding.vm = viewModel
        dataBinding.lifecycleOwner = this//指定生命周期所有者


        dataBinding.myMap = mapOf("sd" to "321")
        dataBinding.handler = this
        dataBinding.arg1 = sdsada
        dataBinding.isTrue = false
        dataBinding.colorValue = resources.getColor(R.color.colorAccent)
        dataBinding.paddingLeft = 100

        //6.1 可观察属性
        dataBinding.button4.setOnClickListener {
            firstName.set("可观察字段+${System.currentTimeMillis()}")
            Log.i(TAG, "可观察字段 firstName---${firstName}")
        }
        //6.1 可观察对象
        dataBinding.button5.setOnClickListener {
            st.name = "可观察对象+${System.currentTimeMillis()}"
            Log.i(TAG, "可观察字段  st.name---${st.name}")
        }
    }

    //4.1事件处理---方法引用
    fun onClickFriend(view: View) {
        Log.i(TAG, "事件处理之方法引用")
        Toast.makeText(this,"事件处理之方法引用",Toast.LENGTH_SHORT).show()
    }

    //4.2事件处理---lambda表达式
    fun onClickBind(flag: Boolean) {
        Log.i(TAG, "事件处理监听器绑定($flag)")
        Toast.makeText(this,"事件处理监听器绑定($flag)",Toast.LENGTH_SHORT).show()
    }

    fun intentDataBinding2(view: View) {
        startActivity(Intent(this, DataBinding2Activity::class.java))
    }

    fun addValue() {
        viewModel.dataInt.value = viewModel.dataInt.value?.plus(1)
        Log.i(TAG, "dataInt.value---${viewModel.dataInt.value}")
    }

    //6. 绑定值引用普通值，普通值改变，绑定值改变吗？
    fun commonValue() {
        sdsada = "普通值---${System.currentTimeMillis()}"
    }

    //7.1
    @BindingMethods(
        value = [BindingMethod(
            type = ImageView::class, attribute = "app:tint", method = "setImageTintList"
        )]
    )
    companion object {
        const val TAG = "DataBindingActivity"

        //7.2
        @BindingAdapter("android:paddingLeft")
        @JvmStatic
        fun setPaddingLeft(view: View, padding: Int) {
            view.setPadding(
                padding, view.paddingTop, view.paddingRight, view.paddingBottom
            )
        }

        //7.2
        @BindingAdapter(value = ["imageUrl", "placeholder", "error"], requireAll = false)
        @JvmStatic
        fun loadImage(view: ImageView, url: String, placeholder: Drawable, error: Drawable) {
            //还能设置多个属性 ，所有属性都设置了 这个BindAdapter才起作用
        }

        //7.3
//        @BindingConversion
//        @JvmStatic
//        fun convertColorToDrawable(color: String): Drawable {
//            Log.i(Relation.TAG, "convertColorToDrawable------------------${color}")
//            return ColorDrawable(Color.parseColor(color))
//        }//note:查看编译后的代码 被转化成 @BindingAdapter("android:background")。相当于2.2第一个参数不用再传类型，在哪个控件上用
//        //      只要函数返回结果是 属性需要的就可以 //build/generated/source/kapt/debug/包名/databinding/不具名+BindingImpl
    }
}