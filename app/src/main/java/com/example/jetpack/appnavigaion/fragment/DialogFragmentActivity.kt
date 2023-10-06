package com.example.jetpack.appnavigaion.fragment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.DialogFragment
import com.example.jetpack.R
import com.example.jetpack.appnavigaion.fragment.dialog.DialogThemActivity
import com.example.jetpack.appnavigaion.fragment.dialog.FirstDialog
import com.example.jetpack.databinding.ActivityDialogFragmentBinding

/**
 * dialog创建 https://developer.android.com/guide/topics/ui/dialogs?hl=zh-cn
 * DialogFragment各项设置看FirstDialog
 * 1. 创建Dialog
 *    覆写DialogFragment的onCreateDialog()在返回中创建 AlertDialog,通常情况下，AlertDialog是您完成构建所需的唯一对话框类
 *    1.1 创建带列表的Dialog
 *        build.setItems(R.array.colors_array
 *        build.setMultiChoiceItems() 多选框列表
 *        build.setSingleChoiceItems()复选框列表
 *        圆角  dialog的xml设置成圆角就得到圆角dialog，然后在dialogFragment 的onCreatView 方法中执行 dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
 *        消除背景颜色
 *    1.2 创建自定义布局Dialog
 *        AlertDialog.Builder 对象调用 setView()，将该布局添加至 AlertDialog。默认情况下，自定义布局会填充对话框窗口，但您仍可使用 AlertDialog.Builder 方法来添加按钮和标题。
 *        还可以用Activity显示对话框只需要加一个主题 @style/Theme.AppCompat.Dialog.MinWidth(由于继承了AppCompatActivity所以必须使用AppCompat主题)
 *        为什么是@style而不是 @android:style 这是因为appcompat包是以 androix.appcompat的形式引入项目
 *    1.3 把对话框事件传回宿主
 *        让宿主继承接口，然后覆写对话框的 override fun onAttach(context: Context)得到接口调用
 *    1.4 显示对话框
 *        dialogFragment.show(supportFragmentManager, "game")
 *        第一个参数：您可以从 FragmentActivity 调用 getSupportFragmentManager() 或从 Fragment 调用 getFragmentManager()，
 *        第二个参数：是系统用于保存 Fragment 状态并在必要时进行恢复的唯一标记名称。该标记还允许您通过调用 findFragmentByTag() 来获取 Fragment 的句柄
 *
 *
 * dialog显示 https://developer.android.com/guide/fragments/dialogs
 *
 * 感觉以前用窗口作为容器，现在用Fragment作为容器
 * 没有调用dismiss就多次调用show会产生异常 Android Fragment already added
 */
class DialogFragmentActivity : AppCompatActivity(), FirstDialog.NoticeDialogListener {
    lateinit var binding: ActivityDialogFragmentBinding
    val dialog by lazy {
        FirstDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_dialog_fragment)
        binding.button1.setOnClickListener {
            dialog.show(supportFragmentManager, "FirstDialog")
        }
        binding.button2.setOnClickListener {
            Intent(this@DialogFragmentActivity, DialogThemActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {

    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {

    }
}