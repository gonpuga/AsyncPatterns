package com.example.asyncpatterns.utils

import android.graphics.Bitmap

/*
    Listener for images being downloaded
 */
interface ImageDownloadListener {
    fun onSuccess(bitmap: Bitmap?)
}