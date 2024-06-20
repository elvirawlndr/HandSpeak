package com.elvira.handspeak.data.response

import com.google.gson.annotations.SerializedName

data class DetailArticleResponse(

	@field:SerializedName("data")
	val data: List<DataItemDetail>,

	@field:SerializedName("status")
	val status: String
)


data class DataItemDetail(

	@field:SerializedName("author")
	val author: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("published_date")
	val publishedDate: String? = null,

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("content")
	val content: String? = null,

	@field:SerializedName("tags")
	val tags: List<String?>? = null
)

