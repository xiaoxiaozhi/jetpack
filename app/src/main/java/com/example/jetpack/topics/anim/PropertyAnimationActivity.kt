package com.example.jetpack.topics.anim

import android.animation.*
import android.graphics.PointF
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.Gravity

import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.jetpack.databinding.ActivityPropertyAnimationBinding

/**
 * [md动画设计准则](https://material.io/design/motion/understanding-motion.html#principles)
 * 1. 为Drawable添加动画
 *    1.1 AnimationDrawable
 *        这是一种传统动画，使用一系列不同的图片创建而成，然后像一卷胶卷一样按顺序播放。用AnimationDrawable类 API 比较麻烦建议在xml中定义
 *        Android 项目的 res/drawable/ 目录中。创建 <animation-list>
 *        note:在onCreate 中调用start动画不能执行 因为控件尚未加载到窗口
 *    1.2 AnimatedVectorDrawable
 *        <animated-vector>
 * 2. 布局和属性动画
 *    使用ValueAnimator为动画播放期间某些类型的值添加动画效果 工厂方法 ofFloat ofInt ofArgb 自带相应求值器，使用ofObject 需要自己自定义求值器 在xml中的标签是<animator>
 *    插值器：如何根据时间计算动画中的特定值 [系统定义的插值器](https://developer.android.google.cn/guide/topics/graphics/prop-animation#property-vs-view)
 *    求值器：属性动画系统如何计算指定属性的值 系统提供的求值器 IntEvaluator、FloatEvaluator、ArgbEvaluator(用于计算颜色) TypeEvaluator(如果要计算的值不是Int Float和颜色，那就要实现这个类自定义求值器)
 *           ArgbEvaluator, FloatArrayEvaluator, FloatEvaluator, IntArrayEvaluator, IntEvaluator, PointFEvaluator, RectEvaluator
 *   repeatCount和repeatMode 相互配合实现动画重复功能 repeatCount>0 repeatMode = RESTART 执行完从开头再次执行； repeatCount>0 repeatMode = REVERSE 执行完从结尾反向执行
 *   2.1 ObjectAnimator
 *        ObjectAnimator是ValueAnimator的子类简化为对象添加动画效果的过程，要添加动画效果的对象属性必须是setPropertyName()形式的函数，例如属性foo则需要使用 setFoo() 函数
 *        如果setPropertyName() 方法不存在，您有两个选择：自定义setPropertyName() 、 改用 ValueAnimator
 *        ObjectAnimator.ofFloat(targetObject, "propName", 1f) 工厂方法中仅有一个 values，则系统会假定它是动画的结束值。要添加动画效果的对象属性必须具有用于获取动画起始值的 getter 函数
 *        在xml文件中的标签是<objectAnimator>
 *        note: 为自定义属性添加动画的时候要在 addUpdateListener调用invalidate()让视图重新绘制，这样才能看到动画效果
 *    2.2 AnimatorSet 动画集
 *        播放一组动画能设定先后顺序,在xml中的标签是<set> 重复模式只能在子动画中设置
 *    2.3 动画监听器
 *        2.3.1 Animator.AnimatorListener 动画重要节点回调
 *              用这个抽象类AnimatorListenerAdapter不用每个都重写
 *        2.3.2 ValueAnimator.AnimatorUpdateListener
 *             对动画的每一帧调用
 *    2.4 为ViewGroup布局动画(添加、删除、隐藏、显示)
 *        ViewGroup设置android:animateLayoutChanges="true"实现默认的布局改变动画。
 *        使用 LayoutTransition 类为 ViewGroup 实现自定义的布局更改动画效果 TODO 待总结
 *    2.5 为视图状态(按下、聚焦 )更改添加动画效果
 *        状态更改后给属性设置动画StateListAnimator在xml的标签是 <selector> 给视图设置动画<Button android:stateListAnimator=
 *        代码设置 AnimatorInflater.loadStateListAnimator()，然后使用 View.setStateListAnimator()
 *
 *        状态更改后给Drawable设置动画AnimatedStateListDrawable在xml的标签 <animated-selector> 给视图设置动画<Button  android:background=
 *        note:该例使用的是state_press,也可以使用state_check 不过使用这个属性还要在代码里使用  it.isChecked = true
 *    2.6 指定关键帧
 *        属性在指定时间处于什么值 ofFloat(fraction,value) 从构造函数也能看出来，
 *    2.7 使用ViewPropertyAnimator添加属性动画
 *        PropertyValuesHolder和ObjectAnimator 添加属性动画麻烦，系统封装了ViewPropertyAnimator简化流程, 不支持repeatCount 和 repeatMode，2.7的代码保存了初始值，反向动画让他支持repeat
 *        withEndAction/StartAction 动画开始个结束回调是一次性的动画执行结束就会删除。
 *
 * note：[拥有get和set方法的属性](https://developer.android.google.cn/guide/topics/graphics/prop-animation#views)
 * note：[根据动画资源章节发现animator是属性动画animation是视图动画](https://developer.android.google.cn/guide/topics/resources/animation-resource)
 *
 */
