package ru.netology.inmedia.dto

data class PushMessage(
    val recipientId: Long?,
    val content: String,
)