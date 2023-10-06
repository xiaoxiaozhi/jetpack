package com.example.jetpack.topics.anim

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.VectorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityAnimatedVectorBinding
import kotlin.math.hypot

/**
 *矢量图动画
 * 1. 为Drawable添加动画
 *    1.1 AnimationDrawable
 *        这是一种传统动画，使用一系列不同的图片创建而成，然后像一卷胶卷一样按顺序播放。用AnimationDrawable类 API 比较麻烦建议在xml中定义
 *        Android 项目的 res/drawable/ 目录中。创建 <animation-list>
 *        note:在onCreate 中调用start动画不能执行 因为控件尚未加载到窗口
 *    1.2 AnimatedVectorDrawable
 *        一个矢量图动画需要三种文件：矢量图、 矢量动画<animated-vector>、动画<objectAnimator>；
 *        <animated-vector> 中的target标签可以为 <group> 和 <path> 元素的属性添加动画效果 target的name属性要和 group和path的name属性一致
 *        在res/drawable xml标签<animated-vector>
 * 2. 揭露动画
 *    暂时放在这里.只能使用一次，每次使用都要重新生成
 * <vector>中的<group>主要就是控制 作用中心坐标、旋转、缩放、位移等动画 。例如pivotX，pivotY 表示横坐标和纵坐标。
 * [vector标签属性](https://developer.android.google.cn/reference/android/graphics/drawable/VectorDrawable?hl=en)
 */
class AnimatedVectorActivity : AppCompatActivity() {
    lateinit var binding: ActivityAnimatedVectorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimatedVectorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //1.1 AnimationDrawable
        binding.image1.setOnClickListener {
            (it.background as AnimationDrawable).start()//
        }
        //1.2
        binding.button1.setOnClickListener {
            (it.background as AnimatedVectorDrawable).start()
        }

        //2. 揭露动画
        binding.button2.setOnClickListener {
            val pair = Pair(it.width / 2, it.height / 2)
            println("$pair}")
            ViewAnimationUtils.createCircularReveal(
                it, pair.first, pair.second, hypot(pair.first.toDouble(), pair.second.toDouble()).toFloat(), 0f
            ).apply {
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        it.visibility = View.INVISIBLE //隐藏图形
                    }

//                    override fun onAnimationStart(animation: Animator?) {
//                        super.onAnimationStart(animation)
//                        it.visibility = View.VISIBLE //显示图形
//                    }
                })
                duration = 500
                start()
            }
        }
    }
}