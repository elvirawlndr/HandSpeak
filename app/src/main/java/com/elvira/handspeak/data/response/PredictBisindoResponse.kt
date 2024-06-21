package com.elvira.handspeak.data.response

import com.google.gson.annotations.SerializedName

data class PredictBisindoResponse(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("code_status")
	val codeStatus: Int? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DataBisindo(

	@field:SerializedName("result")
	val result: String? = null
)
