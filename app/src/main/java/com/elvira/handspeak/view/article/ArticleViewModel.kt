package com.elvira.handspeak.view.article

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.elvira.handspeak.data.response.DataItemDetail
import com.elvira.handspeak.data.response.DetailArticleResponse
import com.elvira.handspeak.data.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArticleViewModel: ViewModel() {
    private val _articleDetail = MutableLiveData<DataItemDetail>()
    val articleDetail: LiveData<DataItemDetail> = _articleDetail


    fun getDetailArticle(id: String) {
        val client = ApiConfig.getStorageApiService().getDetailArticle(id)
        client.enqueue(object : Callback<DetailArticleResponse> {
            override fun onResponse(
                call: Call<DetailArticleResponse>,
                response: Response<DetailArticleResponse>
            ) {
                if (response.isSuccessful) {
                    val dataList = response.body()?.data
                    val dataItem = dataList?.firstOrNull()
                    _articleDetail.value = dataItem!!
                } else {
                    Log.e("onFailure: ", response.message())
                }
            }

            override fun onFailure(call: Call<DetailArticleResponse>, t: Throwable) {
                Log.e("onFailure: ", t.message.toString())
            }

        })
    }
}