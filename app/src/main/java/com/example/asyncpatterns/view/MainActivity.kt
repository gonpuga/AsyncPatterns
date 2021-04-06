package com.example.asyncpatterns.view

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.asyncpatterns.R
import com.example.asyncpatterns.async.DownloaderWorkManager
import com.example.asyncpatterns.async.ImageDownloaderAsyncTask
import com.example.asyncpatterns.async.MyIntentService
import com.example.asyncpatterns.databinding.ActivityMainBinding
import com.example.asyncpatterns.utils.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding

    companion object {
        const val FILTER_ACTION_KEY = "ASYNC_PATTERNS_ACTION"
    }

    enum class MethodToUse {
        UIThread, Thread, AsyncTask, IntentService, Handler, HandlerThread, Executor, WorkManager, RxJava, Coroutines
    }

    //listener
    private val imageDownloadListener = object : ImageDownloadListener {
        override fun onSuccess(bitmap: Bitmap?) {
            // Update UI with downloaded bitmap
            binding.imgResult.setImageBitmap(bitmap)
        }
    }

    //Local Broadcast receiver
    private val myReceiver = MyBroadcastReceiver(imageDownloadListener)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup a rotating spinner in the UI
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_indefinitely)
        binding.imgProgressBar.startAnimation(rotateAnimation)

        binding.btnDownloadBitmap.setOnClickListener {
            if (isOnline()) {
                // Reset the imageview
                binding.imgResult.setImageBitmap(null)
                // get method to use from preferences
                val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                val method = prefs.getString("async_patterns_list", "0")?.toInt()
                when (method) {
                    MethodToUse.UIThread.ordinal -> runUIBlockingProcess()
                    MethodToUse.Thread.ordinal -> getImageUsingThread()
                    MethodToUse.AsyncTask.ordinal -> getImageUsingAsyncTask()
                    MethodToUse.IntentService.ordinal -> getImageUsingIntentService()
                    MethodToUse.Handler.ordinal -> getImageUsingHandler()
                    MethodToUse.HandlerThread.ordinal -> getImageUsingHandlerThread()
                    MethodToUse.Executor.ordinal -> getImageUsingExecutor()
                    MethodToUse.WorkManager.ordinal -> getImageUsingWorkManager()
                    MethodToUse.RxJava.ordinal -> getImageUsingRxJava()
                    MethodToUse.Coroutines.ordinal -> getImageUsingCoroutines()
                }
            }
            else {
                binding.txtMethodUsed.text=""
                showToast(getString(R.string.networkNotAvailable))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_select_pattern -> {
                // opening a new intent to open settings activity.
                val i = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(i)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ----------- Downloading Methods-----------//
    
    private fun runUIBlockingProcess() {
        binding.txtMethodUsed.text=getString(R.string.Fibonacci)
        showToast("Result: ${fibonacci(42)}")
    }

    private fun getImageUsingThread() {
        binding.txtMethodUsed.text=getString(R.string.DownLoadThread)
        // Download image
        val thread = Thread(MyRunnable())
        thread.start()
    }

    private fun getImageUsingAsyncTask() {
        binding.txtMethodUsed.text=getString(R.string.DownloadAsyncTask)
        val mAsyncTask=ImageDownloaderAsyncTask(imageDownloadListener)
        mAsyncTask.execute()
    }

    private fun getImageUsingIntentService() {
        binding.txtMethodUsed.text=getString(R.string.DownloadIntentService)
        // Download image
        val intent = Intent(this@MainActivity, MyIntentService::class.java)
        startService(intent)
    }

    //https://bit.ly/3fimHOh
    private fun getImageUsingHandler() {
        binding.txtMethodUsed.text=getString(R.string.DownloadHandler)
        // Create a Handler using the main Looper
        val uiHandler = Handler(Looper.getMainLooper())
        Thread{
            // Download image
            val bmp = DownloaderUtil.downloadImage()
            // Using the uiHandler update the UI
            uiHandler.post {
                binding.imgResult.setImageBitmap(bmp)
            }
        }.start()
    }

    //https://bit.ly/39kr5Z6
    private lateinit var handlerThread:HandlerThread
    private fun getImageUsingHandlerThread() {
        binding.txtMethodUsed.text=getString(R.string.DownloadHandlerThread)
        // Create a HandlerThread
        handlerThread = HandlerThread("MyHandlerThread")
        handlerThread.let{
            // Start the HandlerThread
            it.start()
            // Get the Looper
            val looper = it.looper
            // Create a Handler using the obtained Looper
            val handler = Handler(looper)
            // Execute the Handler
            handler.post {
                // Download image
                val bmp = DownloaderUtil.downloadImage()
                // Send local broadcast with the bitmap as payload
                LocalBroadcastUtil.sendBitmap(this, bmp)
            }
        }
    }

    private fun getImageUsingExecutor() {
        binding.txtMethodUsed.text=getString(R.string.DownloadExecutor)
        val executor=Executors.newFixedThreadPool(2)
        executor.submit(MyRunnable())
    }

    //https://bit.ly/3sK4P2s
    private fun getImageUsingWorkManager() {
        binding.txtMethodUsed.text=getString(R.string.DownloadWorkManager)
        //create the request
        val workRequest= OneTimeWorkRequestBuilder<DownloaderWorkManager>().build()
        //enqueue de request
        WorkManager.getInstance(this).enqueue(workRequest)
    }

    //https://bit.ly/2PfgF6o
    private lateinit var single:Disposable
    private fun getImageUsingRxJava() {
        binding.txtMethodUsed.text = getString(R.string.DownloadRxJava)
        // Download image
        single = Single.create<Bitmap> { emitter ->
            DownloaderUtil.downloadImage()?.let { bmp ->
                emitter.onSuccess(bmp)
            }
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe { bmp ->
                // Update UI with downloaded bitmap
                binding.imgResult.setImageBitmap(bmp)

            }
    }

    private fun getImageUsingCoroutines() {
        binding.txtMethodUsed.text=getString(R.string.DownloadCoroutines)
        GlobalScope.launch {
            // Download Image in background
            val deferredJob = async(Dispatchers.IO) {
                DownloaderUtil.downloadImage()
            }
            withContext(Dispatchers.Main) {
                val bmp = deferredJob.await()
                // Update UI with downloaded bitmap
                binding.imgResult.setImageBitmap(bmp)
            }
        }
    }

    // ----------- Helper Methods -----------//
    fun fibonacci(number: Int): Long {
        return if (number == 1 || number == 2) {
            1
        } else fibonacci(number - 1) + fibonacci(number - 2)
    }

    // Implementing the Runnable interface to implement threads.
    inner class MyRunnable : Runnable {

        override fun run() {
            // Download Image
            val bmp = DownloaderUtil.downloadImage()

            // Update UI on the UI/Main Thread with downloaded bitmap
            runOnUiThread {
                binding.imgResult.setImageBitmap(bmp)
            }
        }
    }

    // ----------- Lifecycle Methods -----------//
    override fun onStart() {
        super.onStart()
        LocalBroadcastUtil.registerReceiver(this, myReceiver)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastUtil.unregisterReceiver(this, myReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Quit and cleanup any instance of dangling HandlerThread
        handlerThread?.quit()
        // Cleanup disposable if it was created i.e. not null
        single?.dispose()
    }
}