package com.example.jetpack.topics.anim

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jetpack.R

class Transition1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transition1)
    }

    override fun finishAfterTransition() {
        super.finishAfterTransition()
    }
}