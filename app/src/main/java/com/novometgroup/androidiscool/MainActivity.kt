package com.novometgroup.androidiscool

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.novometgroup.androidiscool.databinding.ActivityMainBinding
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.progressBar.isVisible = false

        //Вызов активити камеры
        binding.callCamera.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)

            startActivity(intent)
        }


        //Обработка запроса на сервер
        val apiService = getApiService()

        binding.getInfo.setOnClickListener {

            binding.progressBar.isVisible = true

            binding.responseTextView.text = ""

            apiService.getMotorDetails().enqueue(
                object: Callback<ArrayList<MotorDetails>> {
                    override fun onResponse(
                        call: Call<ArrayList<MotorDetails>>,
                        response: Response<ArrayList<MotorDetails>>
                    ) {
                        val motorDetails = response.body()
                        if (motorDetails != null) {
                            binding.responseTextView.text = "${motorDetails[2].code} : ${motorDetails[2].name}"
                        }

                        binding.progressBar.isVisible = false
                    }

                    override fun onFailure(call: Call<ArrayList<MotorDetails>>, t: Throwable) {
                        binding.responseTextView.text = t.toString()
                        binding.progressBar.isVisible = false
                    }

                }
            )
        }

        //Отправка файла на сервер
        val fileName = "cat.jpeg"

        val file = File("/storage/emulated/0/Pictures/DIFA/$fileName")

        val requestFile = RequestBody.create(MultipartBody.FORM, file)

        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val uploadService = getUploadService()

        binding.postImage.setOnClickListener {

            binding.progressBar.isVisible = true

            binding.responseTextView.text = ""

            uploadService.postImage(body).enqueue(object : Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    binding.responseTextView.text = response.toString()
                    binding.progressBar.isVisible = false
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    binding.responseTextView.text = t.toString()
                    binding.progressBar.isVisible = false
                }

            })
        }
    }
}