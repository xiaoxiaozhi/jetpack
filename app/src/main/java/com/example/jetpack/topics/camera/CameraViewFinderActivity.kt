package com.example.jetpack.topics.camera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityCameraViewFinderBinding
import androidx.databinding.DataBindingUtil.setContentView

/**
 *
 */
class CameraViewFinderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraViewFinderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_camera_view_finder)

    }
}