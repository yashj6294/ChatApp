package com.example.chatapp.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface ApiService {
    @POST("send")
    fun sendMessage(
        @HeaderMap headers: Map<String, String> = mapOf(),
        @Body messageBody :String
    ) :Call<String>
}