class PropertyAnimationActivity : AppCompatActivity() {
    lateinit var binding: ActivityPropertyAnimationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropertyAnimationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //1.1 AnimationDrawable
        binding.image1.setOnClickListener {
            (it.background as AnimationDrawable).start()//
        }

        //2.
        ValueAnimator.ofFloat(0f, 100f).apply {//ofFloat ofInt ofArgb
            duration = 1000
            addUpdateListener {
                println("animatedValue----${it.animatedValue as Float}")
            }
            start()
        }
        PointFEvaluator()
        ValueAnimator.ofObject({ fraction, startValue, endValue -> //fraction 这个值能不能大一点
            arrayOf(startValue as PointF, endValue as PointF)
            val tempX = startValue.x + (endValue.x - startValue.x) * fraction
            val tempY = startValue.y + (endValue.y - startValue.y) * fraction
            PointF(tempX, tempY)
        }, PointF(10f, 10f), PointF(20f, 20f)).apply {
            duration = 1000
            //2.3.2
            addUpdateListener {
                val point = it.animatedValue as PointF
                println("ofObject----${Pair(point.x, point.y)}")
            }
            start()
        }
        //2.1 ObjectAnimator
        binding.button1.setOnClickListener {
            ObjectAnimator.ofFloat(it, "translationX", 100f).apply {
                duration = 1000
                //2. repeatCount 和 repeatMode
                repeatCount = 1
                repeatMode = ValueAnimator.REVERSE
                start()
                //2.3.1
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        println("动画结束后调用")
                    }
                })
            }
        }
        //2.2 AnimatorSet  重复模式只能在子动画中设置
        val objectAnimator1 = ObjectAnimator.ofFloat(binding.button2, "translationX", 100f).apply {
            repeatCount = 1
            repeatMode = ValueAnimator.REVERSE
        }
        val objectAnimator2 = ObjectAnimator.ofFloat(binding.button2, "alpha", 1f, 0.1f).apply {
            repeatCount = 1
            repeatMode = ValueAnimator.REVERSE
        }
        val oa3 = ObjectAnimator.ofFloat(binding.button2, "rotation", 0f, 360f).apply {
            repeatCount = 1
            repeatMode = ValueAnimator.REVERSE
        }
        val oa4 = ObjectAnimator.ofFloat(binding.button2, "translationY", 100f).apply {
            repeatCount = 1
            repeatMode = ValueAnimator.REVERSE
        }
        binding.button2.setOnClickListener {
            AnimatorSet().apply {
                play(objectAnimator1)//播放动画
                    .with(objectAnimator2)//同时播放
                    .after(1000)//等待一定时间后播放play里的动画
//                    .after(oa3)//先执行after里的动画，再执行play里的动画
                    .before(oa4)//先执行play里的动画，再执行after里的动画
                //设置两个after结果发现 after(oa3)执行after(1000)没有执行
                duration = 1000
                start()
            }
        }
        //2.4 为布局更改添加动画
        binding.button3.setOnClickListener {
            binding.container.addView(TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER
                text = "添加一个布局"
            })
        }
        binding.button4.setOnClickListener {
            binding.container.takeIf { binding.container.childCount > 0 }?.removeView(binding.container.getChildAt(0))
        }
        binding.container.layoutTransition = LayoutTransition()
        //2.6 指定关键帧
        binding.button7.setOnClickListener {
            val kf0 = Keyframe.ofFloat(0f, 0f)
            val kf1 = Keyframe.ofFloat(.5f, 360f)
            val kf2 = Keyframe.ofFloat(1f, 0f)
            val pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2)
            ObjectAnimator.ofPropertyValuesHolder(it, pvhRotation).apply {
                duration = 5000
                start()
            }
        }
        //2.7 ViewPropertyAnimator
        binding.button8.setOnClickListener {
            val xyPair = Pair(it.x, it.y)
            it.animate().x(50f).y(100f).setDuration(1000).withEndAction {
                it.animate().x(xyPair.first).y(xyPair.second).setDuration(1000).start()
            }.start()
        }
    }
}
