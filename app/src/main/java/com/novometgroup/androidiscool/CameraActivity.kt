package com.novometgroup.androidiscool

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraActivity : AppCompatActivity(R.layout.activity_camera) {

    private var handler: Handler? = null

    private lateinit var cameraView: PreviewView

    private lateinit var imageCapture: ImageCapture

    private lateinit var flashModeButton: Button

    private lateinit var camera: Camera

    private var torch = false

    private var flashModeOn = true

    private var mediumExposure: Int = 0

    private val photoPath = "/storage/emulated/0/Pictures/DIFA"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        flashModeButton = findViewById(R.id.flash_mode)

        handler = Handler(Looper.getMainLooper())

        val folder = File(photoPath)

        if(!folder.exists()) folder.mkdir()

        cameraView = findViewById(R.id.camera_view)

        imageCapture = prepareImageCapture(ImageCapture.FLASH_MODE_ON)

        flashModeButton.setOnClickListener {
            if(flashModeOn) {
                flashModeOn = false
                imageCapture = prepareImageCapture(ImageCapture.FLASH_MODE_OFF)
                flashModeButton.text = "flash mode OFF"

                val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    bindPreview(cameraProvider, imageCapture)
                }, ContextCompat.getMainExecutor(this))
            } else {
                flashModeOn = true
                imageCapture = prepareImageCapture(ImageCapture.FLASH_MODE_ON)
                flashModeButton.text = "flash mode ON"

                val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    bindPreview(cameraProvider, imageCapture)
                }, ContextCompat.getMainExecutor(this))
            }
        }

        val cameraExecutor = Executors.newSingleThreadExecutor()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider, imageCapture)
        }, ContextCompat.getMainExecutor(this))

        findViewById<Button>(R.id.take_photo).setOnClickListener {
            val fileName = System.currentTimeMillis().toString() + ".jpeg"
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(File(photoPath + File.separator + fileName)).build()
            imageCapture.takePicture(outputFileOptions, cameraExecutor, object : OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("PHOTO", "Фото успешно сохранено в $photoPath")
                    this@CameraActivity.runOnUiThread {
                        Toast.makeText(this@CameraActivity,
                            "Фото сохранено!",
                            Toast.LENGTH_SHORT).show()
                    }
                    makeRequest(prepareFile(fileName))
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d("PHOTO", "Ошибка, фото не сохранено! Исключение: $exception")
                    this@CameraActivity.runOnUiThread {
                        Toast.makeText(this@CameraActivity,
                            "Ошибка! Фото не сохранено! ${exception}",
                            Toast.LENGTH_SHORT).show()
                    }
                }

            })
        }

        findViewById<Button>(R.id.zoom_in).setOnClickListener {
            camera.cameraControl.setLinearZoom(camera.cameraInfo.zoomState.value!!.linearZoom + 0.1F)
        }

        findViewById<Button>(R.id.zoom_out).setOnClickListener {

            camera.cameraControl.setLinearZoom(camera.cameraInfo.zoomState.value!!.linearZoom - 0.1F)
        }

        cameraView.setOnTouchListener { _, event ->
            val meteringPoint = cameraView.meteringPointFactory
                .createPoint(event.x, event.y)
            val action = FocusMeteringAction.Builder(meteringPoint)
                //.setAutoCancelDuration(3, TimeUnit.SECONDS)
                .disableAutoCancel()
                .build()

            val result = camera.cameraControl.startFocusAndMetering(action)

            result.addListener({
            }, ContextCompat.getMainExecutor(this@CameraActivity))

            Log.i("TOUCH", "Screen touch event")

            cameraView.performClick()
        }

        findViewById<Button>(R.id.toggle_torch).setOnClickListener {
            camera.cameraControl.enableTorch(!torch)
            torch = !torch
        }

        findViewById<Button>(R.id.exposure_up).setOnClickListener {
            mediumExposure += 1
            camera.cameraControl.setExposureCompensationIndex(mediumExposure)
        }

        findViewById<Button>(R.id.exposure_down).setOnClickListener {
            mediumExposure -= 1
            camera.cameraControl.setExposureCompensationIndex(mediumExposure)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        val action = event?.action
        when(event?.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> if(action == KeyEvent.ACTION_DOWN)  {
                camera.cameraControl.setLinearZoom(camera.cameraInfo.zoomState.value!!.linearZoom + 0.1F)
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> if(action == KeyEvent.ACTION_DOWN) {
                camera.cameraControl.setLinearZoom(camera.cameraInfo.zoomState.value!!.linearZoom - 0.1F)
                return true
            }
            else -> return super.dispatchKeyEvent(event)
        }
        return super.dispatchKeyEvent(event)
    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider, imageCapture: ImageCapture) {

        var preview = Preview.Builder()
            .build()

        var cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(cameraView.surfaceProvider)

        cameraProvider.unbindAll()

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)

        val range = camera.cameraInfo.exposureState.exposureCompensationRange
        mediumExposure = (range.lower + range.upper) / 2
    }

    private fun prepareFile(name: String): MultipartBody.Part {
        val file = File("/storage/emulated/0/Pictures/DIFA/$name")

        val requestFile = RequestBody.create(MultipartBody.FORM, file)

        return MultipartBody.Part.createFormData("image", file.name, requestFile)
    }

    private fun makeRequest(body: MultipartBody.Part) {
        val uploadService = getUploadService()
        uploadService.postImage(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }

        })
    }

    private fun prepareImageCapture(flashMode: Int): ImageCapture  {
        return ImageCapture.Builder()
            .setFlashMode(flashMode)
            .build()
    }
}