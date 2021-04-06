package com.example.asyncpatterns.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.asyncpatterns.view.MainActivity

internal object LocalBroadcastUtil {
    /**
     * Send local broadcast with the bitmap as payload
     */
    fun sendBitmap(context: Context, bmp: Bitmap?) {
        val newIntent = Intent()
        bmp?.let {
            newIntent.putExtra("bitmap", it)
            newIntent.action = MainActivity.FILTER_ACTION_KEY
            LocalBroadcastManager.getInstance(context).sendBroadcast(newIntent)
        }
    }

    /**
     * Register Local Broadcast Manager with the receiver
     */
    fun registerReceiver(context: Context, myBroadcastReceiver: MyBroadcastReceiver?) {
        myBroadcastReceiver?.let {
            val intentFilter = IntentFilter()
            intentFilter.addAction(MainActivity.FILTER_ACTION_KEY)
            LocalBroadcastManager.getInstance(context).registerReceiver(it, intentFilter)
        }
    }

    /**
     * Unregister Local Broadcast Manager from the receiver
     */
    fun unregisterReceiver(context: Context, myBroadcastReceiver: MyBroadcastReceiver?) {
        myBroadcastReceiver?.let {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(it)
        }
    }
}