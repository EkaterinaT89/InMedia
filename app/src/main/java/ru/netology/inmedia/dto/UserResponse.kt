package ru.netology.inmedia.dto

data class UserResponse(
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String? = null,
)