package com.example.jetpack.topics.appdatafiles.room

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.jetpack.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoomActivity2 : AppCompatActivity() {
    private val viewModel: WordViewModel by viewModels<WordViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room2)
        viewModel.allWords
    }
}