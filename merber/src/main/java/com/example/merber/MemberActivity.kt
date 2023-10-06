package com.example.merber

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myannotation.BindPath

@BindPath("member/MemberActivity")
class MemberActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member)
    }
}