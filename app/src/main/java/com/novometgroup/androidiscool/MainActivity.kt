package com.novometgroup.androidiscool

import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)

        //Вызов активити камеры
        findViewById<Button>(R.id.callCamera).setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)

            startActivity(intent)
        }


        //Обработка запроса на сервер
        val apiService = getApiService()

        val responseTextView = findViewById<TextView>(R.id.responseTextView)
        val getButton = findViewById<Button>(R.id.get_info)

        getButton.setOnClickListener {
            responseTextView.text = ""

            apiService.getMotorDetails().enqueue(
                object: Callback<ArrayList<MotorDetails>> {
                    override fun onResponse(
                        call: Call<ArrayList<MotorDetails>>,
                        response: Response<ArrayList<MotorDetails>>
                    ) {
                        val motorDetails = response.body()
                        if (motorDetails != null) {
                            responseTextView.text = "${motorDetails[2].code} : ${motorDetails[2].name}"
                        }
                    }

                    override fun onFailure(call: Call<ArrayList<MotorDetails>>, t: Throwable) {
                        responseTextView.text = t.toString()
                    }

                }
            )
        }

    }
}