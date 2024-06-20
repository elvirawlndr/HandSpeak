package com.elvira.handspeak.data.retrofit

import com.elvira.handspeak.data.response.ArticleResponse
import com.elvira.handspeak.data.response.DetailArticleResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("article-bucket-capstone/articleAll.json")
    fun getArticle(
    ): Call<ArticleResponse>

    @GET("article-bucket-capstone/article/article{id}.json")
    fun getDetailArticle(
        @Path("id") id: String
    ): Call<DetailArticleResponse>
}