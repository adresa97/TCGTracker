package com.boogie_knight.tcgtracker.models

import kotlinx.serialization.Serializable

data class ImageURLData(
    val host: String,
    val folder: String,
    val file: String
)

@Serializable
data class JsonImageURL(
    val code: String,
    val host: String,
    val folder: String,
    val file: String
)