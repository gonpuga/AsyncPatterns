package com.example.asyncpatterns.async

import android.app.IntentService
import android.content.Intent
import com.example.asyncpatterns.utils.DownloaderUtil
import com.example.asyncpatterns.utils.LocalBroadcastUtil

@Suppress("DEPRECATION")
class MyIntentService : IntentService("MyIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        // Download Image
        val bmp = DownloaderUtil.downloadImage()
        // Send local broadcast with the bitmap as payload
        LocalBroadcastUtil.sendBitmap(this, bmp)
    }
}