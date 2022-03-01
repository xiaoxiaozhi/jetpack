package com.example.jetpack.architecturecomponent.uilibs.databinding

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.databinding.InverseBindingAdapter

class MyTextView(@NonNull context: Context, @Nullable attrs: AttributeSet) :
    androidx.appcompat.widget.AppCompatTextView(context, attrs) {


    override fun setBackground(background: Drawable?) {
        super.setBackground(background)
        println("---------------setBackground---------------------")
    }

    fun showText(str: String) {
        println("自定义属性-----------$str----------------")
    }

    override fun setAccessibilityHeading(isHeading: Boolean) {
        super.setAccessibilityHeading(isHeading)
        println("setAccessibilityHeading------------------${isHeading}")
    }

    fun setAlpha1(alpha: Float) {
        println("setAlpha1----------${alpha}")
    }

    override fun setAlpha(alpha: Float) {
        super.setAlpha(alpha)
        println("setAlpha1----------${alpha}")
    }

    override fun setCameraDistance(distance: Float) {
        super.setCameraDistance(distance)
        println("setCameraDistance-------${distance}")
    }

}