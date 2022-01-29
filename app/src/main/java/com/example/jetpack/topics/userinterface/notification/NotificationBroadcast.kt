package com.example.jetpack.topics.userinterface.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.example.jetpack.KEY_TEXT_REPLY

class NotificationBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        println("${this.javaClass.simpleName}----onReceive")
        println("收到----${intent?.let { getMessageText(it) }}")

    }
    private fun getMessageText(intent: Intent): CharSequence? {
        return RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_TEXT_REPLY)
    }

}