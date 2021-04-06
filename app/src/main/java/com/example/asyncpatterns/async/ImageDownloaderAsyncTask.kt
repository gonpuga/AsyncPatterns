package com.example.asyncpatterns.async

import android.graphics.Bitmap
import android.os.AsyncTask
import com.example.asyncpatterns.utils.DownloaderUtil
import com.example.asyncpatterns.utils.ImageDownloadListener

@Suppress("DEPRECATION")
class ImageDownloaderAsyncTask(val imageDownloadListener: ImageDownloadListener) :
        AsyncTask<Void, Void, Bitmap>() {

    override fun doInBackground(vararg p0: Void?): Bitmap? {
        //Download Image
        return DownloaderUtil.downloadImage()
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        super.onPostExecute(bitmap)
        //pass image to the listener
        imageDownloadListener.onSuccess(bitmap)
    }
}
