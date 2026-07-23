package com.example.hytp.core.network.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BannerItem(
    val imageUrl: String,
    val title: String,
    val linkType: Int = 0,
    val linkValue: String = "",
)
