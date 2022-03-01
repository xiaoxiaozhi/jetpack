package com.example.jetpack.topics.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.jetpack.R
import com.example.jetpack.databinding.FragmentScrollingBinding

class ScrollingFragment : Fragment() {
    private lateinit var binding: FragmentScrollingBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScrollingBinding.inflate(inflater, container, false)
        binding.button1.setOnClickListener {
            ScrollingFragmentDirections.actionScrollingFragmentToBlankFragment()
        }
        return binding.root
    }
}