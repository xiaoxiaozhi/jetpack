package com.example.jetpack.architecturecomponent.uilibs.databinding

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import androidx.annotation.NonNull
import androidx.annotation.Nullable

class MyButton(@NonNull context: Context, @Nullable attrs: AttributeSet) :
    androidx.appcompat.widget.AppCompatButton(context, attrs) {
    var myName: String? = null
        set(value) {
            println("setMyName------${value}")
            field = value
        }
}