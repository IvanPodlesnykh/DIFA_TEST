package com.novometgroup.androidiscool

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity(R.layout.activity_camera) {

    private var handler: Handler? = null

    private lateinit var cameraView: PreviewView

    private lateinit var imageCapture: ImageCapture

    private val photoPath = "/storage/emulated/0/Pictures/DIFA"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler(Looper.getMainLooper())

        val folder = File(photoPath)

        if(!folder.exists()) folder.mkdir()

        cameraView = findViewById(R.id.camera_view)

        imageCapture = ImageCapture.Builder()
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))


        findViewById<Button>(R.id.takePhoto).setOnClickListener {
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(File(photoPath + File.separator + System.currentTimeMillis() + ".jpeg")).build()
            imageCapture.takePicture(outputFileOptions, cameraExecutor, object : OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("PHOTO", "Фото успешно сохранено в $photoPath")
                    this@CameraActivity.runOnUiThread {
                        Toast.makeText(this@CameraActivity,
                            "Фото сохранено!",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d("PHOTO", "Ошибка, фото не сохранено! Исключение: $exception")
                    this@CameraActivity.runOnUiThread {
                        Toast.makeText(this@CameraActivity,
                            "Ошибка! Фото не сохранено!",
                            Toast.LENGTH_SHORT).show()
                    }
                }

            })
        }
    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider) {

        var preview = Preview.Builder()
            .build()

        var cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(cameraView.surfaceProvider)

        cameraProvider.unbindAll()

        var camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)
    }
}