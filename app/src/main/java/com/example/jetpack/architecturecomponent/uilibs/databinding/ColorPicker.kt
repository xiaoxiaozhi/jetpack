package com.example.jetpack.architecturecomponent.uilibs.databinding

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener

/**
 * 1. 跟Relation代码稍有不一样。Databinding会自己查找属性的Getter方法和Setter方法。这里就没有使用BindingAdapter重新定义set逻辑
 *    双向绑定时一定要app:color="@={colorValue}"  加上 =
 */
class ColorPicker(context: Context, attributeSet: AttributeSet?) :
    androidx.appcompat.widget.AppCompatButton(context, attributeSet) {
    var color: Int = 0
        get() {
            println("color-----get()")
            return field
        }
        set(value) {
            if (field != value) {//破除无限循环
                println("color-----set(${value})")
                field = value
            } else {
                println("color-----新旧值一样(${value})")
            }

        }


    fun addListener(listener: OnColorChangeListener?) {
    }

    fun removeListener(listener: OnColorChangeListener?) {
    }

    interface OnColorChangeListener {
        fun onChange()
    }

}

object ColorPickerAdapter {
    @InverseBindingAdapter(attribute = "color")
    @JvmStatic
    fun getColorValue(view: ColorPicker): Int {
        println("getColorValue-----${view.color}")
        return view.color
    }

    @BindingAdapter("app:colorAttrChanged")
    @JvmStatic
    fun colorChange(view: ColorPicker, listener: InverseBindingListener) {
        view.setOnClickListener {
            println("ColorPicker----点击事件")
            view.color = 10
            listener.onChange()
        }
    }
}