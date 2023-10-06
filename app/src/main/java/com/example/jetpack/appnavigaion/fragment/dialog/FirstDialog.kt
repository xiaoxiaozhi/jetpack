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
 */
class FirstDialog : DialogFragment() {
    internal lateinit var listener: NoticeDialogListener
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it).setView(R.layout.dialog_siginin)//1.2 创建自定义布局
            builder.setMessage("Message").setPositiveButton("是") { dialog, witch ->

            }.setNegativeButton("否") { dialog, witch ->

            }
//                .setNeutralButton()// 确定、否定、这个是中性按钮
//            .setItems(R.array.colors_array,DialogInterface.OnClickListener{})//1.1 创建带列表的Dialog
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

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


