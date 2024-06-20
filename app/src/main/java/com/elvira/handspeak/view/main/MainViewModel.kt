package com.elvira.handspeak.view.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elvira.handspeak.data.response.ArticleResponse
import com.elvira.handspeak.data.response.DataItem
import com.elvira.handspeak.data.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel : ViewModel() {
    private val _articleResponse = MutableLiveData<List<DataItem>>()
    val articleResponse: LiveData<List<DataItem>> get() = _articleResponse

    init {
        getArticles()
    }

    private fun getArticles() {
        viewModelScope.launch {
            val client = ApiConfig.getApiService().getArticle()
            client.enqueue(object : Callback<ArticleResponse> {
                override fun onResponse(
                    call: Call<ArticleResponse>,
                    response: Response<ArticleResponse>
                ) {
                    if (response.isSuccessful) {
                        _articleResponse.value = response.body()?.data
                    } else {
                        Log.e("onFailure: ", response.message())
                    }
                }

                override fun onFailure(call: Call<ArticleResponse>, t: Throwable) {
                    Log.e("onFailure: ", t.message.toString())
                }

            })
        }
    }
}