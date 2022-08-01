package com.example.jetpack.appnavigaion.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jetpack.R
import com.example.jetpack.databinding.FragmentBlankBinding
import com.google.android.material.transition.MaterialContainerTransform
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BlankFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BlankFragment : Fragment(), View.OnClickListener {
    lateinit var dataBinding: FragmentBlankBinding

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("${this.javaClass.simpleName}-------onCreate")
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        val arg: BlankFragmentArgs by navArgs<BlankFragmentArgs>()
        println("----name = ${arg.name}")

        sharedElementEnterTransition = MaterialContainerTransform(requireContext(), true)
        sharedElementReturnTransition = MaterialContainerTransform(requireContext(), true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBinding = DataBindingUtil.inflate<FragmentBlankBinding>(
            layoutInflater,
            R.layout.fragment_blank,
            container,
            false
        );
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dataBinding.button1.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in).setExitAnim(R.anim.fade_out)
                .setPopEnterAnim(R.anim.slide_out).setPopExitAnim(R.anim.fade_in).build()

            findNavController().navigate(R.id.thirdFragment, null, navOptions)
        }
        dataBinding.button2.setOnClickListener(this)
        dataBinding.button3.setOnClickListener(this)
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BlankFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button2 -> findNavController().navigateUp()
            R.id.button3 -> {
                val result = findNavController().popBackStack()
                println("popBackStack-----${result}")
            }
        }
    }
}