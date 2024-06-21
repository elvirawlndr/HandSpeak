package com.elvira.handspeak.data.retrofit

import com.elvira.handspeak.data.response.ArticleResponse
import com.elvira.handspeak.data.response.DetailArticleResponse
import com.elvira.handspeak.data.response.PredictBisindoResponse
import com.elvira.handspeak.data.response.PredictSibiResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @GET("article-bucket-capstone/articleAll.json")
    fun getArticle(
    ): Call<ArticleResponse>

    @GET("article-bucket-capstone/article/article{id}.json")
    fun getDetailArticle(
        @Path("id") id: String
    ): Call<DetailArticleResponse>

    @Multipart
    @POST("predictSibi")
    fun uploadSibiImage(
        @Part file: MultipartBody.Part
    ): Call<PredictSibiResponse>

    @Multipart
    @POST("predictBisindo")
    fun uploadBisindoImage(
        @Part file: MultipartBody.Part
    ): Call<PredictBisindoResponse>

}