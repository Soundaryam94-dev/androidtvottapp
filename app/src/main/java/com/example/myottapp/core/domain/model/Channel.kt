package com.example.myottapp.core.domain.model

data class Channel(
    val id: String,
    val name: String,
    val description: String,
    val bannerUrl: String,
    val avatarUrl: String,
    val subscriberCount: Long,
    val videoCount: Int,
    val isSubscribed: Boolean = false
)