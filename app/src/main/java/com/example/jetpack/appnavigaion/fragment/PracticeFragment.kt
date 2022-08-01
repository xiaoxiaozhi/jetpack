package com.example.jetpack.appnavigaion.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jetpack.R


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * https://developer.android.google.cn/guide/fragments
 * 1. Activity管理导航，fragment 管理点击导航出来的具体屏幕
 * 2. fragment可以在多个activity或者其它fragment中实例化，考虑到这点，fragment的UI和逻辑最好不依赖其它fragment
 * 3. 使用DialogFragment创建浮动动画框而不是在Activity中使用AlertDialog，应为fragment会自动处理对话框的创建和清理
 * 4. 通过布局文件或者编程的方式向Activity添加fragment，无论哪种方式都强烈建议使用FragmentContainerView当做容器，它提供了FrameLayout不提供的修复功能
 *    4.1 向布局文件中添加Fragment先声明FragmentContainerView标签。android:name="包名.类名"(或者 android:class)属性指定要实例化的Fragment。
 *        当布局解析时，将实例化指定的片段，对新实例化的片段调用onInflate()，并创建FragmentTransaction将该片段添加到FragmentManager
 *    4.2 以编程的方式添加Fragment，还是要先在Activity的布局文件中声明FragmentContainerView标签，此时不需要指定fragment，
 *        但是FragmentTransaction仍然会实例化一个Fragment并将其添加到活动的布局中
 */
class PracticeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_practice, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PracticeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PracticeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}