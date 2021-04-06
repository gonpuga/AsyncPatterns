package com.example.asyncpatterns.async

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.asyncpatterns.utils.DownloaderUtil
import com.example.asyncpatterns.utils.LocalBroadcastUtil

class DownloaderWorkManager(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Download Image
        val bmp = DownloaderUtil.downloadImage()
        // Send local broadcast with the bitmap as payload
        LocalBroadcastUtil.sendBitmap(applicationContext, bmp)
        //work is done
        return Result.success()
    }
}