package com.elvira.handspeak.data.response

import com.google.gson.annotations.SerializedName


data class ArticleResponse(

    @field:SerializedName("data")
	val data: List<DataItem>,

    @field:SerializedName("status")
	val status: String
)

data class DataItem(

	@field:SerializedName("author")
	val author: String,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("title")
	val title: String,

	@field:SerializedName("published_date")
	val publishedDate: String,

	@field:SerializedName("image")
	val image: String,

	@field:SerializedName("content")
	val content: String,

	@field:SerializedName("tags")
	val tags: List<String>
)
