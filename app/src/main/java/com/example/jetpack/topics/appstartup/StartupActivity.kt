package com.example.jetpack.topics.appstartup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.startup.AppInitializer
import com.example.jetpack.R

/**
 * [androidx中app.startup组件官方指南](https://blog.csdn.net/u011897062/article/details/108325889)
 * App Startup 提供了一个 ContentProvider 来运行所有依赖项的初始化，避免每个第三方库单独使用 ContentProvider 进行初始化，从而提高了应用的程序的启动速度。
 * Startup 初始化组件在 application 的onCreate之前
 * 1. 在build gradle 中引入依赖  implementation "androidx.startup:startup-runtime:1.1.1"
 * 2. 继承Initializer 接口 ，实现两个方法 create() 这里进行组件初始化工作。 dependencies(): 返回需要初始化的列表
 * 3. 在 AndroidManifest.xml 文件中注册 InitializationProvider。查看AndroidManifest.xml 里面的provider 标签 App 启动的时 App Startup 会读取 AndroidManifest.xml 文件里面的 InitializationProvider
 *    自动加载：下面的 <meta-data> 声明要初始化的组件，完成自动初始化工作。可以有多个<meta-data>标签
 *    手动加载：只需要在 <meta-data> 标签内添加 tools:node="remove"就可以禁用 meta标签下组件的初始化；如果要禁用 provider下所有组件初始化则 在<provider>标签里面使用属性  tools:node="remove"
 * note:一个项目中如果存在多个provider，会按照第一个执行忽略第二个 ;
 * note:tools:node="merge" 属性确保manifest的merger工具可以解决实体冲突
 */
class StartupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startp)
        //3. 手动初始化
//        AppInitializer.getInstance(this)
//            .initializeComponent(InitializerA::class.java)

    }
}