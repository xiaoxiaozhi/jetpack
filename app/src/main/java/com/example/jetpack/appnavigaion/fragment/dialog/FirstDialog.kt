package com.example.jetpack.appnavigaion.fragment.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.jetpack.R

/**
 * 1.圆角  dialog的xml设置成圆角就得到圆角dialog，然后在dialogFragment 的onCreatView 方法中执行 dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
 * 2.消除背景颜色
 * 3.设置宽高， 设置的宽高，如果子view填不满会显示子view的高度，超出会使一部分子view不可见
 *
 *  dialog?.window? 代码设置效果的优先级高于主题设置，当全部都设置时，主题设置无效 （ 主题设置 override fun getTheme(): Int = R.style.Theme_NoWiredStrapInNavigationBar）
 *  看这个对DialogFragment 的总结感觉有点东西 https://cloud.tencent.com/developer/article/1453726
 */
class FirstDialog : DialogFragment() {
    internal lateinit var listener: NoticeDialogListener
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_siginin, null)
        initView(view)
        dialog.setContentView(view)
        return dialog
    }

    private fun initView(rootView: View) {

    }

    //主题设置效果
//    override fun getTheme(): Int = R.style.Theme_NoWiredStrapInNavigationBar
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))//2.使用圆角会暴漏默认背景，该设置使背景透明
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        //1.3 把对话框事件传递给宿主
        try {
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                (context.toString() + " must implement NoticeDialogListener")
            )
        }
    }

    override fun onResume() {
        super.onResume()
//        dialog?.window?.setLayout(dp2px(activity, 304f), dp2px(activity, 265f))
    }

    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    fun dp2px(context: Context, dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dpVal, context.resources.displayMetrics
        ).toInt()
    }
}


