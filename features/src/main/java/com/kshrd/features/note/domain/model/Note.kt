package com.kshrd.features.note.domain.model

import com.google.gson.annotations.SerializedName

/** A note as the API returns it. Field names are the API contract; do not rename them. */
data class Note(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("createdAt")
    val createdAt: String,
)

/** Request body for creating a note. */
data class CreateNoteRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("body")
    val body: String,
)
