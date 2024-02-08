package com.novometgroup.androidiscool

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

const val baseServerUrl = "http://172.16.30.75:80/"
interface UploadService {

    @Multipart
    @POST("/test")
    fun postImage(@Part image: MultipartBody.Part): Call<ResponseBody>

}
fun getUploadService(): UploadService {
    return Retrofit.Builder()
        .baseUrl(baseServerUrl)
        .client(getUnsafeOkHttpClient())
        .build().create(UploadService::class.java)
}