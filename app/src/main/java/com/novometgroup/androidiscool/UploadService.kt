package com.novometgroup.androidiscool

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

const val baseServerUrl = "http://192.168.31.18/"
interface UploadService {

    @Multipart
    @POST("/Test")
    fun postImage(@Part image: MultipartBody.Part): Call<ResponseBody>

}
fun getUploadService(): UploadService {
    return Retrofit.Builder()
        .baseUrl(baseServerUrl)
        .client(getUnsafeOkHttpClient())
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(UploadService::class.java)
}