package com.example.jetpack.bestpractice.dependencyinjection

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityDependencyInjectionBinding
import com.example.jetpack.topics.dependencyinjection.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * [谷歌开发者Hilt教程跟官网不同 Hilt 介绍 | MAD Skills](https://blog.csdn.net/jILRvRTrc/article/details/121677853)
 * [Hilt-郭霖 里面有一张mvvm的架构图](https://juejin.cn/post/6902009428633698312)
 * 继承Hilt需要更新Android studio 到Bumblebee。project/gradle插件到7.0.3 ;APP/build.gradle 添加 id 'kotlin-kapt' 依赖注解用 kapt否则会报错
 * 1. 什么是依赖注入?
 *    如果一个类必须依赖其他类才能运行，这些其他类称为依赖项。比如Car必须依赖Engine才能运行
 *    类可通过以下三种方式获取所需的对象：
 *    1.1 类构造其所需的依赖项。例如Car 将创建并初始化自己的 Engine 实例
 *    1.2. 从其他地方抓取。某些 Android API（如 Context getter 和 getSystemService()）的工作原理便是如此
 *    1.3. 以参数形式提供。在构造函数中通过参数传入依赖项，或者将这些依赖项传入需要各个依赖项的函数
 *    第三种方式就是依赖项注入！使用这种方法，您可以获取并提供类的依赖项，而不必让类实例自行获取。
 *    note:第一种方式有个问题，如果Car是电动引擎这就不得不再创造一个Car。对Engine的强依赖还造成了测试困难
 * 2. 自动依赖注入
 *    对于大型应用，获取所有依赖项并正确连接它们可能需要大量样板代码，难以管理。传入依赖项之前要等待依赖项构造。Dagger提供了静态编译时依赖项
 * 3. 添加Hilt
 *    3.1 将 hilt-android-gradle-plugin 插件添加到项目的根级 build.gradle 文件中 classpath 'com.google.dagger:hilt-android-gradle-plugin:2.28-alpha'
 *    3.2 在 app/build.gradle 文件中添加插件 apply plugin: 'dagger.hilt.android.plugin' 如果项目中是 id 来引入插件，则要替换成 id 'dagger.hilt.android.plugin'
 *        id和apply plugin:不能混用
 *    3.3 在app/build.gradle中添加依赖
 *        implementation "com.google.dagger:hilt-android:2.28-alpha"
 *        kapt "com.google.dagger:hilt-android-compiler:2.28-alpha" (如果build.gradle中已经使用annotationProcessor了)则通过annotationProcessor添加依赖
 *        kapt 和 annotationProcessor 不能混用
 *    3.4 Hilt 使用 Java 8 功能。如需在项目中启用 Java 8，请将以下代码添加到 app/build.gradle 文件中：
 *        android {
 *                   ...
 *                  compileOptions {
 *                                   sourceCompatibility JavaVersion.VERSION_1_8
 *                                   targetCompatibility JavaVersion.VERSION_1_8
 *                                 }
 *                  }
 * 4. 使用Hilt
 *    所有使用 Hilt 的应用都必须包含一个带有 @HiltAndroidApp 注释的 Application 类。
 *    @HiltAndroidApp 会触发 Hilt 的代码生成操作，生成的代码包括应用的一个基类，该基类充当应用级依赖项容器。 查看代码 MyApplication
 * 5. 将依赖注入到Android类
 *    Hilt 可以为带有 @AndroidEntryPoint 注释的类提供依赖项，Hilt目前支持的类Application（通过使用 @HiltAndroidApp）Activity、Fragment、View、Service、BroadcastReceiver
 *    如果您使用 @AndroidEntryPoint 为某个 Android 类添加注释，则还必须为依赖于该类的 Android 类添加注释。例如，如果您为某个 Fragment 添加注释，则还必须为使用该 Fragment 的所有 Activity 添加注释。
 *    note：Hilt 仅支持扩展 ComponentActivity 的 Activity，如 AppCompatActivity。Hilt 仅支持扩展 androidx.Fragment 的 Fragment。Hilt 不支持保留的 Fragment。
 *    注入一个依赖项时，需要在您希望注入的变量上添加 @Inject 注解,另外在MusicPlayer 的构造方法上添加@Inject 告诉Hilt怎么获取这个实例
 * 6. Hilt模块
 *    有时，类型不能通过构造函数注入，当类型是一个接口，您无法在构造函数上添加 @Inject；或者类来自于您无法修改的库。
 *    Hilt 模块是一个带有 @Module 注释的类。与 Dagger 模块一样，它会告知 Hilt 如何提供某些类型的实例，您必须使用 @InstallIn 为 Hilt 模块添加注释，以告知 Hilt 每个模块将用在或安装在哪个
 *    Android 类中。
 *    6.1 接口注入  查看代码 AnalyticsInterfaceModule
 *        @Binds 注释会告知 Hilt 在需要提供接口的实例时要使用哪种实现。会向Hilt提供如下信息
 *        - 函数返回类型会告知 Hilt 函数提供哪个接口的实例。
 *        - 函数参数会告知 Hilt 要提供哪种实现。
 *    6.2 第三方库注入 查看代码 AnalyticsProviderModule
 *        带有 @Provider注释的回告诉Hilt以下信息
 *        - 函数返回类型会告知 Hilt 函数提供哪个类型的实例。
 *        - 函数参数会告知 Hilt 相应类型的依赖项。
 *        - 函数主体会告知 Hilt 如何提供相应类型的实例。每当需要提供该类型的实例时，Hilt 都会执行函数主体。
 *        note:函数的返回类型是唯一的，如果有多个函数返回相同类型会报错
 *    6.3 给相同类型注入不同实例 查看代码 EngineModule
 *        对不同类型创建限定符(注解)，然后在方法上添加注解
 *    6.4 Hilt预定义限定符
 *        例如@ApplicationContext 和 @ActivityContext 限定符， @ActivityContext private val context: Context 表示全局的 context   还有 TODO 还有什么？
 * 7. Hilt内置组件和作用域 看英文版的，中文版过时
 *    7.1 [内置组件](https://developer.android.google.cn/training/dependency-injection/hilt-android?hl=en#generated-components)
 *        例如 @InstallIn(ActivityComponent::class)，就是把这个模块安装到 Activity 组件当中。如果我们再Service中使用就会报错
 *    7.2 [组件生命周期](https://developer.android.google.cn/training/dependency-injection/hilt-android?hl=en#component-lifetimes)
 *    7.3 [组件作用域](https://developer.android.google.cn/training/dependency-injection/hilt-android?hl=en#component-scopes)
 *        默认情况下，Hilt 中的所有绑定都未限定作用域，Hilt 允许将依赖注入到特定组件作用域，
 *        例如 Hilt 会为每次的依赖注入行为都创建不同的实例，有时会不合理比如在创建APPDataBase和OkHttp时只要一个实例就可以，为此添加 @Singleton 注解即可 查看代码 AnalyticsProviderModule
 *        例如 如果想要在某个 Activity，以及它内部包含的 Fragment 和 View 中共用某个对象的实例，那么就使用 @ActivityScoped。
 *        note:组件ApplicationComponent的作用域是@Singleton，实际测试仍然会生城实例，存疑？
 *    7.4 [组件层次结构](https://developer.android.google.cn/training/dependency-injection/hilt-android#component-hierarchy)
 *        将模块安装到组件后，其依赖，也可以用作组件层次结构中该组件下的任何子组件
 *
 * 8.不支持的入口点怎么依赖注入
 *    ContentProvider在application之前就已经初始化，所以Hilt不支持它，想要在里面实现依赖注入需要自己实现入口点 TODO 待看
 * 9. Hilt与Jetpack库一起使用
 *    9.1 ViewModel 查看代码 RoomActivity
 *    9.2 TODO 与导航库
 *    9.3 TODO 与WorkerManager
 *
 *
 * NOTE: Expected @HiltAndroidApp to have a value. Did you forget to apply the Gradle Plugin? 当出现这个错误时检查,项目是否也用了Room,如果有则要 arguments 后面=改成+= ["room.schemaLocation":"$projectDir/schemas".toString()]
 */
@AndroidEntryPoint
class HiltActivity : AppCompatActivity() {
    @Inject
    lateinit var analytics: AnalyticsInterfaceAdapter//由 Hilt 注入的字段不能为私有字段。尝试使用 Hilt 注入私有字段会导致编译错误。

    @Inject
    lateinit var provider: AnalyticsProviderAdapter// 第三方库注入

    @Inject
    @BindGasEngine
    lateinit var gasEngine: Engine

    @Inject
    @BindElectricEngine
    lateinit var electricEngine: Engine
    lateinit var binding: ActivityDependencyInjectionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDependencyInjectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //6.1 接口依赖注入
        println("调用--------------------------")
        analytics.hash()
        //6.2 第三方库依赖注入
        provider.data()
        //6.3 给相同类型注入不同实例
        gasEngine.start()
        electricEngine.start()
        binding.button1.setOnClickListener {
            startActivity(Intent(this, Hilt2Activity::class.java))
        }

    }
}