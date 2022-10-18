package com.example.jetpack.bestpractice.mypractices

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.databinding.DataBindingUtil.setContentView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityRootBinding

/**
 * TODO toolbar 文字不跟随导航目的的标签改变；NavigationAdvancedSample 这个例子用ActionBar就能跟随着改变标题
 *
 */
class RootActivity : AppCompatActivity() {
    lateinit var binding: ActivityRootBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView<ActivityRootBinding>(this, R.layout.activity_root)
//        setSupportActionBar(binding.toolbar)
    }

    override fun onStart() {
        super.onStart()
        findNavController(R.id.nav_host_fragment).apply {
            binding.bottomNavigation.setupWithNavController(this)//
            addOnDestinationChangedListener { _, destination, arguments ->
                println("当前标签-----${destination.displayName}")
                println("arguments--------${arguments?.getString("name")}")
            }
        }
        val appBarConfiguration = AppBarConfiguration(findNavController(R.id.nav_host_fragment).graph)
//        val appBarConfiguration =
//            AppBarConfiguration(setOf(R.id.defaultFragment, R.id.githubFragment, R.id.otherFragment))
        binding.toolbar.setupWithNavController(
            findNavController(R.id.nav_host_fragment), appBarConfiguration
        )

    }
}