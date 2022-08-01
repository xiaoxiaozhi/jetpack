package com.example.jetpack.topics.image

import android.graphics.drawable.TransitionDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityImageBinding

/**
 * 1. 可绘制对象drawable
 *    Drawable 是可绘制对象的常规抽象,使用 Drawable 显示静态图片，绘制形状和图片.
 *    1.1 创建Drawable
 *    除了类构造函数创建Drawable之外，还可以使用以下两种方式
 *    1.1.1项目中的图片资源（位图文件) 支持的文件类型包括 PNG（首选）、JPG（可接受）和 GIF（不推荐）这种方法非常适合添加应用图标、徽标和其他图形（例如游戏中使用的图形）
 *         res/drawable/ 目录下的图片资源可由 aapt 工具在构建过程中自动完成无损图片压缩优化。如果您打算将某张图片作为比特流进行读取以将其转换为位图，请改为将图片放在 res/raw/ 文件夹下，这样的话，aapt 工具便无法对其进行修改。
 *         通过资源id引用创建 val myImage: Drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.my_image, null) 或者 android:src="@drawable/my_image"
 *    1.1.2可绘制属性的 XML 资源
 *         在 XML 中定义 Drawable 之后，将文件保存在项目的 res/drawable/ 目录下 例如 expand_collapse.xml 定义了 TransitionDrawable
 *    建议使用 [矢量可绘制对象]它通过一组点、线条和曲线以及相关颜色信息定义图片。这样一来，就可以在不降低质量的情况下将矢量可绘制对象缩放为不同的尺寸。
 *
 *  2. VectorDrawable 矢量可绘制对象
 *     在 XML 文件中定义为一组点、线条和曲线及其相关颜色信息。使用矢量可绘制对象的主要优势在于图片可缩放。您可以在不降低显示质量的情况下缩放图片，
 *     也就是说，可以针对不同的屏幕密度调整同一文件的大小，而不会降低图片质量
 * 使用Vector Asset Studio 创建矢量图，在project 窗口右键 new 选择Vector Asset。选择Clip Art 就是使用Material Icon ；选择Local File 就是使用 svg，psd
 * 在布局中添加VectorDrawable android:src="@drawable/ic_build_black_24dp" 在代码中添加 resources.getDrawable(R.drawable.myimage, theme)
 * Vector Asset Studio修改矢量图 不建议这么做
 * note： Image Asset 作用是创建应用启动图、通知图标、操作栏和标签页图标
 * [矢量图动画](https://developer.android.google.cn/guide/topics/graphics/drawable-animation)
 * 建议您将矢量图片限制为最大 200 x 200 dp；否则，绘制所需的时间可能会太长。
 * Android Studio使用 Vector Asset Studio 还可以将 SVG 、PSD文件转换为矢量可绘制对象格式。Android 4.4（API 级别 20）及更低版本不支持矢量可绘制对象，如果在低版本中使用Vector Asset Studio为了兼容不同分辨率会生成位图
 */
class ImageActivity : AppCompatActivity() {
    lateinit var binding: ActivityImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.image.apply {
            val transient = getDrawable(R.drawable.expand_collapse) as TransitionDrawable
            setImageDrawable(transient)
            transient.startTransition(1000)
        }
    }
}