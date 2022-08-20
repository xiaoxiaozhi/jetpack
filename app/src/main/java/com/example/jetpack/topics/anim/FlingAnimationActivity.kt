package com.example.jetpack.topics.anim

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.marginLeft
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityFlingAnimationBinding

/**
 * 投掷动画
 * TODO 待看
 */
class FlingAnimationActivity : AppCompatActivity() {
    lateinit var binding: ActivityFlingAnimationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityFlingAnimationBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
//        val fling = FlingAnimation(view, DynamicAnimation.SCROLL_X)
        repeat(100) {
            ImageView(this).apply {
                background = resources.getDrawable(R.mipmap.ic_launcher_round, null)
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).also {
                    it.leftMargin = 10
                }
                binding.linear.addView(this)
            }
        }
    }
}