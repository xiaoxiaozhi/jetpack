package com.example.jetpack.bestpractice.mypractices

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jetpack.R
import com.example.jetpack.architecturecomponent.uilibs.paging.MainViewModel
import com.example.jetpack.architecturecomponent.uilibs.paging.RepoAdapter
import com.example.jetpack.databinding.ActivityPagingBinding
import com.example.jetpack.databinding.FragmentDefaultBinding
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DefaultFragment : Fragment() {
    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }
    private val repoAdapter = RepoAdapter()
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentDefaultBinding.inflate(inflater, container, false)
        binding.recycler.layoutManager = LinearLayoutManager(activity)
        binding.recycler.adapter = repoAdapter
        lifecycleScope.launch {
            viewModel.getPagingData().collect {
                repoAdapter.submitData(it)
            }
        }
//        setHasOptionsMenu(true)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = DefaultFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}