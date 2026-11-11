package com.kshrd.core.base

import com.google.gson.annotations.SerializedName

/**
 * The response envelope every API endpoint returns. The backend owns this shape;
 * the app never renames or reorders these fields.
 */
data class BaseResponse<T>(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("payload")
    val data: T? = null,
    @SerializedName("requestedTime")
    val requestedTime: String? = null,
)
