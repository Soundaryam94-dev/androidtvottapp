package com.example.myottapp.core.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val isKidsProfile: Boolean = false
)