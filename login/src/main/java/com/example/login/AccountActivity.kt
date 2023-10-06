package com.example.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
@com.example.compiler.annotation.Function("AccountActivity")
class AccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
    }

    @com.example.compiler.annotation.Function("AA")
    inner class AA
}