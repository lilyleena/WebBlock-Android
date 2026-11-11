package com.kshrd.core.data.entity

import com.google.gson.annotations.SerializedName

/** Error body the backend returns for non-2xx responses. */
data class ErrorBaseResponse(
    @SerializedName("errorDate")
    val errorDate: String? = null,
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("errors")
    val errors: ErrorDetail? = null,
)

data class ErrorDetail(
    @SerializedName("code")
    val code: Int? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("errorMessage")
    val errorMessage: String? = null,
    @SerializedName("path")
    val path: String? = null,
    @SerializedName("timestamp")
    val timestamp: String? = null,
)
