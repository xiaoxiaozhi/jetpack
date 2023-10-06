package com.example.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

@com.example.compiler.annotation.Function("LoginActivity")
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }
}