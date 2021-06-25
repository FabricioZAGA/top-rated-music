package com.example.topratedmusic

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @GET("random")
    fun getRandom(): Call<Cancion>

}