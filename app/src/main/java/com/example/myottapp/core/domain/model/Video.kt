package com.example.myottapp.core.domain.model

data class Video(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val channelId: String,
    val channelName: String,
    val channelAvatarUrl: String,
    val viewCount: Long,
    val likeCount: Long,
    val duration: Long,
    val publishedAt: String,
    val category: String
)