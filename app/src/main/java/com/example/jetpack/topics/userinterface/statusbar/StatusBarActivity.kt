package com.example.jetpack.topics.userinterface.statusbar

import android.graphics.Insets
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import androidx.core.view.*
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityStatusBarBinding

/**
 * 实现边到边显示App内容
 * 1. 全屏显示app
 *    WindowCompat.setDecorFitsSystemWindows(window, false)；App的内容在系统栏和导航栏下面绘制
 * 2. 更改系统栏颜色(状态栏、导航栏)
 *    具体代码查看 @style/StatusBarActivityTheme
 *    android：enforceNavigationBarContrast 在设置导航栏为透明的情况下管用。设置为true 就不会有完全透明，泛白。false 完全透明
 *    android：enforceStatusBarContrast 在设置状态栏为透明的情况下管用。设置为true 就不会有完全透明，泛白。false 完全透明
 *    代码设置
 *    val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)
 *    windowInsetsController?.isAppearanceLightStatusBars = true
 *    windowInsetsController?.isAppearanceLightNavigationBars = true
 * 3. 使用insets处理重叠
 *    例如防止浮动按钮被系统栏遮住
 * 4. 沉浸模式
 *    隐藏系统栏+全屏显示app
 *    有三种系统栏从隐藏到显示交互模式
 *    WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH 默认模式，滑动强制显示系统栏，不会消失
 *    WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE 从屏幕上下边缘滑动临时显示系统栏
 *    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE 从屏幕上下边缘滑动临时显示系统栏，几秒种后小时
 * 5. 刘海屏
 * 6. 控制软键盘
 *
 * Insets 偏移量
 * InsetDrawable
 * WindowInsets 系统窗口的偏移量，insets.top代表状态栏的高度，insets.bottom 代表导航栏高度
 */
class StatusBarActivity : AppCompatActivity() {
    lateinit var binding: ActivityStatusBarBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatusBarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        //3. 使用insets处理重叠
        ViewCompat.setOnApplyWindowInsetsListener(binding.floating) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view. Here the system is setting
            // only the bottom, left, and right dimensions, but apply whichever insets are
            // appropriate to your layout. You can also update the view padding
            // if that's more appropriate.
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }
            // Return CONSUMED if you don't want want the window insets to keep being passed down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
        //4. 沉浸模式
        val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)
        windowInsetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
        binding.button1.setOnClickListener {
            // Show the system bars.

            windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
        binding.button2.setOnClickListener {
            // Hide the system bars.
            windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
//            windowInsetsController?.hide(WindowInsetsCompat.Type.navigationBars())//只隐藏导航栏
//            windowInsetsController?.hide(WindowInsetsCompat.Type.statusBars())//只隐藏状态栏
        }
    }
}