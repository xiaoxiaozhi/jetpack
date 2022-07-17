package com.example.jetpack.topics.intent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ShareBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        println("Received intent after selection:---${intent?.extras?.get(Intent.EXTRA_CHOSEN_COMPONENT)}")
    }

}